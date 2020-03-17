package dev.jahir.frames.extensions

import androidx.palette.graphics.Palette
import java.util.*

internal const val MAX_FRAMES_PALETTE_COLORS = 6

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

val Palette.sortedSwatches: List<Palette.Swatch>
    get() = swatches.sortedByDescending { it?.population ?: 0 }
        .subList(0, MAX_FRAMES_PALETTE_COLORS)

val Palette.Swatch.bestTextColor: Int
    get() {
        return (if (rgb.isDark) titleTextColor.getLighter(bodyTextColor)
        else titleTextColor.getDarker(bodyTextColor))
            .withMinAlpha(.85F)
    }

private fun getBestPaletteSwatch(swatches: List<Palette.Swatch>): Palette.Swatch =
    Collections.max<Palette.Swatch>(swatches) { opt1, opt2 ->
        val a = opt1?.population ?: 0
        val b = opt2?.population ?: 0
        a - b
    }