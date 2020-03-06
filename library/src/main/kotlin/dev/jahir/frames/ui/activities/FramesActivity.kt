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
import androidx.lifecycle.Lifecycle
import dev.jahir.frames.R
import dev.jahir.frames.data.models.Wallpaper
import dev.jahir.frames.extensions.findView
import dev.jahir.frames.extensions.hasContent
import dev.jahir.frames.extensions.invisible
import dev.jahir.frames.extensions.visible
import dev.jahir.frames.ui.activities.base.BaseDonationsActivity
import dev.jahir.frames.ui.fragments.CollectionsFragment
import dev.jahir.frames.ui.fragments.WallpapersFragment
import dev.jahir.frames.ui.fragments.base.BaseFramesFragment
import dev.jahir.frames.utils.Prefs

abstract class FramesActivity : BaseDonationsActivity<Prefs>() {

    override val prefs: Prefs by lazy { Prefs(this) }

    private val wallpapersFragment: WallpapersFragment by lazy {
        WallpapersFragment.create(
            ArrayList(wallpapersViewModel.wallpapers),
            canToggleSystemUIVisibility()
        )
    }
    val collectionsFragment: CollectionsFragment by lazy {
        CollectionsFragment.create(
            ArrayList(wallpapersViewModel.collections),
            canToggleSystemUIVisibility()
        )
    }
    val favoritesFragment: WallpapersFragment by lazy {
        WallpapersFragment.createForFavs(
            ArrayList(wallpapersViewModel.favorites),
            canToggleSystemUIVisibility()
        )
    }

    var currentFragment: Fragment? = null
        private set
    private var currentTag: String = INITIAL_FRAGMENT_TAG
    private var oldTag: String = INITIAL_FRAGMENT_TAG
    private var currentMenuItemId: Int = R.id.wallpapers

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(getLayoutRes())

        setSupportActionBar(toolbar)
        loadFirstFragment()

        bottomNavigation?.setOnNavigationItemSelectedListener { changeFragment(it.itemId) }
        bottomNavigation?.selectedItemId = when (currentTag) {
            CollectionsFragment.TAG -> R.id.collections
            WallpapersFragment.FAVS_TAG -> R.id.favorites
            else -> R.id.wallpapers
        }

        wallpapersViewModel.observeWallpapers(this) { wallpapersFragment.updateItems(ArrayList(it)) }
        wallpapersViewModel.observeCollections(this) { collectionsFragment.updateItems(it) }
        loadData()
    }

    @LayoutRes
    open fun getLayoutRes(): Int = R.layout.activity_base

    override fun onFavoritesUpdated(favorites: List<Wallpaper>) {
        super.onFavoritesUpdated(favorites)
        favoritesFragment.updateItems(ArrayList(favorites))
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

    open fun changeFragment(itemId: Int, force: Boolean = false): Boolean {
        if (currentMenuItemId != itemId || force) {
            var nextFragment: Fragment? = null
            oldTag = currentTag
            currentMenuItemId = itemId
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
            loadFragment(nextFragment, currentTag, force)
            return true
        }
        return false
    }

    private fun loadFragment(fragment: Fragment?, tag: String, force: Boolean = false) {
        fragment ?: return
        if (currentFragment !== fragment || force) {
            fadeFragmentTransition {
                val ft = supportFragmentManager.beginTransaction()
                currentFragment?.let { ft.hide(it).setMaxLifecycle(it, Lifecycle.State.STARTED) }
                if (fragment.isAdded) {
                    ft.show(fragment)
                } else {
                    ft.add(R.id.fragments_container, fragment, tag)
                }
                ft.setMaxLifecycle(fragment, Lifecycle.State.RESUMED)
                currentFragment = fragment
                ft.commit()
                updateSearchHint()
            }
        }
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
                    fadeIn.startOffset = FRAGMENT_TRANSITION_OFFSET_DURATION
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
        super.onSaveInstanceState(outState)
        outState.putString(CURRENT_FRAGMENT_KEY, currentTag)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        currentTag = savedInstanceState.getString(CURRENT_FRAGMENT_KEY, currentTag) ?: currentTag
    }

    override fun internalDoSearch(filter: String, closed: Boolean) {
        super.internalDoSearch(filter, closed)
        (currentFragment as? BaseFramesFragment<*>)?.let {
            it.setRefreshEnabled(!filter.hasContent())
            it.applyFilter(filter, closed)
        }
    }

    companion object {
        private const val CURRENT_FRAGMENT_KEY = "current_fragment"
        private const val INITIAL_FRAGMENT_TAG = WallpapersFragment.TAG
        private const val FRAGMENT_TRANSITION_DURATION = 100L
        private const val FRAGMENT_TRANSITION_OFFSET_DURATION = 50L
    }
}