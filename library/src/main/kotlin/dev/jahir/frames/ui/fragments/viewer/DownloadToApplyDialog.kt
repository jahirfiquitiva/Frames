package dev.jahir.frames.ui.fragments.viewer

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ProgressBar
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.fragment.app.DialogFragment
import com.tonyodev.fetch2.Download
import com.tonyodev.fetch2.Error
import com.tonyodev.fetch2.Fetch
import com.tonyodev.fetch2.FetchConfiguration
import com.tonyodev.fetch2.NetworkType
import com.tonyodev.fetch2.Priority
import com.tonyodev.fetch2.Request
import com.tonyodev.fetch2core.DownloadBlock
import com.tonyodev.fetch2core.Func
import dev.jahir.frames.R
import dev.jahir.frames.data.listeners.BaseFetchListener
import dev.jahir.frames.data.models.Wallpaper
import dev.jahir.frames.extensions.cancelable
import dev.jahir.frames.extensions.gone
import dev.jahir.frames.extensions.mdDialog
import dev.jahir.frames.extensions.view
import dev.jahir.frames.utils.WallpaperDownloadNotificationManager
import java.io.File

open class DownloadToApplyDialog : DialogFragment(), BaseFetchListener {

    var applyToOption = -1
    var wallpaper: Wallpaper? = null

    val filePath: String by lazy {
        val filename =
            wallpaper?.url.orEmpty().substring(wallpaper?.url.orEmpty().lastIndexOf("/") + 1)
        "${requireContext().cacheDir}${File.separator}$filename"
    }

    private val fetchConfig: FetchConfiguration by lazy {
        FetchConfiguration.Builder(requireContext())
            .setDownloadConcurrentLimit(3)
            .build()
    }
    private val fetch: Fetch by lazy {
        Fetch.Impl.getInstance(fetchConfig).apply { addListener(this@DownloadToApplyDialog) }
    }
    private val request: Request by lazy {
        Request(wallpaper?.url.orEmpty(), filePath).apply {
            priority = Priority.HIGH
            networkType = NetworkType.ALL
            addHeader(
                WallpaperDownloadNotificationManager.INTERNAL_FRAMES_WALLPAPER_HEADER,
                wallpaper?.name.orEmpty()
            )
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        super.onCreateDialog(savedInstanceState)
        val dialog = requireContext().mdDialog {
            view(R.layout.dialog_apply)
            cancelable(false)
        }
        isCancelable = false
        dialog.setOnShowListener { startDownload() }
        return dialog
    }

    private fun startDownload() {
        fetch.enqueue(request, Func { }, Func { error ->
            Log.e(
                "Frames",
                "${error.name} ~~ ${error.httpResponse?.code} ~~ ${error.httpResponse?.errorResponse}",
                error.throwable
            )
            showFinalMessage()
        })
    }

    open fun showFinalMessage(@StringRes message: Int = R.string.unexpected_error_occurred) {
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
        try {
            File(filePath).delete()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        fetch.cancel(request.id)
        applyToOption = -1
    }

    private fun setMessage(@StringRes res: Int) {
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

    override fun onStarted(
        download: Download,
        downloadBlocks: List<DownloadBlock>,
        totalBlocks: Int
    ) {
        super.onStarted(download, downloadBlocks, totalBlocks)
        setMessage(R.string.applying_downloading)
    }

    override fun onError(download: Download, error: Error, throwable: Throwable?) {
        super.onError(download, error, throwable)
        showFinalMessage()
    }

    override fun dismiss() {
        super.dismiss()
        fetch.cancel(request.id)
        fetch.removeListener(this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == WITH_OTHER_APP_CODE) {
            showFinalMessage(
                if (resultCode != 0) R.string.applying_applied
                else R.string.unexpected_error_occurred
            )
        }
    }

    companion object {
        internal const val TAG = "WALLPAPER_APPLIER_DIALOG"
        const val WITH_OTHER_APP_CODE = 733
    }
}