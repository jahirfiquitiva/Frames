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

import android.support.annotation.StringRes
import android.view.ViewGroup
import ca.allanwang.kau.utils.inflate
import com.afollestad.sectionedrecyclerview.SectionedRecyclerViewAdapter
import com.afollestad.sectionedrecyclerview.SectionedViewHolder
import com.bumptech.glide.RequestManager
import jahirfiquitiva.libs.frames.R
import jahirfiquitiva.libs.frames.helpers.extensions.jfilter
import jahirfiquitiva.libs.frames.ui.adapters.viewholders.Credit
import jahirfiquitiva.libs.frames.ui.adapters.viewholders.DashboardCreditViewHolder
import jahirfiquitiva.libs.frames.ui.adapters.viewholders.SectionedHeaderViewHolder
import jahirfiquitiva.libs.frames.ui.adapters.viewholders.SimpleCreditViewHolder

class CreditsAdapter(
        @StringRes private val dashboardTitle: Int,
        private val manager: RequestManager,
        private val credits: ArrayList<Credit>
                    ) :
        SectionedRecyclerViewAdapter<SectionedViewHolder>() {
    
    private val creatorCredits: ArrayList<Credit> by lazy {
        credits.jfilter { it.type == Credit.Type.CREATOR }
    }
    
    private val dashboardCredits: ArrayList<Credit> by lazy {
        credits.jfilter { it.type == Credit.Type.DASHBOARD }
    }
    
    private val devCredits: ArrayList<Credit> by lazy {
        credits.jfilter { it.type == Credit.Type.DEV_CONTRIBUTION }
    }
    
    private val uiCredits: ArrayList<Credit> by lazy {
        credits.jfilter { it.type == Credit.Type.UI_CONTRIBUTION }
    }
    
    init {
        shouldShowHeadersForEmptySections(false)
        shouldShowFooters(false)
    }
    
    override fun getSectionCount(): Int = 4
    
    override fun getItemViewType(
            section: Int, relativePosition: Int,
            absolutePosition: Int
                                ): Int = section
    
    override fun onBindViewHolder(
            holder: SectionedViewHolder?, section: Int, relativePosition: Int,
            absolutePosition: Int
                                 ) {
        holder?.let {
            if (it is DashboardCreditViewHolder) {
                when (section) {
                    0 -> it.setItem(manager, creatorCredits[relativePosition])
                    1 -> it.setItem(manager, dashboardCredits[relativePosition])
                    2 -> it.setItem(manager, devCredits[relativePosition])
                    3 -> it.setItem(manager, uiCredits[relativePosition])
                }
            }
        }
    }
    
    override fun onViewRecycled(holder: SectionedViewHolder) {
        (holder as? DashboardCreditViewHolder)?.unbind()
        super.onViewRecycled(holder)
    }
    
    override fun getItemCount(section: Int): Int =
            when (section) {
                0 -> creatorCredits.size
                1 -> dashboardCredits.size
                2 -> devCredits.size
                3 -> uiCredits.size
                else -> 0
            }
    
    override fun onBindHeaderViewHolder(
            holder: SectionedViewHolder?, section: Int,
            expanded: Boolean
                                       ) {
        (holder as? SectionedHeaderViewHolder)?.let {
            when (section) {
                0 -> it.setTitle(R.string.app_name, false, false, expanded)
                1 -> it.setTitle(dashboardTitle, true, false, expanded)
                2 -> it.setTitle(R.string.dev_contributions, true, true, expanded) {
                    toggleSectionExpanded(section)
                }
                3 -> it.setTitle(R.string.ui_contributions, true, true, expanded) {
                    toggleSectionExpanded(section)
                }
            }
        }
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SectionedViewHolder =
            when (viewType) {
                0, 1 -> DashboardCreditViewHolder(parent.inflate(R.layout.item_credits))
                2, 3 -> SimpleCreditViewHolder(parent.inflate(R.layout.item_credits))
                else -> SectionedHeaderViewHolder(parent.inflate(R.layout.item_section_header))
            }
    
    override fun onBindFooterViewHolder(holder: SectionedViewHolder?, section: Int) {}
}