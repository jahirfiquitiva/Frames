package dev.jahir.frames.ui.widgets

import android.content.Context
import android.text.TextUtils
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.google.android.material.button.MaterialButton
import dev.jahir.frames.R
import dev.jahir.frames.extensions.context.openLink
import dev.jahir.frames.extensions.context.preferences
import dev.jahir.frames.extensions.views.inflate

/**
 * Originally created by Aidan Follestad (@afollestad)
 */
open class AboutButtonsLayout @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    style: Int = 0
) : LinearLayout(context, attributeSet, style) {

    init {
        init()
    }

    private fun init() {
        orientation = HORIZONTAL
        if (isInEditMode) {
            addButton("GitHub", "https://github.com/jahirfiquitiva")
            addButton("Website", "https://jahir.dev/")
        }
    }

    override fun setOrientation(orientation: Int) = super.setOrientation(HORIZONTAL)

    @Suppress("MemberVisibilityCanBePrivate")
    fun addButton(text: String, link: String) {
        if (childCount >= 3) {
            Log.e("Frames", "Cannot add more than 3 buttons.")
            return
        }
        val button: View? = try {
            this.inflate(R.layout.item_about_button)
        } catch (e: Exception) {
            null
        }
        button?.id = childCount
        button?.tag = link
        (button as? MaterialButton)?.let {
            it.maxLines = 1
            it.ellipsize = TextUtils.TruncateAt.END
            it.text = text
            it.setSupportAllCaps(!context.preferences.useMaterialYou)
            addView(
                it,
                LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            )
        }
        button?.setOnClickListener { context.openLink(link) }
    }
}
