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

import android.util.TypedValue
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.appcompat.widget.AppCompatButton
import ca.allanwang.kau.utils.gone
import ca.allanwang.kau.utils.openLink
import ca.allanwang.kau.utils.tint
import ca.allanwang.kau.utils.visible
import com.afollestad.sectionedrecyclerview.SectionedViewHolder
import com.bumptech.glide.RequestManager
import jahirfiquitiva.libs.frames.R
import jahirfiquitiva.libs.frames.helpers.glide.loadPicture
import jahirfiquitiva.libs.kext.extensions.accentColor
import jahirfiquitiva.libs.kext.extensions.activeIconsColor
import jahirfiquitiva.libs.kext.extensions.bind
import jahirfiquitiva.libs.kext.extensions.context
import jahirfiquitiva.libs.kext.extensions.dividerColor
import jahirfiquitiva.libs.kext.extensions.hasContent
import jahirfiquitiva.libs.kext.extensions.primaryTextColor
import jahirfiquitiva.libs.kext.extensions.secondaryTextColor
import jahirfiquitiva.libs.kext.extensions.string
import jahirfiquitiva.libs.kext.ui.layouts.SplitButtonsLayout

@Suppress("ArrayInDataClass")
data class Credit(
    val name: String, val photo: String, val type: Type,
    val link: String = "", val description: String = "",
    val buttonsTitles: List<String> = ArrayList(),
    val buttonsLinks: List<String> = ArrayList()
                 ) {
    
    enum class Type {
        CREATOR, DASHBOARD, DEV_CONTRIBUTION, UI_CONTRIBUTION
    }
    
    companion object {
        private val JAMES = Credit(
            "James Fenn", "https://goo.gl/6Wc5rK", Type.DEV_CONTRIBUTION,
            "https://jfenn.me/")
        private val MAX = Credit(
            "Maximilian Keppeler", "https://goo.gl/2qUEtS",
            Type.DEV_CONTRIBUTION,
            "https://twitter.com/maxKeppeler")
        private val SASI = Credit(
            "Sasi Kanth", "https://goo.gl/wvxim8", Type.DEV_CONTRIBUTION,
            "https://twitter.com/its_sasikanth")
        private val ALEX = Credit(
            "Alexandre Piveteau", "https://goo.gl/ZkJNnV",
            Type.DEV_CONTRIBUTION,
            "https://github.com/alexandrepiveteau")
        private val LUKAS = Credit(
            "Lukas Koller", "https://goo.gl/aPtAKZ", Type.DEV_CONTRIBUTION,
            "https://github.com/kollerlukas")
        
        private val PATRYK = Credit(
            "Patryk Goworowski", "https://goo.gl/9ccZcA",
            Type.UI_CONTRIBUTION,
            "https://twitter.com/pgoworowski")
        private val LUMIQ = Credit(
            "Lumiq Creative",
            "https://raw.githubusercontent.com/lumiqcreative/resources/master/brand_resources/profile_picture.png",
            Type.UI_CONTRIBUTION, "https://lumiqcreative.com/")
        private val KEVIN = Credit(
            "Kevin Aguilar", "https://goo.gl/mGuAM9", Type.UI_CONTRIBUTION,
            "https://twitter.com/kevttob")
        private val EDUARDO = Credit(
            "Eduardo Pratti", "https://goo.gl/TSKB7s",
            Type.UI_CONTRIBUTION, "https://pratti.design/")
        private val ANTHONY = Credit(
            "Anthony Nguyen", "https://goo.gl/zxiBQE",
            Type.UI_CONTRIBUTION, "https://twitter.com/link6155")
        
        val EXTRA_CREDITS = arrayListOf(
            JAMES, MAX, SASI, ALEX, LUKAS,
            PATRYK, LUMIQ, KEVIN, EDUARDO, ANTHONY)
    }
}

const val SECTION_ICON_ANIMATION_DURATION: Long = 250

