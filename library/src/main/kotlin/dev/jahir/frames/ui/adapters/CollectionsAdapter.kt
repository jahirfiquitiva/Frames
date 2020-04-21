package dev.jahir.frames.ui.adapters

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import dev.jahir.frames.R
import dev.jahir.frames.data.models.Collection
import dev.jahir.frames.extensions.context.boolean
import dev.jahir.frames.extensions.views.inflate
import dev.jahir.frames.ui.viewholders.CollectionViewHolder

class CollectionsAdapter(private val onClick: ((collection: Collection) -> Unit)? = null) :
    ListAdapter<Collection, CollectionViewHolder>(DIFFER) {

    override fun onBindViewHolder(holder: CollectionViewHolder, position: Int) {
        holder.bind(getItem(position), onClick)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CollectionViewHolder {
        val shouldBeFilled = parent.context.boolean(R.bool.enable_filled_collection_preview)
        return CollectionViewHolder(
            parent.inflate(
                if (shouldBeFilled) R.layout.item_collection_filled
                else R.layout.item_collection
            )
        )
    }

    companion object {
        private val DIFFER = object : DiffUtil.ItemCallback<Collection>() {
            override fun areItemsTheSame(oldItem: Collection, newItem: Collection): Boolean {
                return oldItem.name == newItem.name
            }

            override fun areContentsTheSame(oldItem: Collection, newItem: Collection): Boolean {
                return oldItem == newItem
            }
        }
    }
}