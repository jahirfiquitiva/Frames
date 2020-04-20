package dev.jahir.frames.data.network

import android.content.Context
import android.content.Intent
import android.media.MediaScannerConnection
import android.net.Uri
import dev.jahir.frames.extensions.resources.getUri
import dev.jahir.frames.extensions.resources.hasContent
import java.io.File
import java.net.URLConnection

internal object MediaScanner {
    private fun broadcastMediaMounted(context: Context?, uri: Uri?) {
        try {
            context?.sendBroadcast(Intent(Intent.ACTION_MEDIA_MOUNTED, uri))
        } catch (e: Exception) {
        }
    }

    @Suppress("DEPRECATION")
    private fun broadcastScanFile(context: Context?, uri: Uri?) {
        try {
            context?.sendBroadcast(
                Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE).apply { data = uri })
        } catch (e: Exception) {
        }
    }

    private fun sendBroadcasts(context: Context?, uri: Uri?) {
        broadcastMediaMounted(context, uri)
        broadcastScanFile(context, uri)
    }

    fun scan(context: Context?, path: String) {
        try {
            val file: File? = File(path)
            val fileUri: Uri? = file?.getUri(context) ?: Uri.fromFile(file)
            var mimeType = URLConnection.guessContentTypeFromName(file?.name).orEmpty()
            if (!mimeType.hasContent()) mimeType = "image/*"
            try {
                MediaScannerConnection.scanFile(
                    context, arrayOf(path), arrayOf(mimeType)
                ) { _, uri -> sendBroadcasts(context, uri) }
            } catch (e: Exception) {
                sendBroadcasts(context, fileUri)
            }
        } catch (e: Exception) {
        }
    }
}