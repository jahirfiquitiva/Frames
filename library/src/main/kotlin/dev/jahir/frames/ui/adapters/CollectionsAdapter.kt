package dev.jahir.frames.ui.adapters

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import dev.jahir.frames.R
import dev.jahir.frames.data.models.Collection
import dev.jahir.frames.extensions.boolean
import dev.jahir.frames.extensions.inflate
import dev.jahir.frames.ui.viewholders.CollectionViewHolder

class CollectionsAdapter(private val onClick: ((collection: Collection) -> Unit)? = null) :
    RecyclerView.Adapter<CollectionViewHolder>() {

    var collections: ArrayList<Collection> = ArrayList()
        set(value) {
            collections.clear()
            collections.addAll(value)
            notifyDataSetChanged()
        }

    override fun onBindViewHolder(holder: CollectionViewHolder, position: Int) {
        holder.bind(collections[position], onClick)
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

    override fun getItemCount(): Int = collections.size
}