package dev.jahir.frames.ui

import android.content.Context
import androidx.multidex.MultiDexApplication
import dev.jahir.frames.extensions.setDefaultDashboardTheme

open class FramesApplication : MultiDexApplication() {
    override fun attachBaseContext(base: Context?) {
        base?.setDefaultDashboardTheme()
        super.attachBaseContext(base)
    }
}