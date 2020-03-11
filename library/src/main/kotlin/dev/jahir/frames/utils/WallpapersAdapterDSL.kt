package dev.jahir.frames.utils

import dev.jahir.frames.data.models.Wallpaper
import dev.jahir.frames.ui.adapters.WallpapersAdapter
import dev.jahir.frames.ui.viewholders.WallpaperViewHolder

internal fun wallpapersAdapter(block: WallpapersAdapter.() -> Unit): WallpapersAdapter =
    WallpapersAdapter().apply(block)

internal fun WallpapersAdapter.onClick(what: (Wallpaper, WallpaperViewHolder) -> Unit) {
    this.onClick = what
}

internal fun WallpapersAdapter.onFavClick(what: (Boolean, Wallpaper) -> Unit) {
    this.onFavClick = what
}