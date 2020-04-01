package dev.jahir.frames.ui.activities.base

import android.os.Bundle
import android.util.Log
import androidx.annotation.IdRes
import androidx.annotation.StyleRes
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import dev.jahir.frames.R
import dev.jahir.frames.extensions.getRightNavigationBarColor
import dev.jahir.frames.extensions.isDark
import dev.jahir.frames.extensions.navigationBarColor
import dev.jahir.frames.extensions.navigationBarLight
import dev.jahir.frames.extensions.resolveColor
import dev.jahir.frames.extensions.restart
import dev.jahir.frames.extensions.statusBarColor
import dev.jahir.frames.extensions.statusBarLight
import dev.jahir.frames.utils.Prefs
import dev.jahir.frames.utils.postDelayed

abstract class BaseThemedActivity<out P : Prefs> : BaseFinishResultActivity() {

    private var wasUsingAmoled: Boolean = false
    private var coloredNavbar: Boolean = false

    @StyleRes
    open fun defaultTheme(): Int = R.style.BaseFramesTheme

    @StyleRes
    open fun amoledTheme(): Int = R.style.BaseFramesTheme_Amoled

    abstract val prefs: P

    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        setCustomTheme()
        super.onCreate(savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
        if (wasUsingAmoled != prefs.usesAmoledTheme
            || coloredNavbar != prefs.shouldColorNavbar)
            onThemeChanged()
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        wasUsingAmoled = prefs.usesAmoledTheme
        coloredNavbar = prefs.shouldColorNavbar
    }

    internal fun onThemeChanged() {
        delegate.applyDayNight()
        postDelayed(2) { restart() }
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

    fun replaceFragment(
        fragment: Fragment?,
        fragmentTag: String = "fragment",
        @IdRes fragmentContainerId: Int = R.id.fragments_container
    ) {
        fragment ?: return
        try {
            supportFragmentManager.beginTransaction()
                .replace(fragmentContainerId, fragment, fragmentTag)
                .commit()
        } catch (e: Exception) {
            Log.e("FragmentTransaction", e.message, e)
        }
    }

    open val shouldChangeStatusBarLightStatus: Boolean = true
    open val shouldChangeNavigationBarLightStatus: Boolean = true
}