package dev.jahir.frames.ui.fragments.viewer

import android.app.Dialog
import android.os.Bundle
import android.widget.ProgressBar
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dev.jahir.frames.R
import dev.jahir.frames.extensions.gone

class DownloaderDialog : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        super.onCreateDialog(savedInstanceState)

        val dialog = MaterialAlertDialogBuilder(context)
            .setView(R.layout.dialog_apply)
            .setCancelable(false)
            .create()
        isCancelable = false

        return dialog
    }

    internal fun showFinalMessage(@StringRes message: Int = R.string.unexpected_error_occurred) {
        activity?.runOnUiThread {
            try {
                val progress: ProgressBar? = dialog?.findViewById(R.id.loading)
                progress?.gone()
            } catch (e: Exception) {
            }
            setMessage(message)
            dialog?.setCancelable(true)
        }
        isCancelable = true
    }

    internal fun setMessage(@StringRes res: Int) {
        try {
            setMessage(requireContext().getString(res))
        } catch (e: Exception) {
        }
    }

    internal fun setMessage(message: String) {
        try {
            val textView: TextView? = dialog?.findViewById(R.id.dialog_apply_message)
            textView?.text = message
        } catch (e: Exception) {
        }
    }

    fun show(activity: FragmentActivity) {
        show(activity.supportFragmentManager,
            TAG
        )
    }

    companion object {
        private const val TAG = "WALLPAPER_DOWNLOAD_DIALOG"

        fun create() =
            DownloaderDialog()

        fun show(activity: FragmentActivity) = create().show(activity.supportFragmentManager,
            TAG
        )
    }
}