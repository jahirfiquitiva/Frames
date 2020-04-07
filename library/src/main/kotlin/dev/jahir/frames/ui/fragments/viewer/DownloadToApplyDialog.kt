package dev.jahir.frames.ui.fragments.viewer

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
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
import dev.jahir.frames.extensions.fragments.cancelable
import dev.jahir.frames.extensions.fragments.mdDialog
import dev.jahir.frames.extensions.fragments.string
import dev.jahir.frames.extensions.fragments.view
import dev.jahir.frames.extensions.utils.postDelayed
import dev.jahir.frames.extensions.views.gone
import dev.jahir.frames.ui.notifications.WallpaperDownloadNotificationManager
import java.io.File
import java.lang.ref.WeakReference

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
            .setNotificationManager(
                object : WallpaperDownloadNotificationManager(
                    WeakReference(context?.applicationContext), true
                ) {
                    override fun getFetchInstanceForNamespace(namespace: String): Fetch = fetch
                }
            )
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
        postDelayed(5000) {
            dialog?.setCancelable(true)
            isCancelable = true
        }
    }

    open fun showFinalMessage(@StringRes message: Int = R.string.unexpected_error_occurred) {
        activity?.runOnUiThread {
            dialog?.findViewById<View?>(R.id.loading)?.gone()
            setMessage(message)
            dialog?.setCancelable(true)
        }
        isCancelable = true
        try {
            File(filePath).delete()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        clearAndCancelDownload()
        applyToOption = -1
    }

    private fun setMessage(@StringRes res: Int) {
        setMessage(string(res))
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

    override fun onCompleted(download: Download) {
        super.onCompleted(download)
        clearAndCancelDownload()
    }

    override fun dismiss() {
        super.dismiss()
        clearAndCancelDownload(true)
        fetch.close()
    }

    private fun clearAndCancelDownload(removeListener: Boolean = false) {
        fetch.cancel(request.id)
        fetch.cancelAll()
        fetch.remove(request.id)
        fetch.removeAll()
        fetch.delete(request.id)
        fetch.deleteAll()
        if (removeListener) fetch.removeListener(this)
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