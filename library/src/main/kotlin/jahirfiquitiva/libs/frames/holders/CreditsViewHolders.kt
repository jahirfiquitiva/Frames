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
package jahirfiquitiva.libs.frames.holders

import android.graphics.Bitmap
import android.support.annotation.StringRes
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory
import android.util.TypedValue
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import ca.allanwang.kau.utils.gone
import ca.allanwang.kau.utils.tint
import com.afollestad.sectionedrecyclerview.SectionedViewHolder
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.BitmapImageViewTarget
import jahirfiquitiva.libs.frames.R
import jahirfiquitiva.libs.frames.extensions.hasContent
import jahirfiquitiva.libs.kauextensions.extensions.activeIconsColor
import jahirfiquitiva.libs.kauextensions.extensions.openLink
import jahirfiquitiva.libs.kauextensions.extensions.primaryTextColor
import jahirfiquitiva.libs.kauextensions.extensions.secondaryTextColor
import jahirfiquitiva.libs.kauextensions.ui.layouts.SplitButtonsLayout


@Suppress("ArrayInDataClass")
data class Credit(val type:Type, val photo:String, val name:String, val description:String = "",
                  val buttonsTitles:List<String> = ArrayList<String>(),
                  val buttonsLinks:List<String> = ArrayList<String>(), val link:String = "") {
    enum class Type {
        CREATOR, DASHBOARD, DEV_CONTRIBUTION, UI_CONTRIBUTION
    }
}

class CreditHeaderViewHolder(itemView:View):SectionedViewHolder(itemView) {
    val divider:View = itemView.findViewById(R.id.section_divider)
    val title:TextView = itemView.findViewById(R.id.section_title)
    val icon:ImageView = itemView.findViewById(R.id.section_icon)

    fun setTitle(@StringRes text:Int, expanded:Boolean) {
        title.setTextColor(itemView.context.primaryTextColor)
        title.text = itemView.context.getString(text)
        icon.drawable.tint(itemView.context.activeIconsColor)
        if (expanded) icon.rotation = 180F else icon.rotation = 0F
    }
}

open class CreatorCreditViewHolder(itemView:View?):SectionedViewHolder(itemView) {
    val photo:ImageView? = itemView?.findViewById(R.id.photo)
    val name:TextView? = itemView?.findViewById(R.id.name)
    val description:TextView? = itemView?.findViewById(R.id.description)
    val buttons:SplitButtonsLayout? = itemView?.findViewById(R.id.buttons)

    open fun setItem(credit:Credit, fillAvailableSpace:Boolean = true,
                     shouldHideButtons:Boolean = false) {
        photo?.let {
            Glide.with(itemView.context).load(credit.photo).asBitmap().centerCrop().into(
                    object:BitmapImageViewTarget(it) {
                        override fun setResource(resource:Bitmap?) {
                            val roundedPic = RoundedBitmapDrawableFactory.create(
                                    itemView.context.resources, resource)
                            roundedPic.isCircular = true
                            super.setResource(roundedPic.bitmap)
                        }
                    })
        }
        name?.setTextColor(itemView.context.primaryTextColor)
        name?.text = credit.name
        if (credit.description.hasContent()) {
            description?.setTextColor(itemView.context.secondaryTextColor)
            description?.text = credit.description
        } else {
            description?.gone()
        }
        if (shouldHideButtons || credit.buttonsTitles.isEmpty()) {
            buttons?.gone()
        } else {
            if (credit.buttonsTitles.size == credit.buttonsLinks.size) {
                buttons?.buttonCount = credit.buttonsTitles.size
                for (index in 0 until credit.buttonsTitles.size) {
                    buttons?.addButton(credit.buttonsTitles[index], credit.buttonsLinks[index],
                                       fillAvailableSpace)
                    buttons?.getChildAt(index)?.setOnClickListener { view ->
                        if (view.tag is String) {
                            view.context.openLink(view.tag as String)
                        }
                    }
                }
            } else {
                buttons?.gone()
                if (credit.link.hasContent()) {
                    try {
                        val outValue = TypedValue()
                        itemView.context.theme.resolveAttribute(
                                android.R.attr.selectableItemBackground, outValue, true)
                        itemView?.setBackgroundResource(outValue.resourceId)
                    } catch (ignored:Exception) {
                    }
                    itemView?.setOnClickListener { it.context.openLink(credit.link) }
                }
            }
        }
    }
}

class DashboardCreditViewHolder(itemView:View?):CreatorCreditViewHolder(itemView) {
    override fun setItem(credit:Credit, fillAvailableSpace:Boolean, shouldHideButtons:Boolean) {
        super.setItem(credit, false, false)
    }
}

class SimpleCreditViewHolder(itemView:View?):CreatorCreditViewHolder(itemView) {
    override fun setItem(credit:Credit, fillAvailableSpace:Boolean, shouldHideButtons:Boolean) {
        super.setItem(credit, false, true)
    }
}