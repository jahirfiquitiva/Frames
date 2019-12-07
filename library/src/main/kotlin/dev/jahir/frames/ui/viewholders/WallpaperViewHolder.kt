package dev.jahir.frames.ui.viewholders

import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import dev.jahir.frames.R
import dev.jahir.frames.data.models.Wallpaper
import dev.jahir.frames.utils.extensions.context
import dev.jahir.frames.utils.extensions.loadFramesPic

class WallpaperViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    private val image: AppCompatImageView? = view.findViewById(R.id.wallpaper_image)
    private val title: TextView? = view.findViewById(R.id.wallpaper_name)
    private val author: TextView? = view.findViewById(R.id.wallpaper_author)
    private val favorite: AppCompatImageView? = view.findViewById(R.id.fav_button)

    fun bind(wallpaper: Wallpaper, onFavClick: (Boolean, Wallpaper) -> Unit) {
        image?.loadFramesPic(wallpaper.url, wallpaper.thumbnail) { crossfade(250) }
        title?.text = wallpaper.name
        author?.text = wallpaper.author
        favorite?.setImageDrawable(
            ContextCompat.getDrawable(
                context,
                if (wallpaper.isInFavorites) R.drawable.ic_favorite
                else R.drawable.ic_favorite_outline
            )
        )
        favorite?.setOnClickListener { onFavClick(!wallpaper.isInFavorites, wallpaper) }
    }
}