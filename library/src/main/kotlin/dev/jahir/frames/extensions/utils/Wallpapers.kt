package dev.jahir.frames.extensions.utils

import dev.jahir.frames.data.models.Wallpaper
import dev.jahir.frames.extensions.resources.hasContent
import dev.jahir.frames.extensions.resources.lower

/**
 * Wallpapers whose name, collections or author contain [query], ignoring case.
 * Returns the list unchanged when [query] is blank.
 */
fun List<Wallpaper>.filteredBy(query: String?): List<Wallpaper> {
    if (!query.hasContent()) return this
    val lowerQuery = query.lower()
    return filter {
        it.name.lower().contains(lowerQuery) ||
                it.collections.orEmpty().lower().contains(lowerQuery) ||
                it.author.lower().contains(lowerQuery)
    }
}
