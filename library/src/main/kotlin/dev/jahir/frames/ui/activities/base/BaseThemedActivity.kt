package dev.jahir.frames.ui.activities.base

import android.os.Bundle
import android.os.Handler
import androidx.annotation.StyleRes
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import dev.jahir.frames.R
import dev.jahir.frames.extensions.actualNightMode
import dev.jahir.frames.extensions.currentNightMode
import dev.jahir.frames.extensions.getRightNavigationBarColor
import dev.jahir.frames.extensions.isDark
import dev.jahir.frames.extensions.navigationBarColor
import dev.jahir.frames.extensions.navigationBarLight
import dev.jahir.frames.extensions.resolveColor
import dev.jahir.frames.extensions.restart
import dev.jahir.frames.extensions.statusBarColor
import dev.jahir.frames.extensions.statusBarLight
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
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        AppCompatDelegate.setDefaultNightMode(actualNightMode)
        setCustomTheme()
        super.onCreate(savedInstanceState)
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
        Handler().post { restart() }
    }

    @Suppress("DEPRECATION")
    private fun setCustomTheme() {
        setTheme(if (prefs.usesAmoledTheme) amoledTheme() else defaultTheme())
        resolveColor(
            R.attr.colorPrimaryDark,
            ContextCompat.getColor(this, R.color.primaryDark)
        ).let {
            statusBarColor = it
            if (shouldChangeStatusBarLightStatus)
                statusBarLight = !it.isDark
        }
        getRightNavigationBarColor().let {
            navigationBarColor = it
            if (shouldChangeNavigationBarLightStatus)
                navigationBarLight = !it.isDark
        }
    }

    open val shouldChangeStatusBarLightStatus: Boolean = true
    open val shouldChangeNavigationBarLightStatus: Boolean = true
}