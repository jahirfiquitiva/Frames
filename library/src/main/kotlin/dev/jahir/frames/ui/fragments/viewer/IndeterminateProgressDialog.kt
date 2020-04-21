package dev.jahir.frames.ui.fragments.viewer

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import dev.jahir.frames.R
import dev.jahir.frames.extensions.fragments.cancelable
import dev.jahir.frames.extensions.fragments.mdDialog
import dev.jahir.frames.extensions.fragments.string
import dev.jahir.frames.extensions.fragments.view
import dev.jahir.frames.extensions.views.visibleIf

@Suppress("MemberVisibilityCanBePrivate", "unused")
class IndeterminateProgressDialog : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        super.onCreateDialog(savedInstanceState)
        val dialog = requireContext().mdDialog {
            view(R.layout.dialog_progress)
            cancelable(false)
        }
        isCancelable = false
        return dialog
    }

    fun setMessage(@StringRes stringRes: Int, showProgressBar: Boolean, cancelable: Boolean) {
        setMessage(string(stringRes), showProgressBar, cancelable)
    }

    fun setMessage(message: String, showProgressBar: Boolean, cancelable: Boolean) {
        activity?.runOnUiThread {
            dialog?.findViewById<View?>(R.id.progress_bar)?.visibleIf(showProgressBar)
            try {
                val textView: TextView? = dialog?.findViewById(R.id.progress_message)
                textView?.text = message
            } catch (e: Exception) {
            }
        }
        dialog?.setCancelable(cancelable)
        isCancelable = cancelable
    }

    override fun setCancelable(cancelable: Boolean) {
        super.setCancelable(cancelable)
        dialog?.setCancelable(cancelable)
    }

    fun show(activity: FragmentActivity) {
        show(activity.supportFragmentManager, TAG)
    }

    companion object {
        private const val TAG = "WALLPAPER_DOWNLOAD_DIALOG"

        fun create() = IndeterminateProgressDialog()

        fun show(activity: FragmentActivity) =
            create().show(activity.supportFragmentManager, TAG)
    }
}