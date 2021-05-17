package dev.jahir.frames.ui.viewholders

import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import coil.annotation.ExperimentalCoilApi
import dev.jahir.frames.R
import dev.jahir.frames.data.models.Collection
import dev.jahir.frames.extensions.context.boolean
import dev.jahir.frames.extensions.context.string
import dev.jahir.frames.extensions.resources.withAlpha
import dev.jahir.frames.extensions.views.context
import dev.jahir.frames.extensions.views.findView
import dev.jahir.frames.extensions.views.loadFramesPicResPlaceholder

class CollectionViewHolder(view: View) : PaletteGeneratorViewHolder(view) {

    private val image: AppCompatImageView? by view.findView(R.id.wallpaper_image)
    private val title: TextView? by view.findView(R.id.collection_title)
    private val count: TextView? by view.findView(R.id.collection_count)
    private val detailsBackground: View? by view.findView(R.id.collection_details_background)

    @ExperimentalCoilApi
    fun bind(
        collection: Collection,
        onClick: ((collection: Collection) -> Unit)? = null
    ) {
        title?.text = collection.displayName
        count?.text = collection.count.toString()
        itemView.setOnClickListener { onClick?.invoke(collection) }
        collection.cover?.let {
            image?.loadFramesPicResPlaceholder(
                it.url,
                it.thumbnail,
                context.string(R.string.collections_placeholder),
                onImageLoaded = generatePalette
            )
        }
    }

    override fun doWithColors(bgColor: Int, textColor: Int) {
        detailsBackground?.setBackgroundColor(
            bgColor.withAlpha(
                if (context.boolean(R.bool.enable_filled_collection_preview))
                    FILLED_COLORED_TILES_ALPHA
                else WallpaperViewHolder.COLORED_TILES_ALPHA
            )
        )
        title?.setTextColor(textColor)
        count?.setTextColor(textColor)
    }

    companion object {
        private const val FILLED_COLORED_TILES_ALPHA =
            WallpaperViewHolder.COLORED_TILES_ALPHA - .15F
    }
}
