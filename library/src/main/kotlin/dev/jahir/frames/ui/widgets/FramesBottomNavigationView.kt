package dev.jahir.frames.ui.widgets

import android.content.Context
import android.util.AttributeSet
import androidx.annotation.IdRes
import com.google.android.material.bottomnavigation.BottomNavigationView
import dev.jahir.frames.R
import dev.jahir.frames.extensions.resolveColor

class FramesBottomNavigationView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : BottomNavigationView(context, attrs, defStyleAttr) {

    init {
        init(context, attrs)
    }

    private fun init(context: Context, attributeSet: AttributeSet?) {
        val a = context.obtainStyledAttributes(
            attributeSet, R.styleable.FramesBottomNavigationView, 0, 0
        )
        try {
            val forceRightColor =
                a.getBoolean(R.styleable.FramesBottomNavigationView_forceRightColor, false)
            if (forceRightColor) setBackgroundColor(context.resolveColor(R.attr.colorSurface))
        } finally {
            a.recycle()
        }
    }

    fun setSelectedItemId(@IdRes itemId: Int, triggerEvent: Boolean = true) {
        try {
            if (triggerEvent) super.setSelectedItemId(itemId)
            else menu.findItem(itemId)?.isChecked = true
        } catch (e: Exception) {
        }
    }

    fun removeItem(@IdRes itemId: Int) {
        try {
            menu.removeItem(itemId)
        } catch (e: Exception) {
        }
    }
}