package dev.jahir.frames.extensions

import androidx.palette.graphics.Palette
import java.util.*

internal const val MAX_FRAMES_PALETTE_COLORS = 6

val Palette.bestSwatch: Palette.Swatch?
    get() = bestSwatches.firstOrNull()

val Palette.bestSwatches: List<Palette.Swatch>
    get() {
        val bestSwatches = ArrayList<Palette.Swatch>()
        dominantSwatch?.let { bestSwatches.add(it) }
        vibrantSwatch?.let { bestSwatches.add(it) }
        mutedSwatch?.let { bestSwatches.add(it) }
        lightVibrantSwatch?.let { bestSwatches.add(it) }
        darkVibrantSwatch?.let { bestSwatches.add(it) }
        lightMutedSwatch?.let { bestSwatches.add(it) }
        darkMutedSwatch?.let { bestSwatches.add(it) }
        bestSwatches.addAll(swatches.filterNotNull())
        return bestSwatches
            .distinctBy { it.rgb }
            .subList(0, MAX_FRAMES_PALETTE_COLORS)
            .sortedByDescending { it.population }
    }

val Palette.Swatch.bestTextColor: Int
    get() {
        return (if (rgb.isDark) titleTextColor.getLighter(bodyTextColor)
        else titleTextColor.getDarker(bodyTextColor))
            .withMinAlpha(.85F)
    }