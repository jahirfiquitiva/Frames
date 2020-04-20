package dev.jahir.frames.data.network

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import dev.jahir.frames.R
import dev.jahir.frames.extensions.context.preferences
import dev.jahir.frames.extensions.context.string
import dev.jahir.frames.extensions.context.toast
import dev.jahir.frames.extensions.resources.createIfDidNotExist
import dev.jahir.frames.extensions.resources.hasContent
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.lang.ref.WeakReference
import java.net.URL
import java.net.URLConnection

class DownloadWallpaperWorker(context: Context, params: WorkerParameters) :
    Worker(context, params), DownloadListenerThread.DownloadListener {

    private var weakContext: WeakReference<Context?>? = null
    private val context: Context?
        get() = weakContext?.get()

    init {
        weakContext = WeakReference(context)
    }

    @Suppress("DEPRECATION")
    private fun downloadUsingNotificationManager(url: String, file: File): Long {
        val fileUri: Uri? = Uri.fromFile(file)
        fileUri ?: return -1L

        val request = DownloadManager.Request(Uri.parse(url))
            .apply {
                setTitle(file.name)
                setDescription(context?.string(R.string.downloading_wallpaper, file.name))
                setDestinationUri(fileUri)
                setAllowedOverRoaming(false)
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

    override fun doWork(): Result {
        val url: String = inputData.getString(DOWNLOAD_URL_KEY) ?: ""
        val toApply: Boolean = inputData.getBoolean(DOWNLOAD_TO_APPLY_KEY, false)
        if (!url.hasContent()) return Result.failure()

        val filename = url.substring(url.lastIndexOf("/") + 1)
        val filePath = if (toApply) {
            "${context?.cacheDir}${File.separator}$filename"
        } else {
            val folder = context?.preferences?.downloadsFolder
                ?: context?.externalCacheDir ?: context?.cacheDir
            "$folder${File.separator}$filename"
        }

        val file = File(filePath)
        if (!toApply && file.exists() && file.length() > 0L) {
            onSuccess(filePath)
            val outputData = workDataOf(
                DOWNLOAD_PATH_KEY to file.absolutePath,
                DOWNLOAD_TO_APPLY_KEY to toApply,
                DOWNLOAD_FILE_EXISTED to true
            )
            return Result.success(outputData)
        }

        file.parentFile?.createIfDidNotExist()
        file.delete()

        val downloadId =
            if (!toApply) downloadUsingNotificationManager(url, file) else 0
        if (downloadId == -1L) return Result.failure()

        if (toApply && downloadId == 0L) {
            try {
                val downloadURL = URL(url)
                val urlConnection: URLConnection = downloadURL.openConnection()
                urlConnection.connect()
                // val fileLength: Int = urlConnection.contentLength
                val fos = FileOutputStream(file)
                val inputStream: InputStream = urlConnection.getInputStream()
                val buffer = ByteArray(1024)
                var len1: Int
                var total: Long = 0
                while (inputStream.read(buffer).also { len1 = it } > 0) {
                    total += len1.toLong()
                    // val percentage = (total * 100 / fileLength).toInt()
                    // liveDataHelper?.updateDownloadPer(percentage)
                    fos.write(buffer, 0, len1)
                }
                fos.close()
                inputStream.close()
            } catch (e: Exception) {
                e.printStackTrace()
                return Result.failure()
            }
        }

        val outputData = workDataOf(
            DOWNLOAD_PATH_KEY to filePath,
            DOWNLOAD_TO_APPLY_KEY to toApply,
            DOWNLOAD_TASK_KEY to downloadId,
            DOWNLOAD_FILE_EXISTED to false
        )
        return Result.success(outputData)
    }

    override fun onSuccess(path: String) {
        super.onSuccess(path)
        MediaScanner.scan(context, path)
    }

    override fun onFailure(exception: Exception) {
        super.onFailure(exception)
        try {
            context?.toast(exception.message ?: "Unexpected error!", Toast.LENGTH_LONG)
        } catch (e: Exception) {
        }
    }

    companion object {
        internal const val DOWNLOAD_PATH_KEY = "download_path"
        internal const val DOWNLOAD_URL_KEY = "download_url"
        internal const val DOWNLOAD_TO_APPLY_KEY = "download_to_apply"
        internal const val DOWNLOAD_TASK_KEY = "download_task"
        internal const val DOWNLOAD_FILE_EXISTED = "download_file_existed"

        fun buildRequest(url: String, toApply: Boolean = false): OneTimeWorkRequest? {
            if (!url.hasContent()) return null
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
            val data = workDataOf(DOWNLOAD_URL_KEY to url, DOWNLOAD_TO_APPLY_KEY to toApply)
            return OneTimeWorkRequest.Builder(DownloadWallpaperWorker::class.java)
                .setConstraints(constraints)
                .setInputData(data)
                .build()
        }
    }
}