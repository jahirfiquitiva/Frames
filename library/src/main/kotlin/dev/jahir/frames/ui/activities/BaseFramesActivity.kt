package dev.jahir.frames.ui.activities

import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import dev.jahir.frames.R
import dev.jahir.frames.ui.activities.base.BaseFavoritesConnectedActivity
import dev.jahir.frames.ui.fragments.CollectionsFragment
import dev.jahir.frames.ui.fragments.WallpapersFragment
import dev.jahir.frames.ui.fragments.base.FragmentState

class BaseFramesActivity : BaseFavoritesConnectedActivity() {

    private val bottomBar: BottomNavigationView? by lazy { findViewById<BottomNavigationView?>(R.id.bottom_bar) }

    private val wallpapersFragment: WallpapersFragment by lazy {
        WallpapersFragment.create(ArrayList(wallpapersViewModel.wallpapers), wallpapersViewModel)
    }
    private val collectionsFragment: CollectionsFragment by lazy {
        CollectionsFragment.create(ArrayList(wallpapersViewModel.collections))
    }
    private val favoritesFragment: WallpapersFragment by lazy {
        WallpapersFragment.createForFavs(
            ArrayList(wallpapersViewModel.favorites),
            wallpapersViewModel
        )
    }

    private var currentFragment: Fragment? = null
    private val listState = mutableListOf<FragmentState>()
    private var currentTag: String = WallpapersFragment.TAG
    private var oldTag: String = WallpapersFragment.TAG
    private var currentMenuItemId: Int = R.id.wallpapers

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_base)

        val toolbar: Toolbar? = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        loadFirstFragment()

        bottomBar?.setOnNavigationItemSelectedListener { menuItem ->
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
        bottomBar?.selectedItemId = when (currentTag) {
            CollectionsFragment.TAG -> R.id.collections
            WallpapersFragment.FAVS_TAG -> R.id.favorites
            else -> R.id.wallpapers
        }

        wallpapersViewModel.observeWallpapers(this) { wallpapersFragment.updateItems(ArrayList(it)) }
        wallpapersViewModel.observeCollections(this) { collectionsFragment.updateItems(it) }
        wallpapersViewModel.observeFavorites(this) { favoritesFragment.updateItems(ArrayList(it)) }
        loadData()

        /*
        val fab: FloatingActionButton? = findViewById(R.id.fab)
        fab?.setOnClickListener { changeNightMode(!isNightMode, true) }
        */
    }

    internal fun loadData() {
        wallpapersViewModel.loadData(this, getString(R.string.json_url))
    }

    private fun recoverFragment() {
        val lastState = listState.last()
        listState.removeAt(listState.size - 1)

        currentTag = lastState.currentFragmentTag
        oldTag = lastState.previousFragmentTag

        val ft = supportFragmentManager.beginTransaction()

        val currentFragment = supportFragmentManager.findFragmentByTag(currentTag)
        val oldFragment = supportFragmentManager.findFragmentByTag(oldTag)

        if (currentFragment?.isVisible == true && oldFragment?.isHidden == true) {
            ft.hide(currentFragment).show(oldFragment)
        }

        ft.commit()

        val menu = bottomBar?.menu
        when (oldTag) {
            WallpapersFragment.TAG -> menu?.getItem(0)?.isChecked = true
            CollectionsFragment.TAG -> menu?.getItem(1)?.isChecked = true
            WallpapersFragment.FAVS_TAG -> menu?.getItem(2)?.isChecked = true
        }
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
            addBackStack()
        }
    }

    private fun addBackStack() {
        when (listState.size) {
            MAX_HISTORIC -> {
                listState[1].previousFragmentTag = WallpapersFragment.TAG
                val firstState = listState[1]
                for (i in listState.indices) {
                    if (listState.indices.contains((i + 1))) {
                        listState[i] = listState[i + 1]
                    }
                }
                listState[0] = firstState
                listState[listState.lastIndex] = FragmentState(currentTag, oldTag)
            }
            else -> listState.add(FragmentState(currentTag, oldTag))
        }
    }

    override fun onBackPressed() {
        if (listState.size > 1) {
            recoverFragment()
        } else {
            super.onBackPressed()
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

    override fun onPermissionsAccepted(permissions: Array<out String>) {
        // Not necessary
    }

    companion object {
        private const val CURRENT_FRAGMENT_KEY = "currentFragment"
        private const val MAX_HISTORIC = 2
    }
}