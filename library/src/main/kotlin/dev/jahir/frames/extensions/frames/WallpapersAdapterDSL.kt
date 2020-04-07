package dev.jahir.frames.extensions.frames

import dev.jahir.frames.data.models.Wallpaper
import dev.jahir.frames.ui.adapters.WallpapersAdapter
import dev.jahir.frames.ui.viewholders.WallpaperViewHolder

internal fun wallpapersAdapter(
    canShowFavoritesButton: Boolean = true,
    canModifyFavorites: Boolean = true,
    block: WallpapersAdapter.() -> Unit
): WallpapersAdapter =
    WallpapersAdapter(canShowFavoritesButton, canModifyFavorites).apply(block)

internal fun WallpapersAdapter.onClick(what: (Wallpaper, WallpaperViewHolder) -> Unit) {
    this.onClick = what
}

internal fun WallpapersAdapter.onFavClick(what: (Boolean, Wallpaper) -> Unit) {
    this.onFavClick = what
}