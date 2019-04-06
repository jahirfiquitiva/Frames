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

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context.CLIPBOARD_SERVICE
import android.content.Intent
import android.view.View
import android.widget.ProgressBar
import androidx.fragment.app.FragmentActivity
import androidx.palette.graphics.Palette
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ca.allanwang.kau.utils.dpToPx
import ca.allanwang.kau.utils.gone
import ca.allanwang.kau.utils.setPaddingTop
import ca.allanwang.kau.utils.toHexString
import ca.allanwang.kau.utils.toast
import ca.allanwang.kau.utils.visible
import jahirfiquitiva.libs.frames.R
import jahirfiquitiva.libs.frames.data.models.Collection
import jahirfiquitiva.libs.frames.ui.activities.CollectionActivity
import jahirfiquitiva.libs.frames.ui.activities.base.BaseFramesActivity
import jahirfiquitiva.libs.frames.ui.adapters.WallpaperInfoAdapter
import jahirfiquitiva.libs.frames.ui.adapters.viewholders.WallpaperDetail
import jahirfiquitiva.libs.kext.extensions.boolean
import jahirfiquitiva.libs.kext.extensions.isInHorizontalMode

class InfoBottomSheet : BaseBottomSheet() {
    
    private var recyclerView: RecyclerView? = null
    private var progress: ProgressBar? = null
    private var adapter: WallpaperInfoAdapter? = null
    
    private val collections = ArrayList<Collection>()
    private val details = ArrayList<WallpaperDetail>()
    private var palette: Palette? = null
    
    override fun getContentView(): View? {
        val detailView = View.inflate(context, R.layout.info_dialog, null)
        
        progress = detailView?.findViewById(R.id.loading_view)
        progress?.visible()
        
        recyclerView = detailView?.findViewById(R.id.info_rv)
        recyclerView?.gone()
        recyclerView?.setPaddingTop(8.dpToPx)
        recyclerView?.itemAnimator = DefaultItemAnimator()
        
        val layoutManager = GridLayoutManager(
            context, if (context?.isInHorizontalMode == true) 4 else 3,
            RecyclerView.VERTICAL, false)
        recyclerView?.layoutManager = layoutManager
        
        if (adapter == null) adapter = WallpaperInfoAdapter { forCollection, indexOrColor ->
            if (forCollection && boolean(R.bool.isFrames)) {
                collections.getOrNull(indexOrColor)?.let {
                    val intent = Intent(activity, CollectionActivity::class.java)
                    intent.putExtra("fromViewer", true)
                    intent.putExtra("item", it)
                    intent.putExtra(
                        "checker",
                        (activity as? BaseFramesActivity<*>)?.getLicenseChecker() != null)
                    activity?.startActivityForResult(intent, 12)
                }
            } else {
                val clipboard = context?.getSystemService(CLIPBOARD_SERVICE) as? ClipboardManager
                clipboard?.primaryClip = ClipData.newPlainText("label", indexOrColor.toHexString())
                context?.toast(R.string.copied_to_clipboard)
            }
        }
        adapter?.setLayoutManager(layoutManager)
        adapter?.let { recyclerView?.adapter = it }
        setupAdapter()
        
        return detailView
    }
    
    fun setDetails(
        collections: ArrayList<Collection>,
        details: ArrayList<WallpaperDetail>,
        palette: Palette?
                  ) {
        if (collections.size > 0) {
            this.collections.clear()
            this.collections.addAll(collections)
        }
        if (details.size > 0) {
            this.details.clear()
            this.details.addAll(details)
        }
        this.palette = palette
        setupAdapter()
    }
    
    private fun setupAdapter() {
        adapter?.setDetails(collections, details, palette)
        progress?.gone()
        recyclerView?.visible()
    }
    
    override fun shouldExpandOnShow(): Boolean = true
    
    companion object {
        const val TAG = "InfoBottomSheet"
        
        fun build(
            collections: ArrayList<Collection>,
            details: ArrayList<WallpaperDetail>,
            palette: Palette?
                 ): InfoBottomSheet =
            InfoBottomSheet().apply {
                if (collections.size > 0) {
                    this.collections.clear()
                    this.collections.addAll(collections)
                }
                if (details.size > 0) {
                    this.details.clear()
                    this.details.addAll(details)
                }
                this.palette = palette
            }
        
        fun show(
            context: FragmentActivity,
            collections: ArrayList<Collection>,
            details: ArrayList<WallpaperDetail>,
            palette: Palette?
                ) =
            build(collections, details, palette).show(context.supportFragmentManager, TAG)
    }
}
