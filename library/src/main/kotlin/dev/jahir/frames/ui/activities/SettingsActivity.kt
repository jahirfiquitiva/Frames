package dev.jahir.frames.ui.activities

import android.content.SharedPreferences
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import dev.jahir.frames.BuildConfig
import dev.jahir.frames.R
import dev.jahir.frames.data.Preferences
import dev.jahir.frames.extensions.context.findView
import dev.jahir.frames.extensions.context.setDefaultDashboardTheme
import dev.jahir.frames.extensions.views.tint
import dev.jahir.frames.ui.activities.base.BaseThemedActivity
import dev.jahir.frames.ui.fragments.SettingsFragment

open class SettingsActivity : BaseThemedActivity<Preferences>() {

    private val preferencesListener: SharedPreferences.OnSharedPreferenceChangeListener by lazy {
        SharedPreferences.OnSharedPreferenceChangeListener { _, prefKey ->
            prefKey ?: return@OnSharedPreferenceChangeListener
            when (prefKey) {
                Preferences.CURRENT_THEME -> {
                    setDefaultDashboardTheme()
                    onThemeChanged()
                }
                Preferences.USES_AMOLED_THEME,
                Preferences.MATERIAL_YOU_ENABLED,
                Preferences.SHOULD_COLOR_NAVBAR -> onThemeChanged()
            }
        }
    }

    override val preferences: Preferences by lazy { Preferences(this) }
    private val toolbar: Toolbar? by findView(R.id.toolbar)

    open fun getSettingsFragment(): SettingsFragment =
        SettingsFragment.create(dashboardName, dashboardVersion)

    private var preferenceDialog: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        preferences.registerOnSharedPreferenceChangeListener(preferencesListener)
        setContentView(R.layout.activity_fragments)

        setSupportActionBar(toolbar)
        supportActionBar?.let {
            it.setHomeButtonEnabled(true)
            it.setDisplayHomeAsUpEnabled(true)
            it.setDisplayShowHomeEnabled(true)
        }
        toolbar?.tint()

        replaceFragment(getSettingsFragment(), SettingsFragment.TAG)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) supportFinishAfterTransition()
        return super.onOptionsItemSelected(item)
    }

    fun showDialog(dialog: AlertDialog?) {
        preferenceDialog?.dismiss()
        preferenceDialog = dialog
        preferenceDialog?.show()
    }

    override fun onDestroy() {
        super.onDestroy()
        preferences.unregisterOnSharedPreferenceChangeListener(preferencesListener)
        preferenceDialog?.dismiss()
    }

    open val dashboardName: String = BuildConfig.DASHBOARD_NAME
    open val dashboardVersion: String = BuildConfig.DASHBOARD_VERSION
}
