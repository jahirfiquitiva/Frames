package dev.jahir.frames.extensions.resources

import java.util.*

fun String?.hasContent(): Boolean = orEmpty().isNotBlank() && orEmpty().isNotEmpty()

fun String?.lower(locale: Locale = Locale.ROOT): String = orEmpty().toLowerCase(locale)

fun String?.upper(locale: Locale = Locale.ROOT): String = orEmpty().toUpperCase(locale)