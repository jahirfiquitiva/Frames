package dev.jahir.frames.ui.adapters

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import dev.jahir.frames.R
import dev.jahir.frames.data.models.Wallpaper
import dev.jahir.frames.extensions.views.inflate
import dev.jahir.frames.ui.viewholders.WallpaperViewHolder

internal class WallpapersAdapter(
    private val canShowFavoritesButton: Boolean = true,
    var canModifyFavorites: Boolean = true,
    var onClick: (Wallpaper, WallpaperViewHolder) -> Unit = { _, _ -> },
    var onFavClick: (Boolean, Wallpaper) -> Unit = { _, _ -> }
) : ListAdapter<Wallpaper, WallpaperViewHolder>(DIFFER) {

    override fun onBindViewHolder(holder: WallpaperViewHolder, position: Int) {
        holder.bind(
            getItem(position),
            canShowFavoritesButton,
            canModifyFavorites,
            onClick,
            onFavClick
        )
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WallpaperViewHolder =
        WallpaperViewHolder(parent.inflate(R.layout.item_wallpaper))

    override fun getItemId(position: Int): Long = position.toLong()
    override fun getItemViewType(position: Int): Int = position

    companion object {
        private val DIFFER = object : DiffUtil.ItemCallback<Wallpaper>() {
            override fun areItemsTheSame(oldItem: Wallpaper, newItem: Wallpaper): Boolean {
                return oldItem.name == newItem.name && oldItem.url == newItem.url
            }

            override fun areContentsTheSame(oldItem: Wallpaper, newItem: Wallpaper): Boolean {
                return oldItem == newItem
            }
        }
    }
}