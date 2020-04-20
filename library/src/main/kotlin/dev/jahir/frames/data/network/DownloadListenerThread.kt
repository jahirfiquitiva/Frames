package dev.jahir.frames.data.network

import android.app.DownloadManager
import android.content.Context
import android.database.CursorIndexOutOfBoundsException
import android.os.Handler
import android.os.Looper
import java.io.File
import java.lang.ref.WeakReference

class DownloadListenerThread(
    context: Context?,
    private val downloadManager: DownloadManager,
    private val downloadId: Long,
    private val downloadPath: String,
    private val downloadListener: DownloadListener? = null
) : Thread() {

    private var running: Boolean = true
    private var progress: Int = 0

    private var weakContext: WeakReference<Context?>? = null
    private val context: Context?
        get() = weakContext?.get()

    private val handler: Handler
        get() = Handler(Looper.getMainLooper())

    init {
        weakContext = WeakReference(context)
    }

    override fun run() {
        super.run()
        while (running) {
            context ?: { cancel() }()

            val query = DownloadManager.Query()
            query.setFilterById(downloadId)

            downloadManager.query(query)?.let { cursor ->
                cursor.moveToFirst()
                try {
                    when (cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))) {
                        DownloadManager.STATUS_FAILED -> {
                            handler.post { downloadListener?.onFailure(Exception("Download manager failed")) }
                            cancel()
                            return
                        }
                        DownloadManager.STATUS_SUCCESSFUL -> {
                            handler.post { downloadListener?.onSuccess(downloadPath) }
                            cancel()
                            return
                        }
                        else -> {
                        }
                    }
                    progress =
                        ((cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)) * 100L)
                                / cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))).toInt()
                } catch (e: CursorIndexOutOfBoundsException) {
                    e.printStackTrace()
                    handler.post {
                        val fileExists = File(downloadPath).exists()
                        if (fileExists) downloadListener?.onSuccess(downloadPath)
                        else {
                            downloadListener?.onFailure(
                                Exception("File was not downloaded successfully")
                            )
                        }
                    }
                    cancel()
                    return
                }

                handler.post { downloadListener?.onProgress(progress) }
                cursor.close()
            }
        }
    }

    fun cancel() {
        this.running = false
    }

    interface DownloadListener {
        fun onSuccess(path: String) {}
        fun onProgress(progress: Int) {}
        fun onFailure(exception: Exception) {
            exception.printStackTrace()
        }
    }
}