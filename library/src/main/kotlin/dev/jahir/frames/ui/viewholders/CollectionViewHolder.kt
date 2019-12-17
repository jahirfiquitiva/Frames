package dev.jahir.frames.ui.viewholders

import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.recyclerview.widget.RecyclerView
import dev.jahir.frames.R
import dev.jahir.frames.data.models.Collection
import dev.jahir.frames.extensions.findView
import dev.jahir.frames.extensions.loadFramesPic

class CollectionViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    private val image: AppCompatImageView? by view.findView(R.id.wallpaper_image)
    private val title: TextView? by view.findView(R.id.collection_title)
    private val count: TextView? by view.findView(R.id.collection_count)

    fun bind(collection: Collection) {
        collection.cover?.let {
            image?.loadFramesPic(it.url, it.thumbnail) {
                crossfade(250)
            }
        }
        title?.text = collection.name
        count?.text = collection.count.toString()
    }
}