package dev.jahir.frames.data.workers

import android.R.attr.path
import android.app.DownloadManager
import android.content.Context
import android.media.MediaScannerConnection
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.core.net.toUri
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import dev.jahir.frames.R
import dev.jahir.frames.data.network.DownloadListenerThread
import dev.jahir.frames.data.network.LocalDownloadListenerThread
import dev.jahir.frames.data.network.MediaScanner
import dev.jahir.frames.extensions.context.preferences
import dev.jahir.frames.extensions.context.string
import dev.jahir.frames.extensions.context.toast
import dev.jahir.frames.extensions.frames.filenameAndExtension
import dev.jahir.frames.extensions.resources.createIfDidNotExist
import dev.jahir.frames.extensions.resources.hasContent
import kotlinx.coroutines.coroutineScope
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import kotlin.random.Random


class WallpaperDownloader(context: Context, params: WorkerParameters) :
    ContextAwareWorker(context, params),
    DownloadListenerThread.DownloadListener {

    @Suppress("DEPRECATION")
    private fun downloadUsingNotificationManager(url: String, file: File): Long {
        if (url.startsWith("file://")) {
            val assetFilename = url.replace("file:///android_asset/", "")
            val thread = LocalDownloadListenerThread(context, assetFilename, file, this)
            thread.start()
            return Random(assetFilename.hashCode()).nextLong()
        } else {
            val fileUri: Uri? = Uri.fromFile(file)
            fileUri ?: return -1L

            val downloadUsingWiFiOnly = context?.preferences?.shouldDownloadOnWiFiOnly ?: true
            val allowedNetworkTypes =
                if (downloadUsingWiFiOnly) DownloadManager.Request.NETWORK_WIFI
                else (DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)

            val request = DownloadManager.Request(url.toUri())
                .apply {
                    setTitle(file.name)
                    setDescription(context?.string(R.string.downloading_wallpaper, file.name))
                    setDestinationUri(fileUri)
                    setAllowedNetworkTypes(allowedNetworkTypes)
                    setAllowedOverRoaming(!downloadUsingWiFiOnly)
                    setNotificationVisibility(
                        DownloadManager.Request.VISIBILITY_VISIBLE
                                or DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED
                    )
                    allowScanningByMediaScanner()
                }

            val downloadManager: DownloadManager? =
                context?.getSystemService(Context.DOWNLOAD_SERVICE) as? DownloadManager
            downloadManager ?: return -1L

            val downloadId = try {
                downloadManager.enqueue(request)
            } catch (e: Exception) {
                e.printStackTrace()
                return -1L
            }

            val thread =
                DownloadListenerThread(
                    context, downloadManager, downloadId, file.absolutePath, this
                )
            thread.start()
            return downloadId
        }
    }

    override suspend fun doWork(): Result = coroutineScope {
        val url: String = inputData.getString(DOWNLOAD_URL_KEY) ?: ""
        if (!url.hasContent()) return@coroutineScope Result.failure()

        val (filename, extension) = url.filenameAndExtension
        val folder = context?.preferences?.downloadsFolder
            ?: context?.externalCacheDir ?: context?.cacheDir
        val filePath = "$folder${File.separator}$filename$extension"

        val file = File(filePath)
        if (file.exists() && file.length() > 0L) {
            onSuccess(filePath)
            val outputData = workDataOf(
                DOWNLOAD_PATH_KEY to file.absolutePath,
                DOWNLOAD_FILE_EXISTED to true,
                DOWNLOAD_IS_LOCAL to url.startsWith("file://"),
            )
            return@coroutineScope Result.success(outputData)
        }

        file.parentFile?.createIfDidNotExist()
        file.delete()

        val downloadId = downloadUsingNotificationManager(url, file)
        if (downloadId == -1L) return@coroutineScope Result.failure()

        val outputData = workDataOf(
            DOWNLOAD_PATH_KEY to filePath,
            DOWNLOAD_TASK_KEY to downloadId,
            DOWNLOAD_FILE_EXISTED to false,
            DOWNLOAD_IS_LOCAL to url.startsWith("file://"),
        )
        return@coroutineScope Result.success(outputData)
    }

    override fun onSuccess(path: String) {
        super.onSuccess(path)
        try {
            MediaScanner.scan(context, path)
        } catch (_: Exception) {
        }
    }

    override fun onFailure(exception: Exception) {
        super.onFailure(exception)
        try {
            context?.toast(exception.message ?: "Unexpected error!", Toast.LENGTH_LONG)
        } catch (_: Exception) {
        }
    }

    companion object {
        internal const val DOWNLOAD_PATH_KEY = "download_path"
        internal const val DOWNLOAD_URL_KEY = "download_url"
        internal const val DOWNLOAD_TASK_KEY = "download_task"
        internal const val DOWNLOAD_FILE_EXISTED = "download_file_existed"
        internal const val DOWNLOAD_IS_LOCAL = "download_is_local"

        fun buildRequest(url: String): OneTimeWorkRequest? {
            if (!url.hasContent()) return null
            return try {
                val constraints = Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
                OneTimeWorkRequest.Builder(WallpaperDownloader::class.java)
                    .setConstraints(constraints)
                    .setInputData(
                        workDataOf(
                            DOWNLOAD_URL_KEY to url,
                            DOWNLOAD_IS_LOCAL to url.startsWith("file://")
                        )
                    )
                    .build()
            } catch (e: Exception) {
                null
            }
        }
    }
}
