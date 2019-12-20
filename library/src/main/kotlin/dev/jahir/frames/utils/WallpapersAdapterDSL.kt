package dev.jahir.frames.utils

import dev.jahir.frames.data.models.Wallpaper
import dev.jahir.frames.ui.adapters.WallpapersAdapter

fun wallpapersAdapter(block: WallpapersAdapter.() -> Unit): WallpapersAdapter =
    WallpapersAdapter().apply(block)

fun WallpapersAdapter.onClick(what: (Wallpaper) -> Unit) {
    this.onClick = what
}

fun WallpapersAdapter.onFavClick(what: (Boolean, Wallpaper) -> Unit) {
    this.onFavClick = what
}