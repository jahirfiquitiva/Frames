@file:Suppress("unused")

package dev.jahir.frames.extensions

import android.graphics.Color
import androidx.annotation.ColorInt
import androidx.annotation.FloatRange
import androidx.annotation.IntRange
import androidx.core.graphics.ColorUtils
import java.math.RoundingMode
import java.text.DecimalFormat
import kotlin.math.max
import kotlin.math.roundToInt

val Int.isDark: Boolean
    get() = isDark(0.5)

fun Int.isDark(threshold: Double = 0.5) = luminance < threshold

val Int.luminance
    get() = ColorUtils.calculateLuminance(this)

@ColorInt
fun Int.getLighter(other: Int): Int = if (other.luminance > luminance) other else this

@ColorInt
fun Int.getDarker(other: Int): Int = if (other.luminance < luminance) other else this

/**
 * Utils originally created by Allan Wang
 * Available at https://github.com/AllanWang/KAU
 * I have added them here (copy/pasted) because this lib doesn't really uses/needs all its features
 * at a 100%.
 * Anyway, full credits go to Allan, for these awesome extensions
 */

fun Int.toHexString(withAlpha: Boolean = false, withHexPrefix: Boolean = true): String {
    val hex = if (withAlpha) String.format("#%08X", this)
    else String.format("#%06X", 0xFFFFFF and this)
    return if (withHexPrefix) hex else hex.substring(1)
}

fun Int.toRgbaString(): String =
    "rgba(${Color.red(this)}, ${Color.green(this)}, ${Color.blue(this)}, ${(Color.alpha(
        this
    ) / 255f).round(3)})"

fun FloatArray.toColor(): Int = Color.HSVToColor(this)

@ColorInt
fun Int.adjustAlpha(factor: Float): Int {
    val alpha = (Color.alpha(this) * factor).roundToInt()
    return Color.argb(alpha, Color.red(this), Color.green(this), Color.blue(this))
}

@ColorInt
fun Int.blendWith(@ColorInt color: Int, @FloatRange(from = 0.0, to = 1.0) ratio: Float): Int {
    val inverseRatio = 1f - ratio
    val a = Color.alpha(this) * inverseRatio + Color.alpha(color) * ratio
    val r = Color.red(this) * inverseRatio + Color.red(color) * ratio
    val g = Color.green(this) * inverseRatio + Color.green(color) * ratio
    val b = Color.blue(this) * inverseRatio + Color.blue(color) * ratio
    return Color.argb(a.toInt(), r.toInt(), g.toInt(), b.toInt())
}

@ColorInt
fun Int.withAlpha(@IntRange(from = 0, to = 255) alpha: Int): Int =
    Color.argb(alpha, Color.red(this), Color.green(this), Color.blue(this))

@ColorInt
fun Int.withAlpha(@FloatRange(from = 0.0, to = 1.0) alpha: Float): Int =
    Color.argb((alpha * 255).roundToInt(), Color.red(this), Color.green(this), Color.blue(this))

@ColorInt
fun Int.withMinAlpha(@IntRange(from = 0, to = 255) alpha: Int): Int = Color.argb(
    max(alpha, Color.alpha(this)), Color.red(this), Color.green(this), Color.blue(this)
)

@ColorInt
fun Int.withMinAlpha(@FloatRange(from = 0.0, to = 1.0) alpha: Float): Int = Color.argb(
    max((alpha * 255).roundToInt(), Color.alpha(this)),
    Color.red(this), Color.green(this), Color.blue(this)
)

@ColorInt
fun Int.lighten(@FloatRange(from = 0.0, to = 1.0) factor: Float = 0.1f): Int {
    val (red, green, blue) = intArrayOf(Color.red(this), Color.green(this), Color.blue(this))
        .map { (it * (1f - factor) + 255f * factor).toInt() }
    return Color.argb(Color.alpha(this), red, green, blue)
}

@ColorInt
fun Int.darken(@FloatRange(from = 0.0, to = 1.0) factor: Float = 0.1f): Int {
    val (red, green, blue) = intArrayOf(Color.red(this), Color.green(this), Color.blue(this))
        .map { (it * (1f - factor)).toInt() }
    return Color.argb(Color.alpha(this), red, green, blue)
}

fun Number.round(@IntRange(from = 1L) decimalCount: Int): String {
    val expression = StringBuilder().append("#.")
    (1..decimalCount).forEach { _ -> expression.append("#") }
    val formatter = DecimalFormat(expression.toString())
    formatter.roundingMode = RoundingMode.HALF_UP
    return formatter.format(this)
}