package dev.jahir.frames.ui.viewholders

import android.view.View
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.cardview.widget.CardView
import com.afollestad.sectionedrecyclerview.SectionedViewHolder
import dev.jahir.frames.R
import dev.jahir.frames.extensions.findView
import dev.jahir.frames.extensions.toHexString

class WallpaperPaletteColorViewHolder(view: View) : SectionedViewHolder(view) {

    private val cardView: CardView? by view.findView(R.id.palette_color_card)
    private val colorTextView: TextView? by view.findView(R.id.palette_color_card)

    fun bind(@ColorInt color: Int) {
        cardView?.setCardBackgroundColor(color)
        colorTextView?.text = color.toHexString()
    }
}