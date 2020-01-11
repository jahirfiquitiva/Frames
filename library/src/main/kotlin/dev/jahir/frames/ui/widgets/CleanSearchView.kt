package dev.jahir.frames.ui.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.annotation.ColorInt
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import dev.jahir.frames.R
import dev.jahir.frames.extensions.findView
import dev.jahir.frames.extensions.gone
import dev.jahir.frames.extensions.resolveColor
import dev.jahir.frames.extensions.withAlpha
import dev.jahir.frames.utils.tint

class CleanSearchView @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    style: Int = 0
) :
    SearchView(context, attributeSet, style) {

    private var tintColor: Int = 0

    var isOpen = false
        private set

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
                return true
            }
        })

        tint(
            context.resolveColor(
                R.attr.colorOnPrimary,
                ContextCompat.getColor(context, R.color.onPrimary)
            )
        )
    }

    private fun removeSearchIcon() {
        try {
            val magImage = findViewById<ImageView?>(androidx.appcompat.R.id.search_mag_icon)
            magImage?.setImageDrawable(null)
            magImage?.gone()
        } catch (e: Exception) {
        }
    }

    @Suppress("MemberVisibilityCanBePrivate")
    fun tint(@ColorInt color: Int, @ColorInt hintColor: Int = color) {
        this.tintColor = color

        val field: EditText? by findView(R.id.search_src_text)
        field?.setTextColor(color)
        field?.setHintTextColor(
            if (hintColor == color) hintColor.withAlpha(0.5F) else hintColor
        )
        field?.tint(color)

        val plate: View? by findView(R.id.search_plate)
        plate?.background = null

        val searchIcon: ImageView? by findView(R.id.search_button)
        searchIcon?.tint(color)

        val closeIcon: ImageView? by findView(R.id.search_close_btn)
        closeIcon?.tint(color)

        val goIcon: ImageView? by findView(R.id.search_go_btn)
        goIcon?.tint(color)

        val voiceIcon: ImageView? by findView(R.id.search_voice_btn)
        voiceIcon?.tint(color)

        val collapsedIcon: ImageView? by findView(R.id.search_mag_icon)
        collapsedIcon?.tint(color)
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
            override fun onMenuItemActionExpand(item: MenuItem?): Boolean {
                onExpand()
                isOpen = true
                return true
            }

            override fun onMenuItemActionCollapse(item: MenuItem?): Boolean {
                onCollapse()
                isOpen = false
                return true
            }
        })
        item?.icon?.tint(
            context.resolveColor(
                R.attr.colorOnPrimary,
                ContextCompat.getColor(context, R.color.onPrimary)
            )
        )
    }
}