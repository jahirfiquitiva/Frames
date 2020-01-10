package dev.jahir.frames.extensions

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
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

inline fun Activity.restart(intentBuilder: Intent.() -> Unit = {}) {
    val i = Intent(this, this::class.java)
    intent?.extras?.let { i.putExtras(it) }
    i.intentBuilder()
    startActivity(i)
    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    finish()
    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
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

inline var Activity.statusBarLight: Boolean
    @SuppressLint("InlinedApi")
    get() {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            window.decorView.systemUiVisibility and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR > 0
        else false
    }
    @SuppressLint("InlinedApi")
    set(value) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val flags = window.decorView.systemUiVisibility
            window.decorView.systemUiVisibility =
                if (value) flags or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                else flags and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
        }
    }

inline var Window.navigationBarLight: Boolean
    @SuppressLint("InlinedApi")
    get() {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            decorView.systemUiVisibility and View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR > 0
        else false
    }
    @SuppressLint("InlinedApi")
    set(value) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val flags = decorView.systemUiVisibility
            decorView.systemUiVisibility =
                if (value) flags or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
                else flags and View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR.inv()
        }
    }

inline var Activity.navigationBarLight: Boolean
    @SuppressLint("InlinedApi")
    get() = window.navigationBarLight
    @SuppressLint("InlinedApi")
    set(value) {
        window.navigationBarLight = value
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

fun Activity?.buildTransitionOptions(transitionViews: ArrayList<View?> = ArrayList()): Array<Pair<View?, String>> {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) return arrayOf()

    val statusBar: View? by this?.window?.decorView?.findView(android.R.id.statusBarBackground)
    val navigationBar: View? by this?.window?.decorView?.findView(android.R.id.navigationBarBackground)

    val pairs = ArrayList<Pair<View, String>>()
    statusBar?.let {
        pairs.add(Pair.create(it, Window.STATUS_BAR_BACKGROUND_TRANSITION_NAME))
    }
    navigationBar?.let {
        pairs.add(Pair.create(it, Window.NAVIGATION_BAR_BACKGROUND_TRANSITION_NAME))
    }

    val appBarLayout: View? by this?.window?.decorView?.findView(R.id.appbar)
    appBarLayout?.let { pairs.add(Pair.create(it, "appbar")) }

    val bottomNavigation: View? by this?.window?.decorView?.findView(R.id.bottom_bar)
    bottomNavigation?.let { pairs.add(Pair.create(it, "bottombar")) }

    transitionViews.forEach {
        it?.let {
            val transitionName = ViewCompat.getTransitionName(it) ?: ""
            if (transitionName.hasContent()) pairs.add(Pair.create(it, transitionName))
        }
    }

    return pairs.toArray(arrayOfNulls(pairs.size))
}