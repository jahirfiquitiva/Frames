package dev.jahir.frames.ui.adapters

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import dev.jahir.frames.R
import dev.jahir.frames.data.models.Wallpaper
import dev.jahir.frames.ui.viewholders.WallpaperViewHolder
import dev.jahir.frames.utils.extensions.inflate

class WallpapersAdapter(private var onFavClick: (Boolean, Wallpaper) -> Unit) :
    RecyclerView.Adapter<WallpaperViewHolder>() {

    var wallpapers: ArrayList<Wallpaper> = ArrayList()
        set(value) {
            wallpapers.clear()
            wallpapers.addAll(value)
            notifyDataSetChanged()
        }

    override fun onBindViewHolder(holder: WallpaperViewHolder, position: Int) {
        holder.bind(wallpapers[position], onFavClick)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WallpaperViewHolder =
        WallpaperViewHolder(parent.inflate(R.layout.item_wallpaper))

    override fun getItemCount(): Int = wallpapers.size
}