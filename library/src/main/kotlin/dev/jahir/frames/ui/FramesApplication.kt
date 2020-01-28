package dev.jahir.frames.ui

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import dev.jahir.frames.extensions.prefs
import dev.jahir.frames.utils.Prefs

open class FramesApplication : Application() {
    override fun onCreate() {
        setDefaultNightMode()
        super.onCreate()
    }

    internal fun setDefaultNightMode() {
        AppCompatDelegate.setDefaultNightMode(getNightMode())
    }

    private fun getNightMode(): Int = when (prefs.currentTheme) {
        Prefs.ThemeKey.LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
        Prefs.ThemeKey.DARK -> AppCompatDelegate.MODE_NIGHT_YES
        else -> AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY or AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
    }
}