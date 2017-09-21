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
package jahirfiquitiva.libs.frames.ui.fragments.dialogs

import android.app.Dialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context.CLIPBOARD_SERVICE
import android.support.design.widget.BottomSheetBehavior
import android.support.design.widget.BottomSheetDialogFragment
import android.support.design.widget.CoordinatorLayout
import android.support.v4.app.FragmentActivity
import android.support.v4.app.FragmentManager
import android.support.v7.graphics.Palette
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import ca.allanwang.kau.utils.gone
import ca.allanwang.kau.utils.toHexString
import ca.allanwang.kau.utils.visible
import jahirfiquitiva.libs.frames.R
import jahirfiquitiva.libs.frames.ui.adapters.WallpaperInfoAdapter
import jahirfiquitiva.libs.frames.ui.adapters.viewholders.WallpaperDetail
import jahirfiquitiva.libs.kauextensions.extensions.isInHorizontalMode

class InfoBottomSheet:BottomSheetDialogFragment() {
    
    private var rv:RecyclerView? = null
    private var progress:ProgressBar? = null
    private var adapter:WallpaperInfoAdapter? = null
    private var behavior:BottomSheetBehavior<View>? = null
    private val details = ArrayList<WallpaperDetail>()
    private var palette:Palette? = null
    
    private val sheetCallback = object:BottomSheetBehavior.BottomSheetCallback() {
        override fun onSlide(bottomSheet:View, slideOffset:Float) {
            var correctAlpha = slideOffset + 1
            if (correctAlpha < 0) correctAlpha = 0.0F
            if (correctAlpha > 1) correctAlpha = 1.0F
            bottomSheet.alpha = correctAlpha
        }
        
        override fun onStateChanged(bottomSheet:View, newState:Int) {
            if (newState == BottomSheetBehavior.STATE_HIDDEN) dismiss()
        }
    }
    
    override fun setupDialog(dialog:Dialog?, style:Int) {
        super.setupDialog(dialog, style)
        
        val detailView = View.inflate(context, R.layout.info_dialog, null)
        
        progress = detailView?.findViewById(R.id.loading_view)
        progress?.visible()
        
        rv = detailView?.findViewById(R.id.info_rv)
        rv?.gone()
        rv?.itemAnimator = DefaultItemAnimator()
        
        val layoutManager = GridLayoutManager(context, if (context.isInHorizontalMode) 4 else 3,
                                              GridLayoutManager.VERTICAL, false)
        rv?.layoutManager = layoutManager
        
        if (adapter == null) adapter = WallpaperInfoAdapter {
            if (it != 0) {
                val clipboard = context.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                clipboard.primaryClip = ClipData.newPlainText("label", it.toHexString())
                Toast.makeText(context, R.string.copied_to_clipboard, Toast.LENGTH_SHORT).show()
            }
        }
        adapter?.setLayoutManager(layoutManager)
        adapter?.let { rv?.adapter = it }
        setupAdapter()
        
        dialog?.setContentView(detailView)
        
        val params = (detailView?.parent as? View)?.layoutParams as CoordinatorLayout.LayoutParams
        val parentBehavior = params.behavior
        parentBehavior?.let {
            if (it is BottomSheetBehavior) {
                (parentBehavior as BottomSheetBehavior).setBottomSheetCallback(sheetCallback)
                behavior = parentBehavior
            }
        }
    }
    
    fun setDetailsAndPalette(details:ArrayList<WallpaperDetail>, palette:Palette?) {
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
        rv?.visible()
    }
    
    fun show(context:FragmentActivity) {
        show(context.supportFragmentManager, TAG)
    }
    
    override fun show(manager:FragmentManager?, tag:String?) {
        super.show(manager, tag)
        behavior?.state = BottomSheetBehavior.STATE_EXPANDED
    }
    
    fun animateHide() {
        behavior?.state = BottomSheetBehavior.STATE_COLLAPSED
        behavior?.state = BottomSheetBehavior.STATE_HIDDEN
    }
    
    companion object {
        private val TAG = "InfoBottomSheet"
        
        fun build(details:ArrayList<WallpaperDetail>, palette:Palette?):InfoBottomSheet =
                InfoBottomSheet().apply {
                    if (details.size > 0) {
                        this.details.clear()
                        this.details.addAll(details)
                    }
                    this.palette = palette
                }
        
        fun show(context:FragmentActivity, details:ArrayList<WallpaperDetail>, palette:Palette?) =
                build(details, palette).show(context.supportFragmentManager, TAG)
    }
}