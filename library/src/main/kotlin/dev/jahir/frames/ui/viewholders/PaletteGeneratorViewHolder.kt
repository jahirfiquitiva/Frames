package dev.jahir.frames.ui.viewholders

import android.graphics.drawable.Drawable
import android.view.View
import androidx.annotation.ColorInt
import androidx.recyclerview.widget.RecyclerView
import dev.jahir.frames.R
import dev.jahir.frames.extensions.context.boolean
import dev.jahir.frames.extensions.resources.asBitmap
import dev.jahir.frames.extensions.utils.bestTextColor
import dev.jahir.frames.extensions.views.context
import dev.jahir.harmonic.colors.HarmonicColorExtractor

abstract class PaletteGeneratorViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    internal val shouldColorTiles: Boolean by lazy {
        context.boolean(R.bool.enable_colored_tiles)
    }

    internal val generatePalette: (drawable: Drawable?) -> Unit by lazy {
        val listener: ((drawable: Drawable?) -> Unit) = { drwb ->
            onDrawableReady(drwb)
            if (shouldColorTiles) {
                drwb?.asBitmap()?.let { bitmap ->
                    val harmonic = HarmonicColorExtractor()
                        .setBitmap(bitmap)
                        .setBottomSide()
                        .getColors()
                    doWithColors(harmonic.backgroundColor, harmonic.bestTextColor)
                }
            }
        }
        listener
    }

    open fun onDrawableReady(drawable: Drawable?) {}
    abstract fun doWithColors(@ColorInt bgColor: Int, @ColorInt textColor: Int)
}