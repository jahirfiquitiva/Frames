package dev.jahir.frames.ui

import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.disk.DiskCache
import coil3.memory.MemoryCache
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import coil3.request.allowHardware
import coil3.util.DebugLogger
import dev.jahir.frames.BuildConfig
import dev.jahir.frames.data.network.framesHttpClient
import dev.jahir.frames.extensions.context.setDefaultDashboardTheme
import okio.Path.Companion.toOkioPath

open class FramesApplication(val oneSignalAppId: String? = null) : Application(),
    SingletonImageLoader.Factory {

    override fun attachBaseContext(base: Context?) {
        base?.setDefaultDashboardTheme()
        super.attachBaseContext(base)
    }

    override fun onCreate() {
        super.onCreate()
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
    }

    override fun newImageLoader(context: Context): ImageLoader {
        return ImageLoader.Builder(context)
            .allowHardware(false)
            // Shares connections with the wallpapers JSON request and the wallpaper applier
            .components { add(OkHttpNetworkFetcherFactory(callFactory = { framesHttpClient })) }
            .memoryCache {
                MemoryCache.Builder()
                    .maxSizePercent(context, 0.3)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(context.cacheDir.resolve("image_cache").toOkioPath())
                    .maxSizePercent(0.2)
                    .build()
            }
            .apply {
                if (BuildConfig.DEBUG) logger(DebugLogger())
            }
            .build()
    }
}
