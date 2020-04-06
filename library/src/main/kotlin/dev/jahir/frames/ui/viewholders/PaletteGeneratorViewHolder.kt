package dev.jahir.frames.ui.viewholders

import android.graphics.drawable.Drawable
import android.view.View
import androidx.palette.graphics.Palette
import androidx.recyclerview.widget.RecyclerView
import dev.jahir.frames.R
import dev.jahir.frames.extensions.asBitmap
import dev.jahir.frames.extensions.bestSwatch
import dev.jahir.frames.extensions.boolean
import dev.jahir.frames.extensions.context

abstract class PaletteGeneratorViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    internal val shouldColorTiles: Boolean by lazy {
        context.boolean(R.bool.enable_colored_tiles)
    }

    internal val generatePalette: (drawable: Drawable?) -> Unit by lazy {
        val listener: ((drawable: Drawable?) -> Unit) = {
            it?.asBitmap()?.let { bmp ->
                Palette.from(bmp)
                    .generate { plt ->
                        plt?.bestSwatch?.let { swatch -> doWithBestSwatch(swatch) }
                    }
            }
        }
        listener
    }

    abstract fun doWithBestSwatch(swatch: Palette.Swatch)
}