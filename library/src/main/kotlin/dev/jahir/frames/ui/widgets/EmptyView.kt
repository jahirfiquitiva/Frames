package dev.jahir.frames.ui.widgets

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import dev.jahir.frames.R
import dev.jahir.frames.utils.extensions.*

class EmptyView @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    style: Int = 0
) :
    LinearLayout(context, attributeSet, style) {

    private val progressBar: ProgressBar? by findView(R.id.progress_bar)
    private val imageView: ImageView? by findView(R.id.icon)
    private val textView: TextView? by findView(R.id.text)

    init {
        inflate(context, R.layout.view_empty, this)
    }

    fun setText(@StringRes text: Int = 0) {
        try {
            setText(context.getString(text))
        } catch (e: Exception) {
        }
    }

    fun setText(text: String = "") {
        textView?.text = text
        textView?.visibleIf(text.hasContent())
    }

    fun setLoading() {
        imageView?.gone()
        textView?.gone()
        progressBar?.visible()
        show()
    }

    fun setEmpty(text: String = "") {
        progressBar?.gone()
        imageView?.visible()
        setText(text)
        show()
    }

    fun setEmpty(@StringRes text: Int = 0) {
        try {
            setEmpty(context.getString(text))
        } catch (e: Exception) {
        }
    }

    fun setImageDrawable(drawable: Drawable?) {
        imageView?.setImageDrawable(drawable)
    }

    fun setImageDrawable(@DrawableRes drawable: Int = 0) {
        try {
            setImageDrawable(ContextCompat.getDrawable(context, drawable))
        } catch (e: Exception) {
        }
    }

    fun show() {
        visible()
        imageView?.showAndAnimate()
    }

    fun hide() = gone()
}