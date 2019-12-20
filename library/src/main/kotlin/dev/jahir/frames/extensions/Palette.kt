package dev.jahir.frames.extensions

import androidx.palette.graphics.Palette
import java.util.*

val Palette.bestSwatch: Palette.Swatch?
    get() {
        dominantSwatch?.let { return it }
        vibrantSwatch?.let { return it }
        mutedSwatch?.let { return it }
        lightVibrantSwatch?.let { return it }
        darkVibrantSwatch?.let { return it }
        lightMutedSwatch?.let { return it }
        darkMutedSwatch?.let { return it }
        if (swatches.isNotEmpty()) return getBestPaletteSwatch(swatches)
        return null
    }

private fun getBestPaletteSwatch(swatches: List<Palette.Swatch>): Palette.Swatch =
    Collections.max<Palette.Swatch>(swatches) { opt1, opt2 ->
        val a = opt1?.population ?: 0
        val b = opt2?.population ?: 0
        a - b
    }