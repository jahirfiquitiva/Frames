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
package jahirfiquitiva.libs.frames.adapters

import android.view.ViewGroup
import ca.allanwang.kau.utils.gone
import ca.allanwang.kau.utils.inflate
import com.afollestad.sectionedrecyclerview.SectionedRecyclerViewAdapter
import com.afollestad.sectionedrecyclerview.SectionedViewHolder
import jahirfiquitiva.libs.frames.R
import jahirfiquitiva.libs.frames.holders.CreatorCreditViewHolder
import jahirfiquitiva.libs.frames.holders.Credit
import jahirfiquitiva.libs.frames.holders.CreditHeaderViewHolder
import jahirfiquitiva.libs.frames.holders.DashboardCreditViewHolder
import jahirfiquitiva.libs.frames.holders.SimpleCreditViewHolder

class CreditsAdapter(val credits:ArrayList<Credit>):
        SectionedRecyclerViewAdapter<SectionedViewHolder>() {

    private val CREATOR_CREDIT_VIEW_TYPE = 0
    private val DASHBOARD_CREDIT_VIEW_TYPE = 1
    private val DEV_CONTRIBUTION_CREDIT_VIEW_TYPE = 2
    private val UI_CONTRIBUTION_CREDIT_VIEW_TYPE = 3

    init {
        shouldShowFooters(false)
    }

    override fun getSectionCount():Int = 4

    override fun getItemViewType(section:Int, relativePosition:Int,
                                 absolutePosition:Int):Int = section

    override fun onBindViewHolder(holder:SectionedViewHolder?, section:Int, relativePosition:Int,
                                  absolutePosition:Int) {
        holder?.let {
            if (it is CreatorCreditViewHolder) {
                when (section) {
                    0 -> it.setItem(
                            credits.filter { it.type == Credit.Type.CREATOR }[relativePosition])
                    1 -> it.setItem(
                            credits.filter { it.type == Credit.Type.DASHBOARD }[relativePosition])
                    2 -> it.setItem(
                            credits.filter { it.type == Credit.Type.DEV_CONTRIBUTION }[relativePosition])
                    3 -> it.setItem(
                            credits.filter { it.type == Credit.Type.UI_CONTRIBUTION }[relativePosition])
                }
            }
        }
    }

    override fun getItemCount(section:Int):Int {
        when (section) {
            0 -> return credits.filter { it.type == Credit.Type.CREATOR }.size
            1 -> return credits.filter { it.type == Credit.Type.DASHBOARD }.size
            2 -> return credits.filter { it.type == Credit.Type.DEV_CONTRIBUTION }.size
            3 -> return credits.filter { it.type == Credit.Type.UI_CONTRIBUTION }.size
            else -> return 0
        }
    }

    override fun onBindHeaderViewHolder(holder:SectionedViewHolder?, section:Int,
                                        expanded:Boolean) {
        if (holder is CreditHeaderViewHolder) {
            if (section == 0) {
                holder.itemView.gone()
            } else {
                when (section) {
                    1 -> holder.setTitle(R.string.dashboard, expanded)
                    2 -> holder.setTitle(R.string.dev_contributions, expanded)
                    3 -> holder.setTitle(R.string.ui_contributions, expanded)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent:ViewGroup?, viewType:Int):SectionedViewHolder? {
        when (viewType) {
            0 -> return CreatorCreditViewHolder(parent?.inflate(R.layout.item_creator_credits))
            1 -> return DashboardCreditViewHolder(parent?.inflate(R.layout.item_dashboard_credits))
            2, 3 -> return SimpleCreditViewHolder(parent?.inflate(R.layout.item_dashboard_credits))
        }
        return null
    }

    override fun onBindFooterViewHolder(holder:SectionedViewHolder?, section:Int) {
        return
    }
}