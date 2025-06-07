package dev.jahir.frames.data.network

import android.content.Context
import android.media.MediaScannerConnection
import android.os.Handler
import android.util.Log
import dev.jahir.frames.extensions.utils.SafeHandler
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.lang.ref.WeakReference

private const val BUFFER_SIZE = 2048

class LocalDownloadListenerThread(
    context: Context?,
    private val assetFilename: String,
    private val targetFile: File?,
    private val downloadListener: DownloadListenerThread.DownloadListener? = null
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
            targetFile ?: run { cancel() }
            var assetInputStream: InputStream? = context?.assets?.open(assetFilename)
            if (assetInputStream !== null) {
                try {
                    var out: OutputStream? = FileOutputStream(targetFile)
                    val buffer = ByteArray(BUFFER_SIZE)
                    var read: Int
                    val size: Int = assetInputStream.available()
                    var alreadyCopied: Long = 0
                    while ((assetInputStream.read(buffer).also { read = it }) != -1) {
                        alreadyCopied += read
                        out?.write(buffer, 0, read)
                        handler.post {
                            downloadListener?.onProgress(
                                Math.round(1.0F * alreadyCopied / size)
                            )
                        }
                    }

                    assetInputStream.close()
                    assetInputStream = null
                    out?.flush()
                    out?.close()
                    out = null

                    MediaScannerConnection.scanFile(
                        context,
                        arrayOf(targetFile?.absolutePath),
                        null
                    ) { path, _ ->
                        handler.post {
                            val fileExists = File(path).exists()
                            if (fileExists) downloadListener?.onSuccess(path)
                            else {
                                downloadListener?.onFailure(
                                    Exception("File was not saved successfully!")
                                )
                            }
                        }
                        cancel()
                    }
                } catch (e: IOException) {
                    Log.e("Frames", "Failed to copy asset file: $assetFilename", e)
                    handler.post { downloadListener?.onFailure(Exception("Failed to save file!")) }
                    cancel()
                }
            }
            cancel()
        }
    }

    fun cancel() {
        this.running = false
    }
}
