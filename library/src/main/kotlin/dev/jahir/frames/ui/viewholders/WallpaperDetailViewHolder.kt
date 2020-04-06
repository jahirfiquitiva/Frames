package dev.jahir.frames.ui.viewholders

import android.view.View
import android.widget.TextView
import androidx.annotation.StringRes
import com.afollestad.sectionedrecyclerview.SectionedViewHolder
import dev.jahir.frames.R
import dev.jahir.frames.extensions.findView
import dev.jahir.frames.extensions.string

class WallpaperDetailViewHolder(view: View) : SectionedViewHolder(view) {

    private val titleTextView: TextView? by view.findView(R.id.detail_title)
    private val descriptionTextView: TextView? by view.findView(R.id.detail_description)

    fun bind(pair: Pair<Int, String>?) {
        pair ?: return
        bind(pair.first, pair.second)
    }

    fun bind(@StringRes title: Int, description: String) {
        try {
            bind(itemView.context.string(title), description)
        } catch (e: Exception) {
        }
    }

    fun bind(title: String, description: String) {
        titleTextView?.text = title
        descriptionTextView?.text = description
    }
}