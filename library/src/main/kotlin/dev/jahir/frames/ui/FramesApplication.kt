package dev.jahir.frames.ui

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.multidex.MultiDexApplication
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.util.DebugLogger
import dev.jahir.frames.BuildConfig
import dev.jahir.frames.extensions.context.setDefaultDashboardTheme

open class FramesApplication(val oneSignalAppId: String? = null) : MultiDexApplication(),
    ImageLoaderFactory {

    override fun attachBaseContext(base: Context?) {
        base?.setDefaultDashboardTheme()
        super.attachBaseContext(base)
    }

    override fun onCreate() {
        super.onCreate()
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
    }

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .allowHardware(false)
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.3)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("image_cache"))
                    .maxSizePercent(0.2)
                    .build()
            }
            .apply {
                if (BuildConfig.DEBUG) logger(DebugLogger())
            }
            .build()
    }
}
