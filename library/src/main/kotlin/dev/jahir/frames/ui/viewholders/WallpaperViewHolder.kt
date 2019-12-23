package dev.jahir.frames.ui.viewholders

import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.AppCompatCheckBox
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.view.ViewCompat
import androidx.core.view.postDelayed
import androidx.recyclerview.widget.RecyclerView
import dev.jahir.frames.R
import dev.jahir.frames.data.models.Wallpaper
import dev.jahir.frames.extensions.buildAuthorTransitionName
import dev.jahir.frames.extensions.buildImageTransitionName
import dev.jahir.frames.extensions.buildTitleTransitionName
import dev.jahir.frames.extensions.loadFramesPic

class WallpaperViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    internal val image: AppCompatImageView? = view.findViewById(R.id.wallpaper_image)
    internal val title: TextView? = view.findViewById(R.id.wallpaper_name)
    internal val author: TextView? = view.findViewById(R.id.wallpaper_author)
    internal val favorite: AppCompatCheckBox? = view.findViewById(R.id.fav_button)

    fun bind(
        wallpaper: Wallpaper,
        onClick: (Wallpaper, WallpaperViewHolder) -> Unit,
        onFavClick: (Boolean, Wallpaper) -> Unit
    ) {
        favorite?.setOnCheckedChangeListener(null)
        favorite?.isChecked = wallpaper.isInFavorites
        favorite?.invalidate()
        favorite?.setOnCheckedChangeListener { _, checked ->
            favorite.postDelayed(FAV_DELAY) { onFavClick(checked, wallpaper) }
        }

        title?.let {
            ViewCompat.setTransitionName(it, wallpaper.buildTitleTransitionName(adapterPosition))
        }
        author?.let {
            ViewCompat.setTransitionName(it, wallpaper.buildAuthorTransitionName(adapterPosition))
        }
        image?.let {
            ViewCompat.setTransitionName(it, wallpaper.buildImageTransitionName(adapterPosition))
        }

        title?.text = wallpaper.name
        author?.text = wallpaper.author
        image?.loadFramesPic(wallpaper.url, wallpaper.thumbnail)
        itemView.setOnClickListener { onClick(wallpaper, this) }
    }

    companion object {
        private const val FAV_DELAY = 100L
    }
}