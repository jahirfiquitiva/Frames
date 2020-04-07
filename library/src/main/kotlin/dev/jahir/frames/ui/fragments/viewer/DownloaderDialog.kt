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
import dev.jahir.frames.extensions.utils.postDelayed
import dev.jahir.frames.extensions.views.gone

class DownloaderDialog : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        super.onCreateDialog(savedInstanceState)
        val dialog = requireContext().mdDialog {
            view(R.layout.dialog_apply)
            cancelable(false)
        }
        isCancelable = false
        dialog.setOnShowListener {
            postDelayed(2500) {
                dialog.setCancelable(true)
                isCancelable = true
            }
        }
        return dialog
    }

    internal fun showFinalMessage(@StringRes message: Int = R.string.unexpected_error_occurred) =
        showFinalMessage(string(message))

    internal fun showFinalMessage(message: String) {
        activity?.runOnUiThread {
            dialog?.findViewById<View?>(R.id.loading)?.gone()
            setMessage(message)
        }
        dialog?.setCancelable(true)
        isCancelable = true
    }

    internal fun setMessage(@StringRes res: Int) = setMessage(string(res))

    internal fun setMessage(message: String) {
        try {
            val textView: TextView? = dialog?.findViewById(R.id.dialog_apply_message)
            textView?.text = message
        } catch (e: Exception) {
        }
    }

    fun show(activity: FragmentActivity) {
        show(activity.supportFragmentManager, TAG)
    }

    companion object {
        private const val TAG = "WALLPAPER_DOWNLOAD_DIALOG"

        fun create() = DownloaderDialog()

        fun show(activity: FragmentActivity) =
            create().show(activity.supportFragmentManager, TAG)
    }
}