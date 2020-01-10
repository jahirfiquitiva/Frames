package dev.jahir.frames.ui.fragments

import android.app.Dialog
import android.app.WallpaperManager
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.widget.ProgressBar
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
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
import dev.jahir.frames.data.models.Wallpaper
import dev.jahir.frames.extensions.getUri
import dev.jahir.frames.extensions.gone
import dev.jahir.frames.extensions.prefs
import dev.jahir.frames.utils.BaseFetchListener
import dev.jahir.frames.utils.WallpaperDownloadNotificationManager
import dev.jahir.frames.utils.ensureBackgroundThread
import java.io.File


class WallpaperApplierDialog : DialogFragment(), BaseFetchListener {

    private var applyToOption = 0
    private var wallpaper: Wallpaper? = null

    private val applyFlag: Int
        get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            when (applyToOption) {
                0 -> WallpaperManager.FLAG_SYSTEM
                1 -> WallpaperManager.FLAG_LOCK
                3 -> -1
                else -> WallpaperManager.FLAG_SYSTEM or WallpaperManager.FLAG_LOCK
            }
        } else applyToOption

    private val wm: WallpaperManager? by lazy {
        try {
            WallpaperManager.getInstance(context)
        } catch (e: Exception) {
            null
        }
    }

    private val filePath: String by lazy {
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
        Fetch.Impl.getInstance(fetchConfig).apply { addListener(this@WallpaperApplierDialog) }
    }
    private val request: Request by lazy {
        Request(wallpaper?.url.orEmpty(), filePath).apply {
            priority = Priority.HIGH
            // TODO: Allow WiFi only downloads
            networkType = NetworkType.ALL
            addHeader(
                WallpaperDownloadNotificationManager.INTERNAL_FRAMES_WALLPAPER_HEADER,
                wallpaper?.name.orEmpty()
            )
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        super.onCreateDialog(savedInstanceState)

        val dialog = MaterialAlertDialogBuilder(context)
            .setView(R.layout.dialog_apply)
            .setCancelable(false)
            .create()
        isCancelable = false

        dialog.setOnShowListener { startDownload() }
        return dialog
    }

    private fun startDownload() {
        fetch.enqueue(request, Func { }, Func { showFinalMessage() })
    }

    private fun showFinalMessage(@StringRes message: Int = R.string.unexpected_error_occurred) {
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
    }

    private fun setMessage(@StringRes res: Int) {
        try {
            setMessage(requireContext().getString(res))
        } catch (e: Exception) {
        }
    }

    private fun setMessage(message: String) {
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

    override fun onCompleted(download: Download) {
        super.onCompleted(download)
        val text = try {
            requireContext().getString(R.string.applying_wallpaper, wallpaper?.name.orEmpty())
        } catch (e: Exception) {
            try {
                requireContext().getString(R.string.applying_wallpaper_def)
            } catch (e: Exception) {
                ""
            }
        }
        setMessage(text)
        setAsWallpaper(download)
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

    private fun setAsWallpaper(download: Download) {
        ensureBackgroundThread {
            if (applyFlag > 0) {
                val bitmap = try {
                    BitmapFactory.decodeFile(download.file)
                } catch (e: Exception) {
                    null
                }

                val scaledBitmap = if (context?.prefs?.shouldCropWallpaperBeforeApply == true) {
                    bitmap?.let {
                        try {
                            val wantedHeight = wm?.desiredMinimumHeight ?: 0
                            val ratio = wantedHeight / bitmap.height.toFloat()
                            val wantedWidth = (bitmap.width * ratio).toInt()
                            Bitmap.createScaledBitmap(bitmap, wantedWidth, wantedHeight, true)
                        } catch (e: Exception) {
                            it
                        }
                    }
                } else bitmap

                scaledBitmap?.let { bmp ->
                    wm?.let { wm ->
                        val result = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            wm.setBitmap(bmp, null, true, applyFlag)
                        } else {
                            wm.setBitmap(bmp)
                            -1
                        }
                        showFinalMessage(
                            if (result != 0) R.string.applying_applied
                            else R.string.unexpected_error_occurred
                        )
                    } ?: {
                        showFinalMessage()
                    }()
                } ?: {
                    showFinalMessage()
                }()
            } else {
                val uri = File(filePath).getUri(context)
                uri?.let {
                    val setWall = Intent(Intent.ACTION_ATTACH_DATA)
                    setWall.setDataAndType(it, "image/*")
                    setWall.putExtra("mimeType", "image/*")
                    setWall.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    try {
                        startActivityForResult(
                            Intent.createChooser(setWall, getString(R.string.apply_w_external_app)),
                            WITH_OTHER_APP_CODE
                        )
                    } catch (e: Exception) {
                        showFinalMessage()
                    }
                } ?: { showFinalMessage() }()
            }
        }
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
        private const val TAG = "WALLPAPER_APPLIER_DIALOG"
        private const val WITH_OTHER_APP_CODE = 733

        fun create(option: Int = 0, wallpaper: Wallpaper? = null) = WallpaperApplierDialog().apply {
            applyToOption = option
            this.wallpaper = wallpaper
        }

        fun show(activity: FragmentActivity, option: Int = 0, wallpaper: Wallpaper? = null) =
            create(option, wallpaper).show(activity.supportFragmentManager, TAG)
    }
}