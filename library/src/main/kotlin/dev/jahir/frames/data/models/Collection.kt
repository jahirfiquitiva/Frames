package dev.jahir.frames.data.models

import android.os.Parcelable
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

@Parcelize
open class Collection(
    val name: String,
    var displayName: String = name,
    val wallpapers: ArrayList<Wallpaper> = ArrayList()
) : Parcelable {

    @IgnoredOnParcel
    val count: Int
        get() = wallpapers.size

    @IgnoredOnParcel
    var cover: Wallpaper? = null
        private set

    fun push(wallpaper: Wallpaper) {
        if (wallpapers.any { it.url == wallpaper.url }) return
        wallpapers.add(wallpaper)
    }

    fun setupCover(usedUrls: ArrayList<String>): ArrayList<String> {
        cover = wallpapers.firstOrNull { !usedUrls.contains(it.url) } ?: wallpapers.firstOrNull()
        cover?.let { usedUrls.add(it.url) }
        return usedUrls
    }
}
