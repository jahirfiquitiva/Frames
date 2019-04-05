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
package jahirfiquitiva.libs.frames.ui.adapters

import android.os.Build
import android.view.ViewGroup
import ca.allanwang.kau.utils.inflate
import com.afollestad.sectionedrecyclerview.SectionedRecyclerViewAdapter
import com.afollestad.sectionedrecyclerview.SectionedViewHolder
import jahirfiquitiva.libs.frames.R
import jahirfiquitiva.libs.frames.ui.adapters.viewholders.QuickActionsViewHolder
import jahirfiquitiva.libs.frames.ui.adapters.viewholders.SectionedHeaderViewHolder

class QuickActionsAdapter(
    private val title: String = "",
    private val allowDownload: Boolean = true,
    private val allowExternal: Boolean = false,
    private val onDismissIconPressed: () -> Unit = {},
    private val onOptionClick: (index: Int) -> Unit = {}
                         ) :
    SectionedRecyclerViewAdapter<SectionedViewHolder>() {
    
    private val options: ArrayList<Pair<Int, Int>> by lazy {
        val correctList = ArrayList<Pair<Int, Int>>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            correctList.add(Pair(R.drawable.ic_apply_homescreen, R.string.home_screen))
            correctList.add(Pair(R.drawable.ic_apply_lockscreen, R.string.lock_screen))
        }
        correctList.add(Pair(R.drawable.ic_apply_both, R.string.home_lock_screen))
        if (allowExternal)
            correctList.add(
                Pair(R.drawable.ic_apply_external, R.string.apply_with_other_app))
        if (allowDownload) correctList.add(Pair(R.drawable.ic_download, R.string.download))
        correctList
    }
    
    override fun getSectionCount(): Int = 1
    
    override fun getItemCount(section: Int): Int = options.size
    
    override fun getItemViewType(
        section: Int, relativePosition: Int,
        absolutePosition: Int
                                ): Int = section
    
    override fun onBindViewHolder(
        holder: SectionedViewHolder?, section: Int, relativePosition: Int,
        absolutePosition: Int
                                 ) {
        holder?.let {
            (it as? QuickActionsViewHolder)?.bind(
                options[relativePosition].first, options[relativePosition].second, onOptionClick)
        }
    }
    
    override fun onBindHeaderViewHolder(
        holder: SectionedViewHolder?, section: Int,
        expanded: Boolean
                                       ) {
        (holder as? SectionedHeaderViewHolder)?.setTitle(
            title, false, true, false, onDismissIconPressed)
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SectionedViewHolder =
        when (viewType) {
            0 -> QuickActionsViewHolder(parent.inflate(R.layout.actions_bottom_sheet_item))
            else -> SectionedHeaderViewHolder(parent.inflate(R.layout.item_section_header))
        }
    
    override fun onBindFooterViewHolder(holder: SectionedViewHolder?, section: Int) {}
}
