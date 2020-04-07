package dev.jahir.frames.ui.adapters

import android.view.ViewGroup
import androidx.palette.graphics.Palette
import com.afollestad.sectionedrecyclerview.SectionedRecyclerViewAdapter
import com.afollestad.sectionedrecyclerview.SectionedViewHolder
import dev.jahir.frames.R
import dev.jahir.frames.data.models.Wallpaper
import dev.jahir.frames.extensions.utils.MAX_FRAMES_PALETTE_COLORS
import dev.jahir.frames.extensions.views.inflate
import dev.jahir.frames.ui.viewholders.SectionHeaderViewHolder
import dev.jahir.frames.ui.viewholders.WallpaperDetailViewHolder
import dev.jahir.frames.ui.viewholders.WallpaperPaletteColorViewHolder
import kotlin.math.roundToInt

class WallpaperDetailsAdapter(
    var wallpaper: Wallpaper?,
    private val shouldShowPaletteDetails: Boolean = true
) : SectionedRecyclerViewAdapter<SectionedViewHolder>() {

    var paletteSwatches: ArrayList<Palette.Swatch> = ArrayList()
        set(value) {
            field.clear()
            if (shouldShowPaletteDetails)
                field.addAll(value)
            notifyDataSetChanged()
        }

    init {
        shouldShowHeadersForEmptySections(false)
        shouldShowFooters(false)
    }

    override fun getItemCount(section: Int): Int = when (section) {
        0 -> wallpaper?.detailsCount ?: 0
        1 -> paletteSwatches.size
        else -> 0
    }

    override fun getItemViewType(section: Int, relativePosition: Int, absolutePosition: Int): Int =
        section

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SectionedViewHolder =
        when (viewType) {
            0 -> WallpaperDetailViewHolder(parent.inflate(R.layout.item_wallpaper_detail))
            1 -> WallpaperPaletteColorViewHolder(parent.inflate(R.layout.item_wallpaper_palette_color))
            else -> SectionHeaderViewHolder(parent.inflate(R.layout.item_wallpaper_details_header))
        }

    override fun onBindHeaderViewHolder(
        holder: SectionedViewHolder?,
        section: Int,
        expanded: Boolean
    ) {
        val titleRes = when (section) {
            0 -> R.string.details
            1 -> R.string.palette
            else -> 0
        }
        val subtitleRes = when (section) {
            1 -> R.string.tap_to_copy
            else -> 0
        }
        (holder as? SectionHeaderViewHolder)?.bind(titleRes, subtitleRes, section > 0)
    }

    override fun onBindViewHolder(
        holder: SectionedViewHolder?,
        section: Int,
        relativePosition: Int,
        absolutePosition: Int
    ) {
        when (section) {
            0 -> {
                (holder as? WallpaperDetailViewHolder)?.bind(
                    wallpaper?.details?.getOrNull(relativePosition)
                )
            }
            1 -> {
                (holder as? WallpaperPaletteColorViewHolder)?.bind(
                    paletteSwatches.getOrNull(relativePosition)
                )
            }
        }
    }

    override fun onBindFooterViewHolder(holder: SectionedViewHolder?, section: Int) {}
    override fun getSectionCount(): Int = if (shouldShowPaletteDetails) 2 else 1

    override fun getRowSpan(
        fullSpanSize: Int,
        section: Int,
        relativePosition: Int,
        absolutePosition: Int
    ): Int = if (section == 1) 1 else (MAX_FRAMES_PALETTE_COLORS / 2.0).roundToInt()
}