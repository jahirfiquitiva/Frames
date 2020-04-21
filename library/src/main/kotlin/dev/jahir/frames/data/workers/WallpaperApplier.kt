package dev.jahir.frames.data.workers

import android.app.WallpaperManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import dev.jahir.frames.extensions.context.preferences
import dev.jahir.frames.extensions.resources.createIfDidNotExist
import dev.jahir.frames.extensions.resources.hasContent
import dev.jahir.frames.extensions.utils.ensureBackgroundThreadSuspended
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.net.URL
import java.net.URLConnection

class WallpaperApplier(context: Context, params: WorkerParameters) :
    ContextAwareWorker(context, params) {

    private fun applyWallpaper(filePath: String, option: Int = -1): Boolean {
        if (option !in 0..2) return false

        val applyFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            when (option) {
                0 -> WallpaperManager.FLAG_SYSTEM
                1 -> WallpaperManager.FLAG_LOCK
                else -> -1
            }
        } else -1

        val bitmap = try {
            BitmapFactory.decodeFile(filePath)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
        bitmap ?: return false

        val wallpaperManager = try {
            WallpaperManager.getInstance(context)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
        wallpaperManager ?: return false

        val scaledBitmap = if (context?.preferences?.shouldCropWallpaperBeforeApply == true) {
            try {
                val wantedHeight = wallpaperManager.desiredMinimumHeight
                val ratio = wantedHeight / bitmap.height.toFloat()
                val wantedWidth = (bitmap.width * ratio).toInt()
                Bitmap.createScaledBitmap(bitmap, wantedWidth, wantedHeight, true)
            } catch (e: Exception) {
                bitmap
            }
        } else bitmap
        scaledBitmap ?: return false

        val result = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            if (applyFlag >= 0) {
                wallpaperManager.setBitmap(scaledBitmap, null, true, applyFlag)
            } else wallpaperManager.setBitmap(scaledBitmap, null, true)
        } else {
            wallpaperManager.setBitmap(scaledBitmap)
            -1
        }
        return result != 0
    }

    override suspend fun doWork(): Result = coroutineScope {
        val url: String = inputData.getString(WallpaperDownloader.DOWNLOAD_URL_KEY) ?: ""
        val applyOption: Int = inputData.getInt(APPLY_OPTION_KEY, -1)
        if (!url.hasContent()) return@coroutineScope Result.failure()
        if (applyOption < 0) return@coroutineScope Result.failure()

        val filename = url.substring(url.lastIndexOf("/") + 1)
        val filePath = "${context?.cacheDir}${File.separator}$filename"

        val file = File(filePath)
        try {
            file.parentFile?.createIfDidNotExist()
            file.delete()
            withContext(IO) {
                try {
                    file.createNewFile()
                } catch (e: Exception) {
                }
                val downloadURL = URL(url)
                val urlConnection: URLConnection = downloadURL.openConnection().apply { connect() }
                val fos = FileOutputStream(file)
                val inputStream: InputStream = urlConnection.getInputStream()
                val buffer = ByteArray(1024)
                var len1: Int
                var total: Long = 0
                while (inputStream.read(buffer).also { len1 = it } > 0) {
                    total += len1.toLong()
                    fos.write(buffer, 0, len1)
                }
                fos.close()
                inputStream.close()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return@coroutineScope Result.failure()
        }

        val outputData = workDataOf(
            WallpaperDownloader.DOWNLOAD_PATH_KEY to filePath,
            APPLY_OPTION_KEY to applyOption
        )
        if (applyOption == APPLY_EXTERNAL_KEY) return@coroutineScope Result.success(outputData)
        var success = false
        ensureBackgroundThreadSuspended {
            success = applyWallpaper(filePath, applyOption)
        }
        return@coroutineScope if (success) Result.success(outputData) else Result.failure()
    }

    companion object {
        internal const val APPLY_OPTION_KEY = "apply_option_key"
        internal const val APPLY_EXTERNAL_KEY = 3

        fun buildRequest(url: String, applyOption: Int = -1): OneTimeWorkRequest? {
            if (!url.hasContent()) return null
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
            val data = workDataOf(
                WallpaperDownloader.DOWNLOAD_URL_KEY to url,
                APPLY_OPTION_KEY to applyOption
            )
            return OneTimeWorkRequest.Builder(WallpaperApplier::class.java)
                .setConstraints(constraints)
                .setInputData(data)
                .build()
        }
    }

}