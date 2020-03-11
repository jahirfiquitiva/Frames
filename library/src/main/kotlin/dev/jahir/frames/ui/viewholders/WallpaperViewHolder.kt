package dev.jahir.frames.ui.viewholders

import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.view.ViewCompat
import androidx.core.view.postDelayed
import androidx.core.widget.CompoundButtonCompat
import androidx.palette.graphics.Palette
import dev.jahir.frames.R
import dev.jahir.frames.data.models.Wallpaper
import dev.jahir.frames.extensions.bestTextColor
import dev.jahir.frames.extensions.buildAuthorTransitionName
import dev.jahir.frames.extensions.buildImageTransitionName
import dev.jahir.frames.extensions.buildTitleTransitionName
import dev.jahir.frames.extensions.context
import dev.jahir.frames.extensions.findView
import dev.jahir.frames.extensions.loadFramesPic
import dev.jahir.frames.extensions.withAlpha
import dev.jahir.frames.ui.widgets.FavoriteCheckbox
import dev.jahir.frames.utils.tint

class WallpaperViewHolder(view: View) : PaletteGeneratorViewHolder(view) {
    internal val image: AppCompatImageView? = view.findViewById(R.id.wallpaper_image)
    internal val title: TextView? = view.findViewById(R.id.wallpaper_name)
    internal val author: TextView? = view.findViewById(R.id.wallpaper_author)
    internal val favorite: FavoriteCheckbox? = view.findViewById(R.id.fav_button)
    private val detailsBackground: View? by view.findView(R.id.wallpaper_details_background)

    fun bind(
        wallpaper: Wallpaper,
        canModifyFavorites: Boolean,
        onClick: (Wallpaper, WallpaperViewHolder) -> Unit,
        onFavClick: (Boolean, Wallpaper) -> Unit
    ) {
        favorite?.setOnCheckedChangeListener(null)
        favorite?.isChecked = wallpaper.isInFavorites
        favorite?.invalidate()
        favorite?.canCheck = canModifyFavorites
        favorite?.setOnClickListener { view ->
            view.postDelayed(FAV_DELAY) {
                onFavClick(
                    (view as? FavoriteCheckbox)?.isChecked ?: wallpaper.isInFavorites,
                    wallpaper
                )
            }
        }
        favorite?.onDisabledClickListener = { onFavClick(wallpaper.isInFavorites, wallpaper) }

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
        itemView.setOnClickListener { onClick(wallpaper, this) }
        image?.loadFramesPic(
            wallpaper.url,
            wallpaper.thumbnail,
            context.getString(R.string.wallpapers_placeholder),
            doWithPalette = if (shouldColorTiles) generatePalette else null
        )
    }

    override fun doWithBestSwatch(swatch: Palette.Swatch) {
        detailsBackground?.setBackgroundColor(swatch.rgb.withAlpha(COLORED_TILES_ALPHA))
        val textColor = swatch.bestTextColor
        title?.setTextColor(textColor)
        author?.setTextColor(textColor)
        favorite?.let { favBtn ->
            favBtn.buttonDrawable = CompoundButtonCompat.getButtonDrawable(favBtn)?.tint(textColor)
        }
    }

    companion object {
        private const val FAV_DELAY = 100L
        internal const val COLORED_TILES_ALPHA = .9F
    }
}