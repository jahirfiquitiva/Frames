package dev.jahir.frames.extensions

import android.app.Activity
import android.content.res.Configuration
import android.graphics.Color
import android.os.Build
import android.view.View
import android.view.Window
import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.util.Pair
import androidx.core.view.ViewCompat
import dev.jahir.frames.R


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

fun Activity?.buildTransitionOptions(transitionViews: ArrayList<View?> = ArrayList()): Array<Pair<View?, String>>? {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
        return null
    }

    val statusBar: View? by this?.window?.decorView?.findView(android.R.id.statusBarBackground)
    val navigationBar: View? by this?.window?.decorView?.findView(android.R.id.navigationBarBackground)

    val pairs = ArrayList<Pair<View, String>>()
    pairs.add(Pair.create(statusBar, Window.STATUS_BAR_BACKGROUND_TRANSITION_NAME))
    pairs.add(Pair.create(navigationBar, Window.NAVIGATION_BAR_BACKGROUND_TRANSITION_NAME))

    val appBarLayout: View? by this?.window?.decorView?.findView(R.id.appbar)
    appBarLayout?.let { pairs.add(Pair.create(it, "appbar")) }

    val bottomNavigation: View? by this?.window?.decorView?.findView(R.id.bottom_bar)
    bottomNavigation?.let { pairs.add(Pair.create(it, "bottombar")) }

    transitionViews.forEach {
        val transitionName = it?.let { ViewCompat.getTransitionName(it) } ?: ""
        if (transitionName.hasContent()) pairs.add(Pair.create(it, transitionName))
    }

    return pairs.toArray(arrayOfNulls(pairs.size))
}