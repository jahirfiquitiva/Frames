package dev.jahir.frames.ui.adapters

import android.view.ViewGroup
import com.afollestad.sectionedrecyclerview.SectionedRecyclerViewAdapter
import com.afollestad.sectionedrecyclerview.SectionedViewHolder
import dev.jahir.frames.R
import dev.jahir.frames.data.models.AboutItem
import dev.jahir.frames.extensions.views.inflate
import dev.jahir.frames.ui.viewholders.AboutViewHolder
import dev.jahir.frames.ui.viewholders.SectionHeaderViewHolder

class AboutAdapter(
    private val designerAboutItems: ArrayList<AboutItem>,
    private val internalAboutItems: ArrayList<AboutItem>
) : SectionedRecyclerViewAdapter<SectionedViewHolder>() {

    init {
        shouldShowHeadersForEmptySections(false)
        shouldShowFooters(false)
    }

    override fun getItemCount(section: Int): Int = when (section) {
        0 -> designerAboutItems.size
        1 -> internalAboutItems.size
        else -> 0
    }

    override fun getItemViewType(section: Int, relativePosition: Int, absolutePosition: Int): Int =
        section

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SectionedViewHolder =
        when (viewType) {
            0, 1 -> AboutViewHolder(parent.inflate(R.layout.item_about))
            else -> SectionHeaderViewHolder(parent.inflate(R.layout.item_section_header))
        }

    override fun onBindHeaderViewHolder(
        holder: SectionedViewHolder?,
        section: Int,
        expanded: Boolean
    ) {
        val titleRes = when (section) {
            0 -> R.string.app_name
            1 -> R.string.dashboard
            else -> 0
        }
        (holder as? SectionHeaderViewHolder)?.bind(titleRes, 0, section > 0 && getItemCount(0) > 0)
    }

    override fun onBindViewHolder(
        holder: SectionedViewHolder?,
        section: Int,
        relativePosition: Int,
        absolutePosition: Int
    ) {
        if (section >= 2) return
        (holder as? AboutViewHolder)?.bind(
            if (section == 0) designerAboutItems.getOrNull(relativePosition)
            else internalAboutItems.getOrNull(relativePosition)
        )
    }

    override fun onBindFooterViewHolder(holder: SectionedViewHolder?, section: Int) {}
    override fun getSectionCount(): Int = 2
}