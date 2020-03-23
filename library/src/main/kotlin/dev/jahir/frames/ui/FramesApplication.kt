package dev.jahir.frames.ui

import androidx.appcompat.app.AppCompatDelegate
import androidx.multidex.MultiDexApplication
import dev.jahir.frames.extensions.actualNightMode

open class FramesApplication : MultiDexApplication() {
    override fun onCreate() {
        AppCompatDelegate.setDefaultNightMode(actualNightMode)
        super.onCreate()
    }
}