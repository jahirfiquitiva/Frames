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
package jahirfiquitiva.libs.frames.ui.fragments.dialogs

import android.app.Dialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v4.app.FragmentActivity
import android.support.v7.graphics.Palette
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.widget.ProgressBar
import ca.allanwang.kau.utils.dpToPx
import ca.allanwang.kau.utils.gone
import ca.allanwang.kau.utils.setPadding
import ca.allanwang.kau.utils.setPaddingBottom
import ca.allanwang.kau.utils.toHexString
import ca.allanwang.kau.utils.visible
import jahirfiquitiva.libs.frames.R
import jahirfiquitiva.libs.frames.helpers.extensions.buildMaterialDialog
import jahirfiquitiva.libs.frames.ui.adapters.WallpaperInfoAdapter
import jahirfiquitiva.libs.frames.ui.adapters.viewholders.WallpaperDetail
import jahirfiquitiva.libs.kauextensions.extensions.actv
import jahirfiquitiva.libs.kauextensions.extensions.ctxt
import jahirfiquitiva.libs.kauextensions.extensions.isInHorizontalMode
import jahirfiquitiva.libs.kauextensions.extensions.showToast

@Suppress("DEPRECATION")
class InfoDialog : DialogFragment() {
    
    private var recyclerView: RecyclerView? = null
    private var progress: ProgressBar? = null
    private var adapter: WallpaperInfoAdapter? = null
    
    private val details = ArrayList<WallpaperDetail>()
    private var palette: Palette? = null
    
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = actv.buildMaterialDialog {
            customView(R.layout.info_dialog, false)
        }
        
        val detailView = dialog.customView
        
        progress = detailView?.findViewById(R.id.loading_view)
        progress?.visible()
        
        recyclerView = detailView?.findViewById(R.id.info_rv)
        recyclerView?.gone()
        recyclerView?.setPadding(8.dpToPx)
        recyclerView?.setPaddingBottom(16.dpToPx)
        recyclerView?.itemAnimator = DefaultItemAnimator()
        
        val layoutManager = GridLayoutManager(
                ctxt, if (ctxt.isInHorizontalMode) 3 else 2,
                GridLayoutManager.VERTICAL, false)
        recyclerView?.layoutManager = layoutManager
        
        if (adapter == null) adapter = WallpaperInfoAdapter {
            if (it != 0) {
                val clipboard =
                        context?.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                clipboard?.primaryClip = ClipData.newPlainText("label", it.toHexString())
                ctxt { it.showToast(R.string.copied_to_clipboard) }
            }
        }
        adapter?.setLayoutManager(layoutManager)
        adapter?.let { recyclerView?.adapter = it }
        setupAdapter()
        
        return dialog
    }
    
    fun setDetailsAndPalette(details: ArrayList<WallpaperDetail>, palette: Palette?) {
        if (details.size > 0) {
            this.details.clear()
            this.details.addAll(details)
        }
        this.palette = palette
        setupAdapter()
    }
    
    private fun setupAdapter() {
        adapter?.setDetailsAndPalette(details, palette)
        progress?.gone()
        recyclerView?.visible()
    }
    
    fun show(context: FragmentActivity) {
        show(context.supportFragmentManager, TAG)
    }
    
    companion object {
        private val TAG = "InfoDialog"
        
        fun build(details: ArrayList<WallpaperDetail>, palette: Palette?): InfoDialog =
                InfoDialog().apply {
                    if (details.size > 0) {
                        this.details.clear()
                        this.details.addAll(details)
                    }
                    this.palette = palette
                }
        
        fun show(
                context: FragmentActivity, details: ArrayList<WallpaperDetail>,
                palette: Palette?
                ) =
                build(details, palette).show(context.supportFragmentManager, TAG)
    }
}