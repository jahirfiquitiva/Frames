package dev.jahir.frames.extensions

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.view.View
import android.view.Window
import androidx.annotation.IdRes
import androidx.core.util.Pair
import androidx.core.view.ViewCompat
import dev.jahir.frames.R

inline fun Activity.restart(intentBuilder: Intent.() -> Unit = {}) {
    val i = Intent(this, this::class.java)
    intent?.extras?.let { i.putExtras(it) }
    i.intentBuilder()
    startActivity(i)
    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    finish()
    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
}

inline var Activity.statusBarColor: Int
    get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) window.statusBarColor else Color.BLACK
    set(value) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) return
        window.statusBarColor = value
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

inline var Activity.navigationBarColor: Int
    get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) window.navigationBarColor else Color.BLACK
    set(value) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) return
        window.navigationBarColor = value
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

inline fun <reified T : View> Activity.findView(
    @IdRes id: Int,
    logException: Boolean = false
): Lazy<T?> {
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

    val statusBar: View? = this?.window?.decorView?.findViewById(android.R.id.statusBarBackground)
    val navigationBar: View? =
        this?.window?.decorView?.findViewById(android.R.id.navigationBarBackground)

    val pairs = ArrayList<Pair<View, String>>()
    statusBar?.let {
        pairs.add(Pair.create(it, Window.STATUS_BAR_BACKGROUND_TRANSITION_NAME))
    }
    navigationBar?.let {
        pairs.add(Pair.create(it, Window.NAVIGATION_BAR_BACKGROUND_TRANSITION_NAME))
    }

    val appBarLayout: View? = this?.window?.decorView?.findViewById(R.id.appbar)
    appBarLayout?.let { pairs.add(Pair.create(it, "appbar")) }

    val bottomNavigation: View? = this?.window?.decorView?.findViewById(R.id.bottom_navigation)
    bottomNavigation?.let { pairs.add(Pair.create(it, "bottombar")) }

    transitionViews.forEach {
        it?.let {
            val transitionName = ViewCompat.getTransitionName(it) ?: ""
            if (transitionName.hasContent()) pairs.add(Pair.create(it, transitionName))
        }
    }

    return pairs.toArray(arrayOfNulls(pairs.size))
}