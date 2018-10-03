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

import android.annotation.SuppressLint
import android.app.Dialog
import android.support.design.widget.BottomSheetBehavior
import android.support.design.widget.BottomSheetDialog
import android.support.design.widget.CoordinatorLayout
import android.support.v4.app.FragmentActivity
import android.view.View
import ca.allanwang.kau.utils.postDelayed
import jahirfiquitiva.libs.kext.ui.fragments.RoundedBottomSheetDialogFragment

open class BaseBottomSheet : RoundedBottomSheetDialogFragment() {
    private var behavior: BottomSheetBehavior<*>? = null
    private val sheetCallback = object : BottomSheetBehavior.BottomSheetCallback() {
        override fun onSlide(bottomSheet: View, slideOffset: Float) {
            var correctAlpha = slideOffset + 1
            if (correctAlpha < 0) correctAlpha = 0.0F
            if (correctAlpha > 1) correctAlpha = 1.0F
            bottomSheet.alpha = correctAlpha
        }
        
        override fun onStateChanged(bottomSheet: View, newState: Int) {
            if (newState == BottomSheetBehavior.STATE_HIDDEN) dismiss()
        }
    }
    
    @SuppressLint("RestrictedApi")
    override fun setupDialog(dialog: Dialog?, style: Int) {
        super.setupDialog(dialog, style)
        
        val content = getContentView()
        dialog?.setContentView(content)
        
        val params = (content?.parent as? View)?.layoutParams as? CoordinatorLayout.LayoutParams
        val parentBehavior = params?.behavior
        
        if (parentBehavior != null && parentBehavior is BottomSheetBehavior<*>) {
            behavior = parentBehavior
            behavior?.setBottomSheetCallback(sheetCallback)
        }
        
        dialog?.setOnShowListener { if (shouldExpandOnShow()) expand() }
    }
    
    fun show(context: FragmentActivity, tag: String) {
        show(context.supportFragmentManager, tag)
    }
    
    fun expand() {
        behavior?.state = BottomSheetBehavior.STATE_EXPANDED
        (dialog as? BottomSheetDialog)?.let {
            try {
                val sheet = it.findViewById<View>(android.support.design.R.id.design_bottom_sheet)
                BottomSheetBehavior.from(sheet)?.state = BottomSheetBehavior.STATE_EXPANDED
            } catch (e: Exception) {
            }
        }
    }
    
    fun hide() {
        behavior?.state = BottomSheetBehavior.STATE_COLLAPSED
        postDelayed(10) {
            behavior?.state = BottomSheetBehavior.STATE_HIDDEN
        }
    }
    
    open fun getContentView(): View? = null
    open fun shouldExpandOnShow(): Boolean = false
}