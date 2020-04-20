package dev.jahir.frames.extensions.utils

import androidx.palette.graphics.Palette
import com.apitiphy.harmoniccolorextractor.HarmonicColors
import dev.jahir.frames.extensions.resources.getDarker
import dev.jahir.frames.extensions.resources.getLighter
import dev.jahir.frames.extensions.resources.isDark
import dev.jahir.frames.extensions.resources.withMinAlpha

internal const val MAX_FRAMES_PALETTE_COLORS = 6
private const val MIN_TEXT_ALPHA = 1F

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
        val distinctSwatches = bestSwatches.distinctBy { it.rgb }
        val maxSize =
            if (distinctSwatches.size <= MAX_FRAMES_PALETTE_COLORS) distinctSwatches.size
            else MAX_FRAMES_PALETTE_COLORS
        return distinctSwatches
            .subList(0, maxSize)
            .sortedByDescending { it.population }
    }

val Palette.Swatch.bestTextColor: Int
    get() {
        return (if (rgb.isDark) titleTextColor.getLighter(bodyTextColor)
        else titleTextColor.getDarker(bodyTextColor))
            .withMinAlpha(MIN_TEXT_ALPHA)
    }

val HarmonicColors.primaryTextColor: Int
    get() = firstForegroundColor

val HarmonicColors.secondaryTextColor: Int
    get() = secondForegroundColor

val HarmonicColors.bestTextColor: Int
    get() {
        val preTextColor =
            if (backgroundColor.isDark) primaryTextColor.getLighter(secondaryTextColor)
            else primaryTextColor.getDarker(secondaryTextColor)
        return preTextColor.withMinAlpha(MIN_TEXT_ALPHA)
    }