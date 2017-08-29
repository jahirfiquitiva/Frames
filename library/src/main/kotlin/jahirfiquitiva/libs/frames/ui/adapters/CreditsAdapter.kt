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
package jahirfiquitiva.libs.frames.ui.adapters

import android.view.ViewGroup
import ca.allanwang.kau.utils.gone
import ca.allanwang.kau.utils.inflate
import com.afollestad.sectionedrecyclerview.SectionedRecyclerViewAdapter
import com.afollestad.sectionedrecyclerview.SectionedViewHolder
import com.bumptech.glide.RequestManager
import jahirfiquitiva.libs.frames.R
import jahirfiquitiva.libs.frames.ui.adapters.viewholders.BaseCreditViewHolder
import jahirfiquitiva.libs.frames.ui.adapters.viewholders.Credit
import jahirfiquitiva.libs.frames.ui.adapters.viewholders.CreditHeaderViewHolder
import jahirfiquitiva.libs.frames.ui.adapters.viewholders.DashboardCreditViewHolder
import jahirfiquitiva.libs.frames.ui.adapters.viewholders.SimpleCreditViewHolder

class CreditsAdapter(private val manager:RequestManager, private val credits:ArrayList<Credit>):
        SectionedRecyclerViewAdapter<SectionedViewHolder>() {
    
    init {
        shouldShowHeadersForEmptySections(true)
        shouldShowFooters(false)
    }
    
    override fun onViewRecycled(holder:SectionedViewHolder?) {
        super.onViewRecycled(holder)
        holder?.let {
            (it as? BaseCreditViewHolder)?.onRecycled()
        }
    }
    
    override fun getSectionCount():Int = 4
    
    override fun getItemViewType(section:Int, relativePosition:Int,
                                 absolutePosition:Int):Int = section
    
    override fun onBindViewHolder(holder:SectionedViewHolder?, section:Int, relativePosition:Int,
                                  absolutePosition:Int) {
        holder?.let {
            if (it is DashboardCreditViewHolder) {
                when (section) {
                    0 -> it.setItem(manager,
                                    credits.filter { it.type == Credit.Type.CREATOR }[relativePosition])
                    1 -> it.setItem(manager,
                                    credits.filter { it.type == Credit.Type.DASHBOARD }[relativePosition])
                    2 -> it.setItem(manager,
                                    credits.filter { it.type == Credit.Type.DEV_CONTRIBUTION }[relativePosition])
                    3 -> it.setItem(manager,
                                    credits.filter { it.type == Credit.Type.UI_CONTRIBUTION }[relativePosition])
                }
            }
        }
    }
    
    override fun getItemCount(section:Int):Int = when (section) {
        0 -> credits.filter { it.type == Credit.Type.CREATOR }.size
        1 -> credits.filter { it.type == Credit.Type.DASHBOARD }.size
        2 -> credits.filter { it.type == Credit.Type.DEV_CONTRIBUTION }.size
        3 -> credits.filter { it.type == Credit.Type.UI_CONTRIBUTION }.size
        else -> 0
    }
    
    override fun onBindHeaderViewHolder(holder:SectionedViewHolder?, section:Int,
                                        expanded:Boolean) {
        if (holder is CreditHeaderViewHolder) {
            when (section) {
                0 -> {
                    holder.setTitle(R.string.app_name, expanded)
                    holder.icon?.gone()
                }
                1 -> {
                    holder.setTitle(R.string.dashboard, expanded)
                    holder.icon?.gone()
                }
                2 -> holder.setTitle(R.string.dev_contributions, expanded,
                                     { toggleSectionExpanded(section) })
                3 -> holder.setTitle(R.string.ui_contributions, expanded,
                                     { toggleSectionExpanded(section) })
            }
        }
    }
    
    override fun onCreateViewHolder(parent:ViewGroup?, viewType:Int):SectionedViewHolder =
            when (viewType) {
                0, 1 -> DashboardCreditViewHolder(parent?.inflate(R.layout.item_credits))
                2, 3 -> SimpleCreditViewHolder(parent?.inflate(R.layout.item_credits))
                else -> CreditHeaderViewHolder(parent?.inflate(R.layout.item_section_header))
            }
    
    override fun onBindFooterViewHolder(holder:SectionedViewHolder?, section:Int) {
        return
    }
}