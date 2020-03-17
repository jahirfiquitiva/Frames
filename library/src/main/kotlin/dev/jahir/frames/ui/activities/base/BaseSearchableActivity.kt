package dev.jahir.frames.ui.activities.base

import android.view.Menu
import android.view.MenuItem
import androidx.annotation.MenuRes
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import dev.jahir.frames.R
import dev.jahir.frames.extensions.findView
import dev.jahir.frames.extensions.resolveColor
import dev.jahir.frames.ui.widgets.CleanSearchView
import dev.jahir.frames.utils.Prefs
import dev.jahir.frames.utils.postDelayed
import dev.jahir.frames.utils.tintIcons

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
            searchView?.onCollapse = {
                doSearch(closed = true)
                invalidateOptionsMenu()
            }
            searchView?.onQueryChanged = { query -> doSearch(query) }
            searchView?.onQuerySubmit = { query -> doSearch(query) }
            searchView?.bindToItem(searchItem)
            updateSearchHint()

            toolbar?.tintIcons(
                resolveColor(
                    R.attr.colorOnPrimary,
                    ContextCompat.getColor(this, R.color.onPrimary)
                )
            )
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
}