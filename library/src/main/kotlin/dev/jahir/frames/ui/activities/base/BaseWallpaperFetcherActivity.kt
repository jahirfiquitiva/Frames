package dev.jahir.frames.ui.activities.base

import android.content.Context
import android.content.Intent
import android.media.MediaScannerConnection
import android.net.Uri
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
import dev.jahir.frames.data.Preferences
import dev.jahir.frames.data.listeners.BaseFetchListener
import dev.jahir.frames.data.models.Wallpaper
import dev.jahir.frames.extensions.context.string
import dev.jahir.frames.extensions.utils.ensureBackgroundThread
import dev.jahir.frames.extensions.utils.postDelayed
import dev.jahir.frames.extensions.views.snackbar
import dev.jahir.frames.ui.fragments.viewer.DownloaderDialog
import dev.jahir.frames.ui.notifications.WallpaperDownloadNotificationManager
import java.io.File
import java.lang.ref.WeakReference
import java.net.URLConnection


abstract class BaseWallpaperFetcherActivity<out P : Preferences> :
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
                ensureBackgroundThread {
                    FramesMediaScanner(
                        this@BaseWallpaperFetcherActivity,
                        download.file,
                        download.fileUri
                    )
                }
                snackbar(
                    string(R.string.download_successful, download.file),
                    Snackbar.LENGTH_LONG, snackbarAnchorId
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
        val folder = preferences.downloadsFolder ?: externalCacheDir ?: cacheDir
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
        dismissDownloadDialog(cancelDownload = true, removeDownload = false)
        fetch.removeListener(fetchListener)
        fetch.close()
    }

    private class FramesMediaScanner(
        private var context: Context?,
        private var path: String?,
        private var uri: Uri?
    ) : MediaScannerConnection.MediaScannerConnectionClient {

        private var connection: MediaScannerConnection? = MediaScannerConnection(context, this)

        init {
            connection?.connect()
        }

        override fun onMediaScannerConnected() {
            try {
                connection?.scanFile(path, URLConnection.guessContentTypeFromName(path))
            } catch (e: Exception) {
                broadcastScanFile(uri)
                broadcastMediaMounted(uri)
            } finally {
                clean()
            }
        }

        override fun onScanCompleted(scannedPath: String?, scannedUri: Uri?) {
            broadcastScanFile(scannedUri)
            broadcastMediaMounted(scannedUri)
            clean()
        }

        private fun broadcastMediaMounted(uri: Uri?) {
            try {
                context?.sendBroadcast(Intent(Intent.ACTION_MEDIA_MOUNTED, uri))
            } catch (e: Exception) {
            }
        }

        @Suppress("DEPRECATION")
        private fun broadcastScanFile(uri: Uri?) {
            try {
                context?.sendBroadcast(
                    Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE).apply { data = uri })
            } catch (e: Exception) {
            }
        }

        private fun clean() {
            uri = null
            path = null
            context = null
            connection?.disconnect()
        }
    }
}