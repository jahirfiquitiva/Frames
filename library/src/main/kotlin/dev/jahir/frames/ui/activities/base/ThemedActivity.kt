package dev.jahir.frames.ui.activities.base

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import androidx.annotation.ColorInt
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import dev.jahir.frames.R
import dev.jahir.frames.extensions.isDark
import dev.jahir.frames.extensions.navigationBarColor
import dev.jahir.frames.extensions.navigationBarLight
import dev.jahir.frames.extensions.prefs
import dev.jahir.frames.extensions.restart
import dev.jahir.frames.extensions.statusBarColor
import dev.jahir.frames.extensions.statusBarLight

abstract class ThemedActivity : AppCompatActivity() {

    private var coloredNavbar = false

    override fun onCreate(savedInstanceState: Bundle?) {
        setCustomTheme()
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
    }

    override fun onResume() {
        super.onResume()
        if (coloredNavbar != prefs.shouldColorNavbar)
            onThemeChanged()
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        coloredNavbar = prefs.shouldColorNavbar
    }

    @Suppress("MemberVisibilityCanBePrivate")
    fun onThemeChanged() {
        postRecreate()
    }

    private fun postRecreate() {
        Handler().post { restart() }
    }

    @Suppress("DEPRECATION")
    private fun setCustomTheme() {
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        statusBarColor = ContextCompat.getColor(this, R.color.primaryDark)
        statusBarLight = !statusBarColor.isDark
        val navColor = getCorrectNavbarColor()
        navigationBarColor = navColor
        navigationBarLight = !navColor.isDark
    }

    @ColorInt
    private fun getCorrectNavbarColor(): Int {
        return if (prefs.shouldColorNavbar) {
            try {
                ContextCompat.getColor(this, R.color.surface)
            } catch (e: Exception) {
                Color.parseColor("#000000")
            }
        } else {
            Color.parseColor("#000000")
        }
    }
}