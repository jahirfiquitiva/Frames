package dev.jahir.frames.extensions

import java.util.*

fun String.hasContent(): Boolean = isNotBlank() && isNotEmpty()

fun String.lower(locale: Locale = Locale.ROOT) = toLowerCase(locale)

fun String.upper(locale: Locale = Locale.ROOT) = toUpperCase(locale)