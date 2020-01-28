package dev.jahir.frames.ui.activities

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.fragment.app.Fragment
import dev.jahir.frames.R
import dev.jahir.frames.extensions.hasContent
import dev.jahir.frames.ui.activities.base.BaseDonationsActivity
import dev.jahir.frames.ui.fragments.CollectionsFragment
import dev.jahir.frames.ui.fragments.WallpapersFragment
import dev.jahir.frames.ui.fragments.base.BaseFramesFragment
import dev.jahir.frames.utils.Prefs

abstract class FramesActivity : BaseDonationsActivity<Prefs>() {

    override val prefs: Prefs by lazy { Prefs(this) }

    private val wallpapersFragment: WallpapersFragment by lazy {
        WallpapersFragment.create(ArrayList(wallpapersViewModel.wallpapers))
    }
    private val collectionsFragment: CollectionsFragment by lazy {
        CollectionsFragment.create(ArrayList(wallpapersViewModel.collections))
    }
    private val favoritesFragment: WallpapersFragment by lazy {
        WallpapersFragment.createForFavs(ArrayList(wallpapersViewModel.favorites))
    }

    private var currentFragment: Fragment? = null
    private var currentTag: String = WallpapersFragment.TAG
    private var oldTag: String = WallpapersFragment.TAG
    private var currentMenuItemId: Int = R.id.wallpapers

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_base)

        setSupportActionBar(toolbar)
        loadFirstFragment()

        bottomNavigation?.setOnNavigationItemSelectedListener { menuItem ->
            if (currentMenuItemId != menuItem.itemId) {
                var nextFragment: Fragment? = null
                oldTag = currentTag
                currentMenuItemId = menuItem.itemId
                when (currentMenuItemId) {
                    R.id.wallpapers -> {
                        currentTag = WallpapersFragment.TAG
                        nextFragment = wallpapersFragment
                    }
                    R.id.collections -> {
                        currentTag = CollectionsFragment.TAG
                        nextFragment = collectionsFragment
                    }
                    R.id.favorites -> {
                        currentTag = WallpapersFragment.FAVS_TAG
                        nextFragment = favoritesFragment
                    }
                }
                loadFragment(nextFragment, currentTag)
                return@setOnNavigationItemSelectedListener true
            }
            false
        }
        bottomNavigation?.selectedItemId = when (currentTag) {
            CollectionsFragment.TAG -> R.id.collections
            WallpapersFragment.FAVS_TAG -> R.id.favorites
            else -> R.id.wallpapers
        }

        wallpapersViewModel.observeWallpapers(this) { wallpapersFragment.updateItems(ArrayList(it)) }
        wallpapersViewModel.observeCollections(this) { collectionsFragment.updateItems(it) }
        wallpapersViewModel.observeFavorites(this) { favoritesFragment.updateItems(ArrayList(it)) }
        loadData()
    }

    override fun getMenuRes(): Int = R.menu.toolbar_menu

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.about -> startActivity(Intent(this, AboutActivity::class.java))
            R.id.settings -> startActivity(Intent(this, SettingsActivity::class.java))
            R.id.donate -> launchDonationsFlow()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun getSearchHint(): String = when (currentTag) {
        WallpapersFragment.TAG -> getString(R.string.search_wallpapers)
        CollectionsFragment.TAG -> getString(R.string.search_collections)
        WallpapersFragment.FAVS_TAG -> getString(R.string.search_favorites)
        else -> getString(R.string.search_x)
    }

    private fun loadFirstFragment() {
        val transaction = supportFragmentManager.beginTransaction()
        currentFragment = when (currentTag) {
            CollectionsFragment.TAG -> collectionsFragment
            WallpapersFragment.FAVS_TAG -> favoritesFragment
            else -> wallpapersFragment
        }
        currentFragment?.let { transaction.add(R.id.fragments_container, it, currentTag) }
        transaction.commit()
    }

    private fun loadFragment(fragment: Fragment?, tag: String) {
        fragment ?: return
        if (currentFragment !== fragment) {
            val ft = supportFragmentManager.beginTransaction()
            if (fragment.isAdded) {
                currentFragment?.let { ft.hide(it).show(fragment) }
            } else {
                currentFragment?.let { ft.hide(it).add(R.id.fragments_container, fragment, tag) }
            }
            currentFragment = fragment
            ft.commit()
            updateSearchHint()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(CURRENT_FRAGMENT_KEY, currentTag)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        currentTag = savedInstanceState.getString(CURRENT_FRAGMENT_KEY, currentTag) ?: currentTag
    }

    override fun internalDoSearch(filter: String, closed: Boolean) {
        super.internalDoSearch(filter, closed)
        (currentFragment as? BaseFramesFragment<*>)?.setRefreshEnabled(!filter.hasContent())
        when (currentTag) {
            WallpapersFragment.TAG -> filterWallpapers(filter, closed)
            CollectionsFragment.TAG -> filterCollections(filter, closed)
            WallpapersFragment.FAVS_TAG -> filterFavorites(filter, closed)
        }
    }

    private fun filterWallpapers(filter: String = "", closed: Boolean = false) {
        (currentFragment as? WallpapersFragment)?.applyFilter(
            filter,
            ArrayList(wallpapersViewModel.wallpapers),
            closed
        )
    }

    private fun filterCollections(filter: String = "", closed: Boolean = false) {
        (currentFragment as? CollectionsFragment)?.applyFilter(
            filter,
            ArrayList(wallpapersViewModel.collections),
            closed
        )
    }

    private fun filterFavorites(filter: String = "", closed: Boolean = false) {
        (currentFragment as? WallpapersFragment)?.applyFilter(
            filter,
            ArrayList(wallpapersViewModel.favorites),
            closed
        )
    }

    companion object {
        private const val CURRENT_FRAGMENT_KEY = "current_fragment"
    }
}