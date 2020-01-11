package dev.jahir.frames.ui.widgets

import android.content.Context
import android.util.AttributeSet
import androidx.annotation.IdRes
import com.google.android.material.bottomnavigation.BottomNavigationView

class FramesBottomNavigationView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : BottomNavigationView(context, attrs, defStyle) {

    fun setSelectedItemId(@IdRes itemId: Int, triggerEvent: Boolean = true) {
        try {
            if (triggerEvent) super.setSelectedItemId(itemId)
            else menu.findItem(itemId)?.isChecked = true
        } catch (e: Exception) {
        }
    }
}