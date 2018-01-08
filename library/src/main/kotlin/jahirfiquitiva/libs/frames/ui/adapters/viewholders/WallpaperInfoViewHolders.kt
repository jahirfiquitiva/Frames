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
package jahirfiquitiva.libs.frames.ui.adapters.viewholders

import android.support.annotation.ColorInt
import android.view.View
import android.widget.TextView
import ca.allanwang.kau.utils.toHexString
import com.afollestad.sectionedrecyclerview.SectionedViewHolder
import com.pchmn.materialchips.ChipView
import jahirfiquitiva.libs.frames.R
import jahirfiquitiva.libs.kauextensions.extensions.applyColorFilter
import jahirfiquitiva.libs.kauextensions.extensions.bind
import jahirfiquitiva.libs.kauextensions.extensions.getActiveIconsColorFor
import jahirfiquitiva.libs.kauextensions.extensions.getDrawable
import jahirfiquitiva.libs.kauextensions.extensions.getPrimaryTextColorFor
import jahirfiquitiva.libs.kauextensions.extensions.secondaryTextColor
import jahirfiquitiva.libs.kauextensions.ui.widgets.MaterialIcon

class WallpaperInfoHolder(itemView: View) : SectionedViewHolder(itemView) {
    val icon: MaterialIcon by itemView.bind(R.id.info_item_icon)
    val content: TextView by itemView.bind(R.id.info_item_text)
    fun bind(detail: WallpaperDetail) = with(itemView) {
        icon.setImageDrawable(detail.icon.getDrawable(context))
        content.setTextColor(context.secondaryTextColor)
        content.text = detail.value
    }
}

class WallpaperPaletteHolder(itemView: View) : SectionedViewHolder(itemView) {
    private val chip: ChipView by itemView.bind(R.id.info_palette_color)
    fun bind(@ColorInt color: Int, colorListener: (Int) -> Unit = {}) = with(itemView) {
        chip.setBackgroundColor(color)
        chip.setLabelColor(context.getPrimaryTextColorFor(color, 0.6F))
        chip.label = color.toHexString()
        chip.setAvatarIcon(
                "ic_color_palette".getDrawable(context)?.applyColorFilter(
                        context.getActiveIconsColorFor(color, 0.6F)))
        chip.setOnChipClicked { colorListener(color) }
    }
}

data class WallpaperDetail(val icon: String, val value: String) {
    override fun equals(other: Any?): Boolean =
            (other is WallpaperDetail) && (icon.equals(other.icon, true))
    
    override fun hashCode(): Int {
        var result = icon.hashCode()
        result = 31 * result + value.hashCode()
        return result
    }
}