package dev.jahir.frames.ui.activities

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import dev.jahir.frames.R
import dev.jahir.frames.data.Preferences
import dev.jahir.frames.data.models.Collection
import dev.jahir.frames.data.models.Wallpaper
import dev.jahir.frames.extensions.context.drawable
import dev.jahir.frames.extensions.context.getAppName
import dev.jahir.frames.extensions.context.string
import dev.jahir.frames.extensions.resources.hasContent
import dev.jahir.frames.ui.activities.base.BaseBillingActivity
import dev.jahir.frames.ui.fragments.CollectionsFragment
import dev.jahir.frames.ui.fragments.WallpapersFragment
import dev.jahir.frames.ui.fragments.base.BaseFramesFragment

@Suppress("LeakingThis", "MemberVisibilityCanBePrivate")
abstract class FramesActivity : BaseBillingActivity<Preferences>() {

    override val preferences: Preferences by lazy { Preferences(this) }

    open val wallpapersFragment: WallpapersFragment? by lazy {
        WallpapersFragment.create(ArrayList(wallpapersViewModel.wallpapers), canModifyFavorites())
    }
    open val collectionsFragment: CollectionsFragment? by lazy {
        CollectionsFragment.create(ArrayList(wallpapersViewModel.collections))
    }
    open val favoritesFragment: WallpapersFragment? by lazy {
        WallpapersFragment.createForFavs(
            ArrayList(wallpapersViewModel.favorites),
            canModifyFavorites()
        )
    }

    var currentFragment: Fragment? = null
        private set

    open val initialFragmentTag: String = WallpapersFragment.TAG

    private var currentTag: String = initialFragmentTag
    private var oldTag: String = initialFragmentTag

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(getLayoutRes())

        setSupportActionBar(toolbar)
        changeFragment(initialItemId, force = true)

        bottomNavigation?.selectedItemId = initialItemId
        bottomNavigation?.setOnNavigationItemSelectedListener { changeFragment(it.itemId) }

        wallpapersViewModel.observeWallpapers(this) { wallpapersFragment?.updateItems(ArrayList(it)) }
        wallpapersViewModel.observeCollections(this, ::handleCollectionsUpdate)
        loadWallpapersData(true)
    }

    fun updateToolbarTitle(itemId: Int = currentItemId) {
        var logoSet = false
        if (shouldShowToolbarLogo(itemId)) {
            drawable(string(R.string.toolbar_logo))?.let {
                supportActionBar?.setDisplayShowTitleEnabled(false)
                supportActionBar?.setLogo(it)
                logoSet = true
            }
        }
        if (!logoSet) {
            supportActionBar?.setLogo(null)
            supportActionBar?.title = getToolbarTitleForItem(itemId) ?: getAppName()
            supportActionBar?.setDisplayShowTitleEnabled(true)
        }
    }

    open fun shouldShowToolbarLogo(itemId: Int): Boolean = itemId == initialItemId

    @LayoutRes
    open fun getLayoutRes(): Int = R.layout.activity_fragments_bottom_navigation

    override fun onFavoritesUpdated(favorites: List<Wallpaper>) {
        super.onFavoritesUpdated(favorites)
        favoritesFragment?.updateItems(ArrayList(favorites))
    }

    override fun getMenuRes(): Int = R.menu.toolbar_menu

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.about -> startActivity(Intent(this, AboutActivity::class.java))
            R.id.settings -> startActivity(Intent(this, SettingsActivity::class.java))
            R.id.donate -> showDonationsDialog()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun getSearchHint(itemId: Int): String = when (itemId) {
        R.id.wallpapers -> string(R.string.search_wallpapers)
        R.id.collections -> string(R.string.search_collections)
        R.id.favorites -> string(R.string.search_favorites)
        else -> string(R.string.search_x)
    }

    open fun getToolbarTitleForItem(itemId: Int): String? =
        when (itemId) {
            R.id.collections -> string(R.string.collections)
            R.id.favorites -> string(R.string.favorites)
            else -> null
        }

    open fun getNextFragment(itemId: Int): Pair<Pair<String?, Fragment?>?, Boolean>? =
        when (itemId) {
            R.id.wallpapers -> Pair(Pair(WallpapersFragment.TAG, wallpapersFragment), true)
            R.id.collections -> Pair(Pair(CollectionsFragment.TAG, collectionsFragment), true)
            R.id.favorites -> Pair(Pair(WallpapersFragment.FAVS_TAG, favoritesFragment), true)
            else -> null
        }

    @Suppress("MemberVisibilityCanBePrivate")
    fun changeFragment(itemId: Int, force: Boolean = false, animate: Boolean = true): Boolean {
        if (currentItemId != itemId || force) {
            val next = getNextFragment(itemId)
            // Pair ( Pair ( fragmentTag, fragment ) , shouldShowItemAsSelected )
            val nextFragmentTag = next?.first?.first.orEmpty()
            if (!nextFragmentTag.hasContent()) return false
            val nextFragment = next?.first?.second
            val shouldSelectItem = next?.second == true
            return nextFragment?.let {
                if (shouldSelectItem) {
                    oldTag = currentTag
                    currentItemId = itemId
                    currentTag = nextFragmentTag
                    loadFragment(nextFragment, currentTag, force, animate)
                    updateToolbarTitle(itemId)
                }
                shouldSelectItem
            } ?: false
        }
        return false
    }

    private fun loadFragment(
        fragment: Fragment?,
        tag: String,
        force: Boolean = false,
        animate: Boolean = true
    ) {
        fragment ?: return
        if (currentFragment !== fragment || force) {
            replaceFragment(fragment, tag, animate = animate)
            currentFragment = fragment
            invalidateOptionsMenu()
            updateSearchHint()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString(CURRENT_FRAGMENT_KEY, currentTag)
        super.onSaveInstanceState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        currentTag = savedInstanceState.getString(CURRENT_FRAGMENT_KEY, currentTag) ?: currentTag
        changeFragment(currentItemId, true)
    }

    override fun internalDoSearch(filter: String, closed: Boolean) {
        super.internalDoSearch(filter, closed)
        (currentFragment as? BaseFramesFragment<*>)?.let {
            it.setRefreshEnabled(!filter.hasContent())
            it.applyFilter(filter, closed)
        }
    }

    open fun handleCollectionsUpdate(collections: ArrayList<Collection>) {
        collectionsFragment?.updateItems(collections)
    }

    companion object {
        private const val CURRENT_FRAGMENT_KEY = "current_fragment"
    }
}