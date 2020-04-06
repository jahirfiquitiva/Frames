package dev.jahir.frames.ui.fragments.viewer

import android.app.Dialog
import android.os.Bundle
import android.widget.ProgressBar
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import dev.jahir.frames.R
import dev.jahir.frames.extensions.cancelable
import dev.jahir.frames.extensions.gone
import dev.jahir.frames.extensions.mdDialog
import dev.jahir.frames.extensions.string
import dev.jahir.frames.extensions.view
import dev.jahir.frames.utils.postDelayed

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
            try {
                val progress: ProgressBar? = dialog?.findViewById(R.id.loading)
                progress?.gone()
            } catch (e: Exception) {
            }
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