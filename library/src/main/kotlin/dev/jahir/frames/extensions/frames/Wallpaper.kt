package dev.jahir.frames.extensions.frames

import dev.jahir.frames.extensions.resources.hasContent

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
