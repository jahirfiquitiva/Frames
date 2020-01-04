package dev.jahir.frames.ui.viewholders

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context.CLIPBOARD_SERVICE
import android.view.View
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.palette.graphics.Palette
import com.afollestad.sectionedrecyclerview.SectionedViewHolder
import dev.jahir.frames.R
import dev.jahir.frames.extensions.*

class WallpaperPaletteColorViewHolder(view: View) : SectionedViewHolder(view) {

    private val cardView: CardView? by view.findView(R.id.palette_color_card)
    private val colorTextView: TextView? by view.findView(R.id.palette_color_text)

    fun bind(swatch: Palette.Swatch? = null) {
        swatch ?: return
        cardView?.setCardBackgroundColor(swatch.rgb)
        colorTextView?.text = swatch.rgb.toHexString()
        colorTextView?.setTextColor(swatch.bestTextColor)

        cardView?.setOnClickListener {
            val clipboard = context.getSystemService(CLIPBOARD_SERVICE) as? ClipboardManager
            clipboard?.setPrimaryClip(
                ClipData.newPlainText("label", swatch.rgb.toHexString())
            )
            context.toast(R.string.copied_to_clipboard)
        }
    }
}