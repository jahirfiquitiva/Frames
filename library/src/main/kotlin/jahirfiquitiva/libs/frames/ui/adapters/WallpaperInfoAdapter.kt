/*
 * Copyright (c) 2018. Jahir Fiquitiva
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

import android.support.v7.graphics.Palette
import android.view.ViewGroup
import ca.allanwang.kau.utils.inflate
import com.afollestad.sectionedrecyclerview.SectionedRecyclerViewAdapter
import com.afollestad.sectionedrecyclerview.SectionedViewHolder
import jahirfiquitiva.libs.frames.R
import jahirfiquitiva.libs.frames.ui.adapters.viewholders.SectionedHeaderViewHolder
import jahirfiquitiva.libs.frames.ui.adapters.viewholders.WallpaperDetail
import jahirfiquitiva.libs.frames.ui.adapters.viewholders.WallpaperInfoHolder
import jahirfiquitiva.libs.frames.ui.adapters.viewholders.WallpaperPaletteHolder
import jahirfiquitiva.libs.kauextensions.extensions.hasContent

class WallpaperInfoAdapter(private val colorListener: (Int) -> Unit) :
        SectionedRecyclerViewAdapter<SectionedViewHolder>() {
    
    private val details = ArrayList<WallpaperDetail>()
    private val colors = ArrayList<Int>()
    
    fun setDetailsAndPalette(details: ArrayList<WallpaperDetail>, palette: Palette?) {
        if (details.size > 0) {
            this.details.clear()
            details.filter { it.value.hasContent() }.forEach { this.details.add(it) }
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
    
    override fun getSectionCount(): Int = if (colors.size > 0) 2 else 1
    
    override fun getItemCount(section: Int): Int = when (section) {
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
                    ?: (it as? WallpaperPaletteHolder)?.bind(
                            colors[relativePosition],
                            { colorListener(it) })
        }
    }
    
    override fun onBindHeaderViewHolder(
            holder: SectionedViewHolder?, section: Int,
            expanded: Boolean
                                       ) {
        (holder as? SectionedHeaderViewHolder)?.setTitle(
                if (section == 0) R.string.wallpaper_details else R.string.wallpaper_palette,
                false, false)
    }
    
    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): SectionedViewHolder? =
            when (viewType) {
                0 -> parent?.inflate(R.layout.info_item)?.let { WallpaperInfoHolder(it) }
                1 -> parent?.inflate(R.layout.info_color)?.let { WallpaperPaletteHolder(it) }
                else -> parent?.inflate(R.layout.item_section_header)?.let {
                    SectionedHeaderViewHolder(it)
                }
            }
    
    override fun onBindFooterViewHolder(holder: SectionedViewHolder?, section: Int) {}
}