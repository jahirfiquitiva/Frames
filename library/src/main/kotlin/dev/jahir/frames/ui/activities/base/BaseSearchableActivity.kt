package dev.jahir.frames.ui.activities.base

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.annotation.MenuRes
import androidx.appcompat.widget.Toolbar
import dev.jahir.frames.R
import dev.jahir.frames.extensions.color
import dev.jahir.frames.extensions.findView
import dev.jahir.frames.extensions.gone
import dev.jahir.frames.extensions.resolveColor
import dev.jahir.frames.extensions.visible
import dev.jahir.frames.ui.widgets.CleanSearchView
import dev.jahir.frames.utils.Prefs
import dev.jahir.frames.utils.postDelayed
import dev.jahir.frames.utils.tintIcons

@Suppress("LeakingThis")
abstract class BaseSearchableActivity<out P : Prefs> : BaseFavoritesConnectedActivity<P>() {

    val toolbar: Toolbar? by findView(R.id.toolbar)

    open val initialItemId: Int = R.id.wallpapers
    var currentItemId: Int = initialItemId
        internal set

    private var searchItem: MenuItem? = null
    private var searchView: CleanSearchView? = null

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(getMenuRes(), menu)

        menu?.let {
            searchItem = it.findItem(R.id.search)
            searchView = searchItem?.actionView as? CleanSearchView
            searchView?.onExpand = {
                it.findItem(R.id.search).isVisible = false
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

            toolbar?.tintIcons(resolveColor(R.attr.colorOnPrimary, color(R.color.onPrimary)))
            searchItem?.isVisible = canShowSearch(currentItemId)
        }

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
        outState.putInt(CURRENT_ITEM_KEY, currentItemId)
        super.onSaveInstanceState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        currentItemId = savedInstanceState.getInt(CURRENT_ITEM_KEY, initialItemId)
        bottomNavigation?.selectedItemId = currentItemId
    }

    companion object {
        private const val CURRENT_ITEM_KEY = "current_item"
    }
}