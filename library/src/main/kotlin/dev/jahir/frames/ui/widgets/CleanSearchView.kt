package dev.jahir.frames.ui.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.MenuItem
import android.view.inputmethod.EditorInfo
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.widget.SearchView
import dev.jahir.frames.R
import dev.jahir.frames.extensions.context.color
import dev.jahir.frames.extensions.context.resolveColor
import dev.jahir.frames.extensions.resources.tint
import dev.jahir.frames.extensions.views.gone

class CleanSearchView @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    style: Int = 0
) : SearchView(context, attributeSet, style) {

    var isOpen = false
        private set

    var allowKeyboardHideOnSubmit = false

    var onExpand: () -> Unit = {}
    var onCollapse: () -> Unit = {}

    var onQueryChanged: (query: String) -> Unit = {}
    var onQuerySubmit: (query: String) -> Unit = {}

    init {
        init()
    }

    private fun init() {
        maxWidth = Int.MAX_VALUE
        layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        imeOptions = EditorInfo.IME_ACTION_SEARCH
        setIconifiedByDefault(false)
        isIconified = false
        removeSearchIcon()

        super.setOnQueryTextListener(object : OnQueryTextListener {
            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let { onQueryChanged(it.trim()) }
                return true
            }

            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let { onQuerySubmit(it.trim()) }
                if (allowKeyboardHideOnSubmit) clearFocus()
                return true
            }
        })
    }

    private fun removeSearchIcon() {
        try {
            val magImage = findViewById<ImageView?>(androidx.appcompat.R.id.search_mag_icon)
            magImage?.setImageDrawable(null)
            magImage?.gone()
        } catch (e: Exception) {
        }
    }

    override fun setIconified(iconify: Boolean) {
        super.setIconified(false)
    }

    override fun setIconifiedByDefault(iconified: Boolean) {
        super.setIconifiedByDefault(false)
    }

    override fun setOnQueryTextListener(listener: OnQueryTextListener?) {
        this.onQuerySubmit = { listener?.onQueryTextSubmit(it) }
        this.onQueryChanged = { listener?.onQueryTextChange(it) }
    }

    fun bindToItem(item: MenuItem?) {
        item?.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(p0: MenuItem): Boolean {
                onExpand()
                isOpen = true
                return true
            }

            override fun onMenuItemActionCollapse(p0: MenuItem): Boolean {
                onCollapse()
                isOpen = false
                return true
            }
        })
        item?.icon?.tint(
            context.resolveColor(R.attr.colorOnPrimary, context.color(R.color.onPrimary))
        )
    }
}