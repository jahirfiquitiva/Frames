package dev.jahir.frames.extensions

import android.app.Activity
import android.content.res.Configuration
import android.graphics.Color
import android.os.Build
import android.view.View
import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatDelegate


val Activity.isNightMode: Boolean
    get() {
        val currentNightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        return currentNightMode == Configuration.UI_MODE_NIGHT_YES
    }

fun Activity.changeNightMode(setNight: Boolean = true, force: Boolean = false) {
    AppCompatDelegate.setDefaultNightMode(
        if (force) {
            if (setNight) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        } else {
            if (setNight) {
                AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY or AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            } else {
                AppCompatDelegate.MODE_NIGHT_NO
            }
        }
    )
}

inline var Activity.navigationBarColor: Int
    get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) window.navigationBarColor else Color.BLACK
    set(value) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return
        }
        window.navigationBarColor = value
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return
        }
        var prevSystemUiVisibility = window.decorView.systemUiVisibility
        prevSystemUiVisibility = if (value.isDark) {
            prevSystemUiVisibility and View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR.inv()
        } else {
            prevSystemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
        }
        window.decorView.systemUiVisibility = prevSystemUiVisibility
    }

inline var Activity.statusBarColor: Int
    get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) window.statusBarColor else Color.BLACK
    set(value) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return
        }
        window.statusBarColor = value
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return
        }
        var prevSystemUiVisibility = window.decorView.systemUiVisibility
        prevSystemUiVisibility = if (value.isDark) {
            prevSystemUiVisibility and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
        } else {
            prevSystemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }
        window.decorView.systemUiVisibility = prevSystemUiVisibility
    }

inline fun <reified T : View> Activity.findView(@IdRes id: Int, logException: Boolean = false): Lazy<T?> {
    return lazy {
        try {
            findViewById<T>(id)
        } catch (e: Exception) {
            if (logException) e.printStackTrace()
            null
        }
    }
}