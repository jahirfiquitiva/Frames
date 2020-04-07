package dev.jahir.frames.ui.fragments.base

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Build
import android.view.View
import android.view.WindowManager
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.FragmentActivity
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dev.jahir.frames.R
import dev.jahir.frames.extensions.context.getRightNavigationBarColor
import dev.jahir.frames.extensions.context.navigationBarLight
import dev.jahir.frames.extensions.resources.isDark
import dev.jahir.frames.extensions.utils.postDelayed

open class BaseBottomSheet : BottomSheetDialogFragment() {

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
    override fun setupDialog(dialog: Dialog, style: Int) {
        super.setupDialog(dialog, style)

        val content = getContentView()
        content?.let { dialog.setContentView(it) }

        val params =
            (content?.parent as? View)?.layoutParams as? CoordinatorLayout.LayoutParams
        val parentBehavior = params?.behavior

        if (parentBehavior != null && parentBehavior is BottomSheetBehavior<*>) {
            behavior = parentBehavior
            behavior?.saveFlags = BottomSheetBehavior.SAVE_ALL
            behavior?.addBottomSheetCallback(sheetCallback)
        }

        dialog.setOnShowListener { dialogInterface ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                (dialogInterface as? BottomSheetDialog)?.apply {
                    window?.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                    val navigationBarColor = context.getRightNavigationBarColor()
                    window?.navigationBarColor = navigationBarColor
                    window?.navigationBarLight = !navigationBarColor.isDark
                }
            }
            if (shouldExpandOnShow()) expand()
        }
    }

    fun show(context: FragmentActivity, tag: String) {
        show(context.supportFragmentManager, tag)
    }

    fun expand() {
        behavior?.state = BottomSheetBehavior.STATE_EXPANDED
        (dialog as? BottomSheetDialog)?.let {
            try {
                val sheet = it.findViewById<View?>(R.id.design_bottom_sheet)
                sheet ?: return@let
                BottomSheetBehavior.from(sheet).state = BottomSheetBehavior.STATE_EXPANDED
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