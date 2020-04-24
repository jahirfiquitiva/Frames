package dev.jahir.frames.extensions.frames

import dev.jahir.frames.data.models.Wallpaper
import dev.jahir.frames.extensions.resources.hasContent

val Wallpaper.urlAsKey: String
    get() = url.replace("[^A-Za-z0-9]", "")

fun Wallpaper.buildImageTransitionName(index: Int = 0, key: String = urlAsKey): String =
    "image_${index}_$key"

fun Wallpaper.buildTitleTransitionName(index: Int = 0, key: String = urlAsKey): String =
    "title_${index}_$key"

fun Wallpaper.buildAuthorTransitionName(index: Int = 0, key: String = urlAsKey): String =
    "author_${index}_$key"

internal val String.filenameAndExtension: Pair<String, String>
    get() {
        var filename = substring(lastIndexOf("/") + 1)
        var extension = try {
            filename.substring(filename.lastIndexOf("."))
        } catch (e: Exception) {
            ""
        }
        filename = try {
            filename.substring(0, filename.lastIndexOf("."))
        } catch (e: Exception) {
            filename
        }
        if (filename.startsWith("uc?id=", true)) filename = filename.substring(6)
        if (!extension.hasContent()) extension = ".jpg"
        return Pair(filename, extension)
    }