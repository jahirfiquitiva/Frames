package dev.jahir.frames.ui.activities

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.AnimationSet
import android.view.animation.DecelerateInterpolator
import androidx.annotation.IdRes
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import dev.jahir.frames.R
import dev.jahir.frames.data.models.Collection
import dev.jahir.frames.data.models.Wallpaper
import dev.jahir.frames.extensions.findView
import dev.jahir.frames.extensions.getAppName
import dev.jahir.frames.extensions.hasContent
import dev.jahir.frames.extensions.invisible
import dev.jahir.frames.extensions.string
import dev.jahir.frames.extensions.visible
import dev.jahir.frames.ui.activities.base.BaseBillingActivity
import dev.jahir.frames.ui.fragments.CollectionsFragment
import dev.jahir.frames.ui.fragments.WallpapersFragment
import dev.jahir.frames.ui.fragments.base.BaseFramesFragment
import dev.jahir.frames.utils.Prefs

@Suppress("LeakingThis", "MemberVisibilityCanBePrivate")
abstract class FramesActivity : BaseBillingActivity<Prefs>() {

    override val prefs: Prefs by lazy { Prefs(this) }

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
        changeFragment(initialItemId, true, false)

        bottomNavigation?.selectedItemId = initialItemId
        bottomNavigation?.setOnNavigationItemSelectedListener { changeFragment(it.itemId) }

        wallpapersViewModel.observeWallpapers(this) { wallpapersFragment?.updateItems(ArrayList(it)) }
        wallpapersViewModel.observeCollections(this, ::handleCollectionsUpdate)
        loadWallpapersData()
    }

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
            R.id.donate -> showInAppPurchasesDialog()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun getSearchHint(itemId: Int): String = when (itemId) {
        R.id.wallpapers -> string(R.string.search_wallpapers)
        R.id.collections -> string(R.string.search_collections)
        R.id.favorites -> string(R.string.search_favorites)
        else -> string(R.string.search_x)
    }

    open fun getToolbarTitleForItem(itemId: Int): String? = null

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
                    supportActionBar?.title =
                        getToolbarTitleForItem(itemId) ?: getAppName("MyWallApp")
                }
                shouldSelectItem
            } ?: false
        }
        return false
    }

    private fun internalLoadFragment(fragment: Fragment?, tag: String, force: Boolean = false) {
        fragment ?: return
        if (currentFragment !== fragment || force) {
            replaceFragment(fragment, tag)
            currentFragment = fragment
            invalidateOptionsMenu()
            updateSearchHint()
        }
    }

    private fun loadFragment(
        fragment: Fragment?,
        tag: String,
        force: Boolean = false,
        animate: Boolean = true
    ) {
        if (animate && prefs.animationsEnabled)
            fadeFragmentTransition { internalLoadFragment(fragment, tag, force) }
        else internalLoadFragment(fragment, tag, force)
    }

    private fun fadeFragmentTransition(
        @IdRes viewId: Int = R.id.fragments_container,
        fragmentTransaction: () -> Unit = {}
    ) {
        val fragmentsContainer by findView<View>(viewId)

        fragmentsContainer?.let { container ->
            val fadeOut = AlphaAnimation(1F, 0F)
            fadeOut.interpolator = DecelerateInterpolator()
            fadeOut.duration = FRAGMENT_TRANSITION_DURATION

            fadeOut.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationEnd(p0: Animation?) {
                    container.invisible()
                    fragmentTransaction()

                    val fadeIn = AlphaAnimation(0F, 1F)
                    fadeIn.interpolator = AccelerateInterpolator()
                    fadeIn.startOffset = FRAGMENT_TRANSITION_DURATION
                    fadeIn.duration = FRAGMENT_TRANSITION_DURATION

                    val animation = AnimationSet(false)
                    animation.addAnimation(fadeIn)
                    container.visible()
                    container.animation = animation
                }

                override fun onAnimationRepeat(p0: Animation?) {}
                override fun onAnimationStart(p0: Animation?) {}
            })

            val animation = AnimationSet(false)
            animation.addAnimation(fadeOut)
            container.animation = animation
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
        private const val FRAGMENT_TRANSITION_DURATION = 100L
    }
}