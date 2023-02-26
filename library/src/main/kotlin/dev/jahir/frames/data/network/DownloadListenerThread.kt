package dev.jahir.frames.data.network

import android.app.DownloadManager
import android.content.Context
import android.database.Cursor
import android.database.CursorIndexOutOfBoundsException
import android.os.Handler
import dev.jahir.frames.extensions.utils.SafeHandler
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
        get() = SafeHandler()

    init {
        weakContext = WeakReference(context)
    }

    override fun run() {
        super.run()
        while (running) {
            context ?: run { cancel() }

            val query = DownloadManager.Query()
            query.setFilterById(downloadId)

            downloadManager.query(query)?.let { cursor ->
                cursor.moveToFirst()
                try {
                    when (cursor.getInt(DownloadManager.COLUMN_STATUS)) {
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
                    }
                    val bytesDownloaded =
                        cursor.getInt(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)
                    val totalBytes = cursor.getInt(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)
                    progress = ((bytesDownloaded * 100L) / totalBytes).toInt()
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

    private fun Cursor.getInt(columnName: String? = null): Int {
        columnName ?: return 0
        val columnIndex = getColumnIndex(columnName)
        if (columnIndex < 0) return 0
        return getInt(columnIndex)
    }
}
