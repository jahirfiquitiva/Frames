package dev.jahir.frames.ui.activities.base

import com.google.android.material.snackbar.Snackbar
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
import dev.jahir.frames.extensions.snackbar
import dev.jahir.frames.extensions.string
import dev.jahir.frames.ui.fragments.viewer.DownloaderDialog
import dev.jahir.frames.utils.Prefs
import dev.jahir.frames.utils.WallpaperDownloadNotificationManager
import dev.jahir.frames.utils.postDelayed
import java.io.File
import java.lang.ref.WeakReference

abstract class BaseWallpaperFetcherActivity<out P : Prefs> :
    BaseStoragePermissionRequestActivity<P>() {

    private val fetchListener: BaseFetchListener by lazy {
        object : BaseFetchListener {
            override fun onStarted(
                download: Download,
                downloadBlocks: List<DownloadBlock>,
                totalBlocks: Int
            ) {
                super.onStarted(download, downloadBlocks, totalBlocks)
                dismissDownloadDialog()
            }

            override fun onCompleted(download: Download) {
                super.onCompleted(download)
                snackbar(
                    string(R.string.download_successful, download.file),
                    Snackbar.LENGTH_LONG
                )
                dismissDownloadDialog()
            }

            override fun onError(download: Download, error: Error, throwable: Throwable?) {
                super.onError(download, error, throwable)
                dismissDownloadDialog(true)
            }
        }
    }

    private val fetch: Fetch by lazy {
        val fetchConfig = FetchConfiguration.Builder(this)
            .setNotificationManager(object :
                WallpaperDownloadNotificationManager(WeakReference(applicationContext)) {
                override fun getFetchInstanceForNamespace(namespace: String): Fetch = fetch
            })
            .build()
        Fetch.Impl.getInstance(fetchConfig).apply { addListener(fetchListener) }
    }

    private val downloaderDialog: DownloaderDialog by lazy { DownloaderDialog.create() }

    private var request: Request? = null

    internal fun initFetch(wallpaper: Wallpaper?) {
        wallpaper ?: return
        val folder = prefs.downloadsFolder ?: externalCacheDir ?: cacheDir
        val filename = wallpaper.url.substring(wallpaper.url.lastIndexOf("/") + 1)

        request = Request(wallpaper.url, "$folder${File.separator}$filename")
        request?.priority = Priority.HIGH
        request?.networkType = NetworkType.ALL
        request?.addHeader(
            WallpaperDownloadNotificationManager.INTERNAL_FRAMES_WALLPAPER_HEADER,
            wallpaper.name
        )
    }

    internal fun startDownload() {
        request?.let {
            fetch.enqueue(it, Func { downloaderDialog.show(this) },
                Func {
                    downloaderDialog.showFinalMessage()
                    downloaderDialog.show(this)
                })
        } ?: { dismissDownloadDialog(true) }()
    }

    internal fun cancelDownload(remove: Boolean = true) {
        try {
            fetch.cancel(request?.id ?: -1)
            fetch.cancelAll()
            if (remove) {
                fetch.remove(request?.id ?: -1)
                fetch.removeAll()
                fetch.delete(request?.id ?: -1)
                fetch.deleteAll()
            }
        } catch (e: Exception) {
        }
    }

    private fun dismissDownloadDialog(
        cancelDownload: Boolean = false,
        removeDownload: Boolean = true
    ) {
        if (cancelDownload) cancelDownload(removeDownload)
        postDelayed(50) {
            try {
                downloaderDialog.dismiss()
            } catch (e: Exception) {
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        dismissDownloadDialog(true, false)
        fetch.removeListener(fetchListener)
        fetch.close()
    }
}