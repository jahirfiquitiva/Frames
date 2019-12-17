package dev.jahir.frames.ui.adapters

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import dev.jahir.frames.R
import dev.jahir.frames.data.models.Collection
import dev.jahir.frames.ui.viewholders.CollectionViewHolder
import dev.jahir.frames.extensions.inflate

class CollectionsAdapter : RecyclerView.Adapter<CollectionViewHolder>() {

    var collections: ArrayList<Collection> = ArrayList()
        set(value) {
            collections.clear()
            collections.addAll(value)
            notifyDataSetChanged()
        }

    override fun onBindViewHolder(holder: CollectionViewHolder, position: Int) {
        holder.bind(collections[position])
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CollectionViewHolder =
        CollectionViewHolder(parent.inflate(R.layout.item_collection))

    override fun getItemCount(): Int = collections.size
}