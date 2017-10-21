/*
 * Copyright (c) 2017. Jahir Fiquitiva
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

import android.support.annotation.StringRes
import android.support.v7.widget.AppCompatButton
import android.util.TypedValue
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import ca.allanwang.kau.utils.gone
import ca.allanwang.kau.utils.tint
import ca.allanwang.kau.utils.visible
import com.afollestad.sectionedrecyclerview.SectionedViewHolder
import com.bumptech.glide.RequestManager
import jahirfiquitiva.libs.frames.R
import jahirfiquitiva.libs.frames.helpers.extensions.loadAvatar
import jahirfiquitiva.libs.frames.helpers.extensions.releaseFromGlide
import jahirfiquitiva.libs.kauextensions.extensions.accentColor
import jahirfiquitiva.libs.kauextensions.extensions.activeIconsColor
import jahirfiquitiva.libs.kauextensions.extensions.bind
import jahirfiquitiva.libs.kauextensions.extensions.dividerColor
import jahirfiquitiva.libs.kauextensions.extensions.hasContent
import jahirfiquitiva.libs.kauextensions.extensions.openLink
import jahirfiquitiva.libs.kauextensions.extensions.primaryTextColor
import jahirfiquitiva.libs.kauextensions.extensions.secondaryTextColor
import jahirfiquitiva.libs.kauextensions.ui.layouts.SplitButtonsLayout

@Suppress("ArrayInDataClass")
data class Credit(val name: String, val photo: String, val type: Type,
                  val link: String = "", val description: String = "",
                  val buttonsTitles: List<String> = ArrayList(),
                  val buttonsLinks: List<String> = ArrayList()) {
    
    enum class Type {
        CREATOR, DASHBOARD, DEV_CONTRIBUTION, UI_CONTRIBUTION
    }
    
    companion object {
        private val JAMES = Credit("James Fenn", "https://goo.gl/6Wc5rK", Type.DEV_CONTRIBUTION,
                                   "https://plus.google.com/+JamesFennJAFFA2157")
        private val MAX = Credit("Maximilian Keppeler", "https://goo.gl/2qUEtS",
                                 Type.DEV_CONTRIBUTION,
                                 "https://plus.google.com/+MaxKeppeler")
        private val SASI = Credit("Sasi Kanth", "https://goo.gl/wvxim8", Type.DEV_CONTRIBUTION,
                                  "https://plus.google.com/+Sasikanth")
        private val ALEX = Credit("Alexandre Piveteau", "https://goo.gl/ZkJNnV",
                                  Type.DEV_CONTRIBUTION,
                                  "https://github.com/alexandrepiveteau")
        private val LUKAS = Credit("Lukas Koller", "https://goo.gl/aPtAKZ", Type.DEV_CONTRIBUTION,
                                   "https://github.com/kollerlukas")
        
        private val PATRYK = Credit("Patryk Goworowski", "https://goo.gl/9ccZcA",
                                    Type.UI_CONTRIBUTION,
                                    "https://plus.google.com/+PatrykGoworowski")
        private val LUMIQ = Credit("Lumiq Creative", "https://goo.gl/UVRC7G", Type.UI_CONTRIBUTION,
                                   "https://plus.google.com/+LumiqCreative")
        private val KEVIN = Credit("Kevin Aguilar", "https://goo.gl/mGuAM9", Type.UI_CONTRIBUTION,
                                   "http://kevaguilar.com/")
        private val EDUARDO = Credit("Eduardo Pratti", "https://goo.gl/TSKB7s",
                                     Type.UI_CONTRIBUTION, "https://plus.google.com/+EduardoPratti")
        private val ANTHONY = Credit("Anthony Nguyen", "https://goo.gl/zxiBQE",
                                     Type.UI_CONTRIBUTION, "https://plus.google.com/+AHNguyen")
        
        val EXTRA_CREDITS = arrayListOf(JAMES, MAX, SASI, ALEX, LUKAS,
                                        PATRYK, LUMIQ, KEVIN, EDUARDO, ANTHONY)
    }
}

const val SECTION_ICON_ANIMATION_DURATION: Long = 250

class SectionedHeaderViewHolder(itemView: View) : SectionedViewHolder(itemView) {
    val divider: View by itemView.bind(R.id.section_divider)
    val title: TextView by itemView.bind(R.id.section_title)
    val icon: ImageView by itemView.bind(R.id.section_icon)
    
    fun setTitle(@StringRes text: Int, shouldShowIcon: Boolean = false, expanded: Boolean = true,
                 listener: () -> Unit = {}) {
        setTitle(itemView.context.getString(text), shouldShowIcon, expanded, listener)
    }
    
    fun setTitle(text: String, shouldShowIcon: Boolean = false, expanded: Boolean = true,
                 listener: () -> Unit = {}) {
        divider.setBackgroundColor(itemView.context.dividerColor)
        title.setTextColor(itemView.context.secondaryTextColor)
        title.text = text
        if (shouldShowIcon) {
            icon.drawable?.tint(itemView.context.activeIconsColor)
            icon.visible()
            icon.animate()?.rotation(if (expanded) 180F else 0F)?.setDuration(
                    SECTION_ICON_ANIMATION_DURATION)?.start()
        } else icon.gone()
        itemView?.setOnClickListener { listener() }
    }
}

open class DashboardCreditViewHolder(itemView: View) : GlideSectionedViewHolder(itemView) {
    val photo: ImageView by itemView.bind(R.id.photo)
    val name: TextView by itemView.bind(R.id.name)
    private val description: TextView by itemView.bind(R.id.description)
    private val buttons: SplitButtonsLayout by itemView.bind(R.id.buttons)
    
    open fun setItem(manager: RequestManager, credit: Credit, fillAvailableSpace: Boolean = true,
                     shouldHideButtons: Boolean = false) {
        photo.loadAvatar(manager, credit.photo, false)
        name.setTextColor(itemView.context.primaryTextColor)
        name.text = credit.name
        if (credit.description.hasContent()) {
            description.setTextColor(itemView.context.secondaryTextColor)
            description.text = credit.description
        } else {
            description.gone()
        }
        if (shouldHideButtons || credit.buttonsTitles.isEmpty()) {
            buttons.gone()
            if (credit.link.hasContent()) {
                itemView?.setOnClickListener { view -> view.context.openLink(credit.link) }
                try {
                    val outValue = TypedValue()
                    itemView.context.theme.resolveAttribute(
                            android.R.attr.selectableItemBackground, outValue, true)
                    itemView?.setBackgroundResource(outValue.resourceId)
                } catch (ignored: Exception) {
                }
            }
        } else {
            if (credit.buttonsTitles.size == credit.buttonsLinks.size) {
                buttons.buttonCount = credit.buttonsTitles.size
                for (index in 0 until credit.buttonsTitles.size) {
                    val hasThemAll = buttons.hasAllButtons() != false
                    if (!hasThemAll) {
                        buttons.addButton(credit.buttonsTitles[index], credit.buttonsLinks[index],
                                          fillAvailableSpace)
                        val btn = buttons.getChildAt(index)
                        btn?.let {
                            it.setOnClickListener { view ->
                                if (view.tag is String) {
                                    view.context.openLink(view.tag as String)
                                }
                            }
                            (it as? AppCompatButton)?.setTextColor(it.context.accentColor)
                        }
                    }
                }
            } else {
                buttons.gone()
            }
        }
    }
    
    override fun doOnRecycle() {
        photo.releaseFromGlide()
    }
}

class SimpleCreditViewHolder(itemView: View) : DashboardCreditViewHolder(itemView) {
    override fun setItem(manager: RequestManager, credit: Credit, fillAvailableSpace: Boolean,
                         shouldHideButtons: Boolean) {
        super.setItem(manager, credit, false, true)
    }
}