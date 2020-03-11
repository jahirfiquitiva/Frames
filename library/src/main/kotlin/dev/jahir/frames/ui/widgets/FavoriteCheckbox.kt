package dev.jahir.frames.ui.widgets

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatCheckBox

class FavoriteCheckbox @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    style: Int = 0
) : AppCompatCheckBox(context, attributeSet, style) {

    internal var canCheck = true
    internal var onDisabledClickListener: (() -> Unit)? = {}

    override fun performClick(): Boolean {
        return if (canCheck) super.performClick()
        else {
            onDisabledClickListener?.invoke()
            true
        }
    }
}