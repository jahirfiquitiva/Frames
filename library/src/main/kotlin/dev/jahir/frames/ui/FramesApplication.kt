package dev.jahir.frames.ui

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.multidex.MultiDexApplication
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.util.CoilUtils
import coil.util.DebugLogger
import dev.jahir.frames.BuildConfig
import dev.jahir.frames.extensions.context.setDefaultDashboardTheme
import okhttp3.OkHttpClient

open class FramesApplication : MultiDexApplication(), ImageLoaderFactory {
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
            .availableMemoryPercentage(0.4)
            .bitmapPoolPercentage(0.4)
            .okHttpClient {
                OkHttpClient.Builder()
                    .cache(CoilUtils.createDefaultCache(this))
                    .build()
            }
            .apply {
                if (BuildConfig.DEBUG) logger(DebugLogger())
            }
            .build()
    }
}