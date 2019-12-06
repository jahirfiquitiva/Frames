package dev.jahir.frames.data.models


data class Collection(val name: String, val wallpapers: ArrayList<Wallpaper> = ArrayList()) {
    val count: Int
        get() = wallpapers.size

    val cover: Wallpaper?
        get() {
            return try {
                wallpapers.random()
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }

    fun push(wallpaper: Wallpaper) {
        if (wallpapers.any { it.url == wallpaper.url }) return
        wallpapers.add(wallpaper)
    }
}
