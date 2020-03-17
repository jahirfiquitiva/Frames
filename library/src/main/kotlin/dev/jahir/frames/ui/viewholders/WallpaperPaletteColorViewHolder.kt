package dev.jahir.frames.ui.viewholders

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context.CLIPBOARD_SERVICE
import android.view.View
import androidx.appcompat.widget.AppCompatButton
import androidx.palette.graphics.Palette
import com.afollestad.sectionedrecyclerview.SectionedViewHolder
import dev.jahir.frames.R
import dev.jahir.frames.extensions.bestTextColor
import dev.jahir.frames.extensions.context
import dev.jahir.frames.extensions.findView
import dev.jahir.frames.extensions.toHexString
import dev.jahir.frames.extensions.toast

class WallpaperPaletteColorViewHolder(view: View) : SectionedViewHolder(view) {

    private val colorBtn: AppCompatButton? by view.findView(R.id.palette_color_btn)

    fun bind(swatch: Palette.Swatch? = null) {
        swatch ?: return
        colorBtn?.setBackgroundColor(swatch.rgb)
        colorBtn?.setTextColor(swatch.bestTextColor)
        colorBtn?.text = swatch.rgb.toHexString()
        colorBtn?.setOnClickListener {
            val clipboard = context.getSystemService(CLIPBOARD_SERVICE) as? ClipboardManager
            clipboard?.setPrimaryClip(
                ClipData.newPlainText("label", swatch.rgb.toHexString())
            )
            context.toast(R.string.copied_to_clipboard)
        }
    }
}