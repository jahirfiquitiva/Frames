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
package jahirfiquitiva.libs.frames.ui.fragments.dialogs

import android.os.Build
import android.view.View
import android.widget.ProgressBar
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ca.allanwang.kau.utils.gone
import ca.allanwang.kau.utils.visible
import jahirfiquitiva.libs.frames.R
import jahirfiquitiva.libs.frames.data.models.Wallpaper
import jahirfiquitiva.libs.frames.ui.activities.base.BaseWallpaperActionsActivity
import jahirfiquitiva.libs.frames.ui.activities.base.BaseWallpaperActionsActivity.Companion.APPLY_ACTION_ID
import jahirfiquitiva.libs.frames.ui.activities.base.BaseWallpaperActionsActivity.Companion.DOWNLOAD_ACTION_ID
import jahirfiquitiva.libs.frames.ui.adapters.QuickActionsAdapter
import jahirfiquitiva.libs.kext.extensions.string

class QuickActionsBottomSheet : BaseBottomSheet() {
    
    private var wallpaper: Wallpaper? = null
    private var allowDownload: Boolean = true
    private var allowExternal: Boolean = false
    
    private var recyclerView: RecyclerView? = null
    private var progress: ProgressBar? = null
    private var adapter: QuickActionsAdapter? = null
    
    private val onDismissPressed: () -> Unit = { dismiss() }
    private val onOptionPressed: (Int) -> Unit = { doAction(it) }
    
    override fun getContentView(): View? {
        val detailView = View.inflate(context, R.layout.actions_bottom_sheet, null)
        
        progress = detailView?.findViewById(R.id.loading_view)
        progress?.visible()
        
        recyclerView = detailView?.findViewById(R.id.info_rv)
        recyclerView?.gone()
        recyclerView?.itemAnimator = DefaultItemAnimator()
        
        recyclerView?.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        
        if (adapter == null)
            adapter =
                QuickActionsAdapter(
                    if (allowDownload) wallpaper?.name ?: "" else string(R.string.apply_to),
                    allowDownload, allowExternal, onDismissPressed, onOptionPressed)
        adapter?.let { recyclerView?.adapter = it }
        
        progress?.gone()
        recyclerView?.visible()
        
        return detailView
    }
    
    private fun doAction(index: Int) {
        val rightOption = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) index + 2 else index
        val downloadOption = if (allowExternal) 4 else 3
        (activity as? BaseWallpaperActionsActivity<*>)?.let {
            if (rightOption == downloadOption) {
                it.doItemClick(DOWNLOAD_ACTION_ID)
            } else if (rightOption < downloadOption) {
                it.doItemClick(APPLY_ACTION_ID, rightOption)
            }
            dismiss()
        }
    }
    
    override fun shouldExpandOnShow(): Boolean = true
    
    companion object {
        private const val TAG = "quick_actions"
        
        fun show(
            context: FragmentActivity,
            wallpaper: Wallpaper? = null,
            allowDownload: Boolean = true,
            allowExternal: Boolean = false
                ) {
            QuickActionsBottomSheet().apply {
                this.wallpaper = wallpaper
                this.allowDownload = allowDownload
                this.allowExternal = allowExternal
            }.show(context, TAG)
        }
    }
}
