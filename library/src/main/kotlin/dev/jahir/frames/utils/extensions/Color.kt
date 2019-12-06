package dev.jahir.frames.utils.extensions

import androidx.core.graphics.ColorUtils

val Int.isDark: Boolean
    get() = isDark(0.5)

fun Int.isDark(threshold: Double = 0.5) = ColorUtils.calculateLuminance(this) < threshold