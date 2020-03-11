package dev.jahir.frames.ui.activities.base

import android.os.Bundle
import android.os.Handler
import androidx.annotation.StyleRes
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import dev.jahir.frames.R
import dev.jahir.frames.extensions.currentNightMode
import dev.jahir.frames.extensions.getRightNavigationBarColor
import dev.jahir.frames.extensions.navigationBarColor
import dev.jahir.frames.extensions.resolveColor
import dev.jahir.frames.extensions.restart
import dev.jahir.frames.extensions.statusBarColor
import dev.jahir.frames.ui.FramesApplication
import dev.jahir.frames.utils.Prefs

abstract class BaseThemedActivity<out P : Prefs> : BaseFinishResultActivity() {

    private var lastTheme: Prefs.ThemeKey = Prefs.ThemeKey.FOLLOW_SYSTEM
    private var wasUsingAmoled: Boolean = false
    private var coloredNavbar: Boolean = false

    @StyleRes
    open fun defaultTheme(): Int = R.style.BaseFramesTheme

    @StyleRes
    open fun amoledTheme(): Int = R.style.BaseFramesTheme_Amoled

    abstract val prefs: P

    override fun onCreate(savedInstanceState: Bundle?) {
        setCustomTheme()
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
    }

    override fun onResume() {
        super.onResume()
        if (lastTheme != prefs.currentTheme || currentNightMode != prefs.lastNightMode
            || wasUsingAmoled != prefs.usesAmoledTheme || coloredNavbar != prefs.shouldColorNavbar) {
            prefs.lastNightMode = currentNightMode
            onThemeChanged()
        }
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        lastTheme = prefs.currentTheme
        wasUsingAmoled = prefs.usesAmoledTheme
        coloredNavbar = prefs.shouldColorNavbar
    }

    @Suppress("MemberVisibilityCanBePrivate")
    fun onThemeChanged() {
        (applicationContext as? FramesApplication)?.setDefaultNightMode()
        postRecreate()
    }

    private fun postRecreate() {
        Handler().post { restart() }
    }

    @Suppress("DEPRECATION")
    private fun setCustomTheme() {
        setTheme(if (prefs.usesAmoledTheme) amoledTheme() else defaultTheme())
        statusBarColor = resolveColor(
            R.attr.colorPrimaryDark,
            ContextCompat.getColor(this, R.color.primaryDark)
        )
        navigationBarColor = getRightNavigationBarColor()
    }
}