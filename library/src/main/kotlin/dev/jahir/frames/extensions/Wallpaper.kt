package dev.jahir.frames.extensions

import dev.jahir.frames.data.models.Wallpaper

val Wallpaper.urlAsKey: String
    get() = url.replace("[^A-Za-z0-9]", "")

fun Wallpaper.urlAsKey(extra: String): String = "$urlAsKey$extra"