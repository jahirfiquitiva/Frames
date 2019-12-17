package dev.jahir.frames.ui.viewholders

import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.AppCompatCheckBox
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.view.postDelayed
import androidx.recyclerview.widget.RecyclerView
import dev.jahir.frames.R
import dev.jahir.frames.data.models.Wallpaper
import dev.jahir.frames.utils.extensions.loadFramesPic

class WallpaperViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    private val image: AppCompatImageView? = view.findViewById(R.id.wallpaper_image)
    private val title: TextView? = view.findViewById(R.id.wallpaper_name)
    private val author: TextView? = view.findViewById(R.id.wallpaper_author)
    private val favorite: AppCompatCheckBox? = view.findViewById(R.id.fav_button)

    fun bind(wallpaper: Wallpaper, onFavClick: (Boolean, Wallpaper) -> Unit) {
        favorite?.setOnCheckedChangeListener(null)
        favorite?.isChecked = wallpaper.isInFavorites
        favorite?.invalidate()
        favorite?.setOnCheckedChangeListener { _, checked ->
            favorite.postDelayed(FAV_DELAY) { onFavClick(checked, wallpaper) }
        }
        title?.text = wallpaper.name
        author?.text = wallpaper.author
        image?.loadFramesPic(wallpaper.url, wallpaper.thumbnail) { crossfade(250) }
    }

    companion object {
        private const val FAV_DELAY = 150L
    }
}