class SectionedHeaderViewHolder(itemView: View) : SectionedViewHolder(itemView) {
    private val container: LinearLayout? by bind(R.id.section_title_container)
    private val divider: View? by bind(R.id.section_divider)
    private val title: TextView? by bind(R.id.section_title)
    private val icon: ImageView? by bind(R.id.section_icon)
    
    fun setTitle(
        @StringRes text: Int,
        shouldShowDivider: Boolean = false,
        shouldShowIcon: Boolean = false,
        expanded: Boolean = true,
        listener: () -> Unit = {}
                ) {
        setTitle(string(text), shouldShowDivider, shouldShowIcon, expanded, listener)
    }
    
    @Suppress("MemberVisibilityCanBePrivate")
    fun setTitle(
        text: String,
        shouldShowDivider: Boolean = false,
        shouldShowIcon: Boolean = false,
        expanded: Boolean = true,
        listener: () -> Unit = {}
                ) {
        if (shouldShowDivider) {
            divider?.setBackgroundColor(context.dividerColor)
            divider?.visible()
        } else divider?.gone()
        
        if (text.hasContent()) {
            title?.setTextColor(context.secondaryTextColor)
            title?.text = text
            container?.visible()
        } else container?.gone()
        
        if (shouldShowIcon) {
            icon?.drawable?.tint(context.activeIconsColor)
            icon?.visible()
            icon?.animate()?.rotation(if (expanded) 180F else 0F)
                ?.setDuration(SECTION_ICON_ANIMATION_DURATION)?.start()
        } else icon?.gone()
        itemView.setOnClickListener { listener() }
    }
}

open class DashboardCreditViewHolder(itemView: View) : SectionedViewHolder(itemView) {
    private val photo: ImageView? by bind(R.id.photo)
    private val name: TextView? by bind(R.id.name)
    private val description: TextView? by bind(R.id.description)
    private val buttons: SplitButtonsLayout? by bind(R.id.buttons)
    
    open fun setItem(
        manager: RequestManager,
        credit: Credit,
        fillAvailableSpace: Boolean = true,
        shouldHideButtons: Boolean = false
                    ) {
        photo?.loadPicture(manager, credit.photo, circular = true)
        name?.setTextColor(context.primaryTextColor)
        name?.text = credit.name
        if (credit.description.hasContent()) {
            description?.setTextColor(context.secondaryTextColor)
            description?.text = credit.description
        } else {
            description?.gone()
        }
        if (shouldHideButtons || credit.buttonsTitles.isEmpty()) {
            buttons?.gone()
            if (credit.link.hasContent()) {
                itemView.setOnClickListener { view -> view.context.openLink(credit.link) }
                try {
                    val outValue = TypedValue()
                    context.theme.resolveAttribute(
                        android.R.attr.selectableItemBackground, outValue, true)
                    itemView.setBackgroundResource(outValue.resourceId)
                } catch (ignored: Exception) {
                }
            }
        } else {
            if (credit.buttonsTitles.size == credit.buttonsLinks.size) {
                buttons?.buttonCount = credit.buttonsTitles.size
                for (index in 0 until credit.buttonsTitles.size) {
                    if (buttons?.hasAllButtons() == false) {
                        buttons?.addButton(
                            credit.buttonsTitles[index], credit.buttonsLinks[index],
                            fillAvailableSpace)
                        val btn = buttons?.getChildAt(index)
                        btn?.let {
                            it.setOnClickListener { view ->
                                (view.tag as? String)?.let { view.context.openLink(it) }
                            }
                            (it as? AppCompatButton)?.setTextColor(it.context.accentColor)
                        }
                    }
                }
            } else {
                buttons?.gone()
            }
        }
    }
}

class SimpleCreditViewHolder(itemView: View) : DashboardCreditViewHolder(itemView) {
    override fun setItem(
        manager: RequestManager, credit: Credit, fillAvailableSpace: Boolean,
        shouldHideButtons: Boolean
                        ) {
        super.setItem(manager, credit, false, true)
    }
}
