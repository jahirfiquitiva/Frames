package dev.jahir.frames.extensions.resources

import java.util.concurrent.TimeUnit
import kotlin.math.ln
import kotlin.math.pow

fun Long.toReadableByteCount(si: Boolean = true): String {
    if (this <= 0L) return "-0"
    try {
        val unit = if (si) 1000 else 1024
        if (this < unit) return "$this B"
        val exp = (ln(this.toDouble()) / ln(unit.toDouble())).toInt()
        val pre = (if (si) "kMGTPE" else "KMGTPE")[exp - 1] + if (si) "" else "i"
        return String.format("%.1f %sB", this / unit.toDouble().pow(exp.toDouble()), pre)
    } catch (ignored: Exception) {
        return "-0"
    }
}

fun Long.toReadableTime(): String {
    val hr = TimeUnit.MILLISECONDS.toHours(this)
    val min = TimeUnit.MILLISECONDS.toMinutes(this - TimeUnit.HOURS.toMillis(hr))
    return String.format("%02d:%02d", hr, min)
}