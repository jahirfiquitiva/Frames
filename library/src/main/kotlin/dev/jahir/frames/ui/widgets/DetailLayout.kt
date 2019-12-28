package dev.jahir.frames.ui.widgets

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.StringRes
import dev.jahir.frames.R
import dev.jahir.frames.extensions.findView

class DetailLayout @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    style: Int = 0
) :
    LinearLayout(context, attributeSet, style) {

    private val titleTextView: TextView? by findView(R.id.detail_title)
    private val descriptionTextView: TextView? by findView(R.id.detail_description)

    init {
        inflate(context, R.layout.item_wallpaper_detail, this)
    }

    fun setTitle(@StringRes res: Int) {
        setTitle(context.getString(res))
    }

    fun setTitle(title: String) {
        titleTextView?.text = title
    }

    fun setDescription(@StringRes res: Int) {
        setDescription(context.getString(res))
    }

    fun setDescription(description: String) {
        descriptionTextView?.text = description
    }
}