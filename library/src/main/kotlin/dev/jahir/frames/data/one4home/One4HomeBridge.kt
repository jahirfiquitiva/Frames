package dev.jahir.frames.data.one4home

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import androidx.annotation.WorkerThread
import androidx.core.content.FileProvider
import dev.jahir.frames.R
import dev.jahir.frames.extensions.context.boolean
import dev.jahir.frames.extensions.context.getPackageInfoCompat
import dev.jahir.frames.extensions.context.preferences
import java.io.File

/**
 * Serves only `cache/one4home-bridge/` (see `res/xml/one4home_bridge_paths.xml`).
 * It is a subclass so it does not merge with an app's own `androidx.core.content.FileProvider`.
 */
class One4HomeBridgeFileProvider : FileProvider()

/**
 * Optional hand-off to the One4Home launcher after a static wallpaper is applied.
 *
 * Android does not let launchers read the wallpaper image, so after a successful apply to the
 * Home Screen, Frames can share the applied file with One4Home through
 * [One4HomeBridgeFileProvider]. Nothing is shared until the user connects One4Home from
 * Settings and approves it in One4Home's own consent dialog. There is no service, network call
 * or polling, and live wallpapers and Lock Screen-only applies are never shared.
 *
 * Dashboards can turn it off with `<bool name="enable_one4home_bridge">false</bool>`.
 */
object One4HomeBridge {
    private const val BRIDGE_DIRECTORY = "one4home-bridge"

    // One4Home rejects bigger images
    private const val MAX_FILE_BYTES = 64L * 1024 * 1024

    // Delivery is asynchronous and unacknowledged, so files are kept for a day
    private const val FILE_LIFETIME_MS = 24L * 60 * 60 * 1000

    fun isEnabled(context: Context): Boolean =
        context.boolean(R.bool.enable_one4home_bridge, true)

    fun isOne4HomeInstalled(context: Context): Boolean = try {
        context.packageManager.getPackageInfoCompat(One4HomeWallpaperBridge.HOME_PACKAGE)
        true
    } catch (e: Exception) {
        false
    }

    /** Whether Settings should offer to connect: enabled by the dashboard and One4Home is installed */
    fun canConnect(context: Context): Boolean = isEnabled(context) && isOne4HomeInstalled(context)

    /** Must be launched for a result: One4Home trusts the verified calling package */
    fun connectionIntent(): Intent = One4HomeWallpaperBridge.connectionIntent()

    /**
     * Shares [appliedFile] after it was applied to the Home Screen.
     * [homeWallpaperId] is the id returned by `WallpaperManager.setBitmap` with `FLAG_SYSTEM`.
     * Does nothing unless the dashboard enables the bridge and the user connected One4Home.
     * Never throws: a failed hand-off must not turn a successful apply into an error.
     */
    @WorkerThread
    fun shareAppliedWallpaper(context: Context, appliedFile: File, homeWallpaperId: Int) {
        if (homeWallpaperId <= 0) return
        if (!isEnabled(context) || !context.preferences.isOne4HomeConnected) return
        try {
            if (!isOne4HomeInstalled(context)) return
            if (!appliedFile.isFile || appliedFile.length() !in 1..MAX_FILE_BYTES) return
            val directory = File(context.cacheDir, BRIDGE_DIRECTORY).apply { mkdirs() }
            removeExpiredFiles(context, directory)
            val mimeType = appliedFile.imageMimeType()
            // A unique file per apply: One4Home reads it asynchronously
            val sharedFile = File.createTempFile("applied-", mimeType.extension, directory)
            appliedFile.copyTo(sharedFile, overwrite = true)
            One4HomeWallpaperBridge.notifyApplied(
                context, sharedFile, homeWallpaperId, mimeType = mimeType
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun removeExpiredFiles(context: Context, directory: File) {
        val expiredBefore = System.currentTimeMillis() - FILE_LIFETIME_MS
        directory.listFiles()?.filter { it.isFile && it.lastModified() < expiredBefore }
            ?.forEach { file ->
                try {
                    One4HomeWallpaperBridge.revoke(context, file)
                } catch (_: Exception) {
                }
                file.delete()
            }
    }

    private fun File.imageMimeType(): String {
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(absolutePath, options)
        return options.outMimeType?.takeIf { it.startsWith("image/") } ?: "image/jpeg"
    }

    private val String.extension: String
        get() = when (this) {
            "image/png" -> ".png"
            "image/webp" -> ".webp"
            else -> ".jpg"
        }
}
