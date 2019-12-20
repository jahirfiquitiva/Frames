package dev.jahir.frames.utils

import android.view.View
import dev.jahir.frames.data.models.Wallpaper
import dev.jahir.frames.ui.adapters.WallpapersAdapter

fun wallpapersAdapter(block: WallpapersAdapter.() -> Unit): WallpapersAdapter =
    WallpapersAdapter().apply(block)

fun WallpapersAdapter.onClick(what: (Wallpaper, T: View?) -> Unit) {
    this.onClick = what
}

fun WallpapersAdapter.onFavClick(what: (Boolean, Wallpaper) -> Unit) {
    this.onFavClick = what
}