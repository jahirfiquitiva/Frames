package dev.jahir.frames.ui.activities.base

import com.tonyodev.fetch2.Fetch
import com.tonyodev.fetch2.FetchConfiguration
import com.tonyodev.fetch2.NetworkType
import com.tonyodev.fetch2.Priority
import com.tonyodev.fetch2.Request
import com.tonyodev.fetch2core.Func
import dev.jahir.frames.data.models.Wallpaper
import dev.jahir.frames.extensions.getWallpapersDownloadFolder
import dev.jahir.frames.utils.WallpaperDownloadNotificationManager
import java.io.File
import java.lang.ref.WeakReference


abstract class BaseWallpaperFetcherActivity : BaseStoragePermissionRequestActivity() {

    private val fetch: Fetch by lazy {
        val fetchConfig = FetchConfiguration.Builder(this)
            .setDownloadConcurrentLimit(2)
            .setNotificationManager(object :
                WallpaperDownloadNotificationManager(WeakReference(this@BaseWallpaperFetcherActivity)) {
                override fun getFetchInstanceForNamespace(namespace: String): Fetch = fetch
            })
            .build()
        Fetch.Impl.getInstance(fetchConfig)
    }

    private var request: Request? = null

    internal fun initFetch(wallpaper: Wallpaper?) {
        wallpaper ?: return
        val folder = getWallpapersDownloadFolder() ?: externalCacheDir ?: cacheDir
        val filename = wallpaper.url.substring(wallpaper.url.lastIndexOf("/") + 1)

        request = Request(wallpaper.url, "$folder${File.separator}$filename")
        request?.priority = Priority.HIGH
        // TODO: Allow WiFi only downloads
        request?.networkType = NetworkType.ALL
        request?.addHeader(
            WallpaperDownloadNotificationManager.INTERNAL_FRAMES_WALLPAPER_HEADER,
            wallpaper.name
        )
    }

    internal fun startDownload() {
        request?.let { fetch.enqueue(it, Func { }, Func { }) }
    }

    internal fun cancelDownload() {
        fetch.cancel(request?.id ?: 0)
        fetch.cancelAll()
    }
}