package dev.jahir.frames.extensions

import dev.jahir.frames.data.models.Wallpaper

val Wallpaper.urlAsKey: String
    get() = url.replace("[^A-Za-z0-9]", "")

fun Wallpaper.buildImageTransitionName(index: Int = 0, key: String = urlAsKey): String =
    "image_${index}_$key"

fun Wallpaper.buildTitleTransitionName(index: Int = 0, key: String = urlAsKey): String =
    "title_${index}_$key"

fun Wallpaper.buildAuthorTransitionName(index: Int = 0, key: String = urlAsKey): String =
    "author_${index}_$key"