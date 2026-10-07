package dev.jahir.frames.ui.activities.base

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.annotation.MenuRes
import androidx.appcompat.widget.Toolbar
import dev.jahir.frames.R
import dev.jahir.frames.data.Preferences
import dev.jahir.frames.extensions.context.findView
import dev.jahir.frames.extensions.utils.postDelayed
import dev.jahir.frames.extensions.views.gone
import dev.jahir.frames.extensions.views.goneIf
import dev.jahir.frames.extensions.views.tint
import dev.jahir.frames.extensions.views.visible
import dev.jahir.frames.ui.activities.ViewerActivity
import dev.jahir.frames.ui.animations.WallpaperReturnSharedElementCallback
import dev.jahir.frames.ui.fragments.WallpapersFragment
import dev.jahir.frames.ui.widgets.CleanSearchView

@Suppress("LeakingThis")
abstract class BaseSearchableActivity<out P : Preferences> : BaseFavoritesConnectedActivity<P>() {

    val toolbar: Toolbar? by findView(R.id.toolbar)

    open val initialItemId: Int = R.id.wallpapers
    var currentItemId: Int = initialItemId
        internal set

    private var searchItem: MenuItem? = null
    private var searchView: CleanSearchView? = null

    private val searchOpen: Boolean
        get() = searchView?.isOpen ?: false

    private val wallpaperReturnCallback = WallpaperReturnSharedElementCallback()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setExitSharedElementCallback(wallpaperReturnCallback)
    }

    // Runs before the return transition, so it can point it at the wallpaper the viewer ended on
    override fun onActivityReenter(resultCode: Int, data: Intent?) {
        super.onActivityReenter(resultCode, data)
        val url = data?.getStringExtra(ViewerActivity.CURRENT_WALLPAPER_URL_EXTRA) ?: return
        val fragment = supportFragmentManager.fragments
            .filterIsInstance<WallpapersFragment>()
            .firstOrNull { it.isVisible } ?: return
        var started = false
        val startReturnTransition = { card: View? ->
            if (!started) {
                started = true
                wallpaperReturnCallback.setReturnTarget(card)
                supportStartPostponedEnterTransition()
            }
        }
        supportPostponeEnterTransition()
        val isInFavorites = data.extras?.takeIf {
            it.containsKey(ViewerActivity.CURRENT_WALLPAPER_IN_FAVORITES_EXTRA)
        }?.getBoolean(ViewerActivity.CURRENT_WALLPAPER_IN_FAVORITES_EXTRA)
        fragment.findWallpaperCard(url, isInFavorites) { card -> startReturnTransition(card) }
        // The screen stays frozen while the transition is postponed, so never wait for the card
        // longer than this. Without a card it fades back instead of flying to the wrong one.
        postDelayed(RETURN_TRANSITION_TIMEOUT) { startReturnTransition(null) }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(getMenuRes(), menu)
        searchItem = menu.findItem(R.id.search)
        searchView = searchItem?.actionView as? CleanSearchView
        searchView?.allowKeyboardHideOnSubmit = true
        searchView?.onExpand = {
            searchItem?.isVisible = false
            bottomNavigation?.gone()
        }
        searchView?.onCollapse = {
            doSearch(closed = true)
            invalidateOptionsMenu()
            bottomNavigation?.visible()
        }
        searchView?.onQueryChanged = { query -> doSearch(query) }
        searchView?.onQuerySubmit = { query -> doSearch(query) }
        searchView?.bindToItem(searchItem)
        updateSearchHint()

        toolbar?.tint()
        searchItem?.isVisible = canShowSearch(currentItemId)
        return super.onCreateOptionsMenu(menu)
    }

    internal fun updateSearchHint() {
        searchView?.queryHint = getSearchHint(currentItemId)
    }

    private val lock by lazy { Any() }
    private fun doSearch(filter: String = "", closed: Boolean = false) {
        try {
            synchronized(lock) { postDelayed(100) { internalDoSearch(filter, closed) } }
        } catch (e: Exception) {
        }
    }

    @MenuRes
    open fun getMenuRes(): Int = 0

    open fun getSearchHint(itemId: Int): String = ""
    open fun canShowSearch(itemId: Int): Boolean = true
    open fun internalDoSearch(filter: String = "", closed: Boolean = false) {}

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(CURRENT_ITEM_KEY, currentItemId)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        currentItemId = savedInstanceState.getInt(CURRENT_ITEM_KEY, initialItemId)
        bottomNavigation?.selectedItemId = currentItemId
    }

    override fun onResume() {
        super.onResume()
        bottomNavigation?.goneIf(canShowSearch(currentItemId) && searchOpen)
    }

    companion object {
        private const val CURRENT_ITEM_KEY = "current_item"
        private const val RETURN_TRANSITION_TIMEOUT = 300L
    }
}
