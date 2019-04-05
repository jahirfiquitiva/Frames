/*
 * Copyright (c) 2019. Jahir Fiquitiva
 *
 * Licensed under the CreativeCommons Attribution-ShareAlike
 * 4.0 International License. You may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *    http://creativecommons.org/licenses/by-sa/4.0/legalcode
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jahirfiquitiva.libs.frames.ui.adapters

import android.view.ViewGroup
import androidx.palette.graphics.Palette
import ca.allanwang.kau.utils.inflate
import com.afollestad.sectionedrecyclerview.SectionedRecyclerViewAdapter
import com.afollestad.sectionedrecyclerview.SectionedViewHolder
import jahirfiquitiva.libs.frames.R
import jahirfiquitiva.libs.frames.data.models.Collection
import jahirfiquitiva.libs.frames.helpers.extensions.jfilter
import jahirfiquitiva.libs.frames.ui.adapters.viewholders.SectionedHeaderViewHolder
import jahirfiquitiva.libs.frames.ui.adapters.viewholders.WallpaperDetail
import jahirfiquitiva.libs.frames.ui.adapters.viewholders.WallpaperInfoHolder
import jahirfiquitiva.libs.frames.ui.adapters.viewholders.WallpaperPaletteHolder
import jahirfiquitiva.libs.kext.extensions.hasContent

class WallpaperInfoAdapter(private val listener: (forCollection: Boolean, color: Int) -> Unit = { _, _ -> }) :
    SectionedRecyclerViewAdapter<SectionedViewHolder>() {
    
    private val collections = ArrayList<Collection>()
    private val details = ArrayList<WallpaperDetail>()
    private val colors = ArrayList<Int>()
    
    fun setDetails(
        collections: ArrayList<Collection>,
        details: ArrayList<WallpaperDetail>,
        palette: Palette?
                  ) {
        if (collections.size > 0) {
            this.collections.clear()
            this.collections.addAll(collections)
        }
        if (details.size > 0) {
            this.details.clear()
            this.details.addAll(details.jfilter { it.value.hasContent() })
        }
        colors.clear()
        palette?.let {
            with(it) {
                dominantSwatch?.let { addToColors(it.rgb) }
                vibrantSwatch?.let { addToColors(it.rgb) }
                lightVibrantSwatch?.let { addToColors(it.rgb) }
                darkVibrantSwatch?.let { addToColors(it.rgb) }
                mutedSwatch?.let { addToColors(it.rgb) }
                lightMutedSwatch?.let { addToColors(it.rgb) }
                darkMutedSwatch?.let { addToColors(it.rgb) }
            }
        }
        notifyDataSetChanged()
    }
    
    private fun addToColors(color: Int) {
        if (colors.contains(color)) return
        colors.add(color)
    }
    
    override fun getSectionCount(): Int = if (colors.size > 0) 3 else 2
    
    override fun getItemCount(section: Int): Int = when (section) {
        2 -> collections.size
        1 -> colors.size
        0 -> details.size
        else -> 0
    }
    
    override fun getItemViewType(
        section: Int, relativePosition: Int,
        absolutePosition: Int
                                ): Int = section
    
    override fun onBindViewHolder(
        holder: SectionedViewHolder?, section: Int, relativePosition: Int,
        absolutePosition: Int
                                 ) {
        holder?.let {
            (it as? WallpaperInfoHolder)?.bind(details[relativePosition])
                ?: (it as? WallpaperPaletteHolder)?.let {
                    var chipColorIndex = relativePosition
                    if (chipColorIndex < 0) chipColorIndex = 0
                    if (chipColorIndex > colors.size - 1) chipColorIndex = colors.size - 1
                    if (section == 2) {
                        chipColorIndex = colors.size - chipColorIndex - 1
                        it.bindChip(collections[relativePosition], colors[chipColorIndex], listener)
                    } else {
                        it.bindChip(colors[chipColorIndex], listener)
                    }
                }
        }
    }
    
    override fun onBindHeaderViewHolder(
        holder: SectionedViewHolder?, section: Int,
        expanded: Boolean
                                       ) {
        (holder as? SectionedHeaderViewHolder)?.setTitle(
            when (section) {
                0 -> R.string.wallpaper_details
                1 -> R.string.wallpaper_palette
                2 -> R.string.collections
                else -> 0
            }, shouldShowDivider = false, shouldShowIcon = false)
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SectionedViewHolder =
        when (viewType) {
            0 -> WallpaperInfoHolder(parent.inflate(R.layout.info_item))
            1, 2 -> WallpaperPaletteHolder(parent.inflate(R.layout.info_color))
            else -> SectionedHeaderViewHolder(parent.inflate(R.layout.item_section_header))
        }
    
    override fun onBindFooterViewHolder(holder: SectionedViewHolder?, section: Int) {}
}
