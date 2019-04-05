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
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import ca.allanwang.kau.utils.tint
import com.afollestad.sectionedrecyclerview.SectionedViewHolder
import jahirfiquitiva.libs.frames.R
import jahirfiquitiva.libs.kext.extensions.accentColor
import jahirfiquitiva.libs.kext.extensions.bind
import jahirfiquitiva.libs.kext.extensions.context
import jahirfiquitiva.libs.kext.extensions.drawable
import jahirfiquitiva.libs.kext.extensions.primaryTextColor
import jahirfiquitiva.libs.kext.extensions.string

class QuickActionsViewHolder(itemView: View) : SectionedViewHolder(itemView) {
    private val optionIcon: ImageView? by itemView.bind(R.id.option_icon)
    private val optionTitle: TextView? by itemView.bind(R.id.option_title)
    
    fun bind(@DrawableRes icon: Int, @StringRes title: Int, onClick: (index: Int) -> Unit = {}) {
        optionIcon?.setImageDrawable(context.drawable(icon)?.tint(context.accentColor))
        optionTitle?.text = context.string(title)
        optionTitle?.setTextColor(context.primaryTextColor)
        itemView.setOnClickListener { onClick(relativePosition.relativePos()) }
    }
}
