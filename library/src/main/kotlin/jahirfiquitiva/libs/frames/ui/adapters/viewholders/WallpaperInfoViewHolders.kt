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
package jahirfiquitiva.libs.frames.ui.adapters.viewholders

import android.view.View
import android.widget.TextView
import androidx.annotation.ColorInt
import ca.allanwang.kau.utils.tint
import ca.allanwang.kau.utils.toHexString
import com.afollestad.sectionedrecyclerview.SectionedViewHolder
import com.jahirfiquitiva.chip.ChipView
import jahirfiquitiva.libs.frames.R
import jahirfiquitiva.libs.frames.data.models.Collection
import jahirfiquitiva.libs.kext.extensions.bind
import jahirfiquitiva.libs.kext.extensions.drawable
import jahirfiquitiva.libs.kext.extensions.getActiveIconsColorFor
import jahirfiquitiva.libs.kext.extensions.getPrimaryTextColorFor
import jahirfiquitiva.libs.kext.extensions.secondaryTextColor
import jahirfiquitiva.libs.kext.ui.widgets.MaterialIconView

class WallpaperInfoHolder(itemView: View) : SectionedViewHolder(itemView) {
    val icon: MaterialIconView? by bind(R.id.info_item_icon)
    val content: TextView? by bind(R.id.info_item_text)
    fun bind(detail: WallpaperDetail) = with(itemView) {
        icon?.setImageDrawable(context.drawable(detail.icon))
        content?.setTextColor(context.secondaryTextColor)
        content?.text = detail.value
    }
}

class WallpaperPaletteHolder(itemView: View) : SectionedViewHolder(itemView) {
    private val chip: ChipView? by bind(R.id.info_palette_color)
    
    fun bindChip(
        @ColorInt
        color: Int, listener: (forCollection: Boolean, color: Int) -> Unit = { _, _ -> }
                ) = with(itemView) {
        chip?.setBackgroundColor(color)
        chip?.setTextColor(context.getPrimaryTextColorFor(color))
        chip?.text = color.toHexString()
        val icon = context.drawable("ic_color_palette")?.tint(context.getActiveIconsColorFor(color))
        chip?.setIcon(icon)
        chip?.setOnClickListener { listener(false, color) }
    }
    
    fun bindChip(
        collection: Collection,
        @ColorInt chipColor: Int,
        listener: (forCollection: Boolean, index: Int) -> Unit = { _, _ -> }
                ) =
        with(itemView) {
            chip?.setBackgroundColor(chipColor)
            chip?.setTextColor(context.getPrimaryTextColorFor(chipColor))
            chip?.text = collection.name
            val icon =
                context.drawable("ic_collections")?.tint(context.getActiveIconsColorFor(chipColor))
            chip?.setIcon(icon)
            chip?.setOnClickListener { listener(true, relativePosition.relativePos()) }
        }
}

data class WallpaperDetail(val icon: String, val value: String) {
    override fun equals(other: Any?): Boolean =
        other is WallpaperDetail && icon.equals(other.icon, true)
    
    override fun hashCode(): Int {
        var result = icon.hashCode()
        result = 31 * result + value.hashCode()
        return result
    }
}
