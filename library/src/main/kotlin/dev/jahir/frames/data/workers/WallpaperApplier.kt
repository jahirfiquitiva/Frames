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
import dev.jahir.frames.extensions.frames.filenameAndExtension
import dev.jahir.frames.extensions.resources.createIfDidNotExist
import dev.jahir.frames.extensions.resources.hasContent
import dev.jahir.frames.extensions.utils.ensureBackgroundThreadSuspended
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream

@Suppress("BlockingMethodInNonBlockingContext")
class WallpaperApplier(context: Context, params: WorkerParameters) :
    ContextAwareWorker(context, params) {

    private fun applyWallpaper(filePath: String, option: Int = -1): Boolean {
        if (option !in 0..2) return false

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
            var result = 0
            if (option == 0 || option == 2) {
                result +=
                    wallpaperManager.setBitmap(
                        scaledBitmap, null, true, WallpaperManager.FLAG_SYSTEM
                    )
            }
            if (option == 1 || option == 2) {
                result +=
                    wallpaperManager.setBitmap(scaledBitmap, null, true, WallpaperManager.FLAG_LOCK)
            }
            result
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

        val (_, extension) = url.filenameAndExtension
        val filePath = "${context?.cacheDir}${File.separator}to-apply$extension"

        val file = File(filePath)
        try {
            file.parentFile?.createIfDidNotExist()
            file.delete()
            withContext(IO) {
                try {
                    file.createNewFile()
                } catch (_: Exception) {
                }
                val client = OkHttpClient()
                val request = Request.Builder().url(url)
                    .build()
                val response = client.newCall(request).execute()
                val fos = FileOutputStream(file)
                fos.write(response.body?.bytes())
                fos.close()
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
        const val APPLY_EXTERNAL_KEY = 3

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
