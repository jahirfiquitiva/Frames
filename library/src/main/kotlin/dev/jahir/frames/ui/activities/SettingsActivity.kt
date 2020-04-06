package dev.jahir.frames.ui.activities

import android.content.SharedPreferences
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import dev.jahir.frames.R
import dev.jahir.frames.extensions.color
import dev.jahir.frames.extensions.findView
import dev.jahir.frames.extensions.resolveColor
import dev.jahir.frames.extensions.setDefaultDashboardTheme
import dev.jahir.frames.ui.activities.base.BaseThemedActivity
import dev.jahir.frames.ui.fragments.SettingsFragment
import dev.jahir.frames.utils.Prefs
import dev.jahir.frames.utils.tintIcons

open class SettingsActivity : BaseThemedActivity<Prefs>() {

    private val preferencesListener: SharedPreferences.OnSharedPreferenceChangeListener by lazy {
        SharedPreferences.OnSharedPreferenceChangeListener { _, prefKey ->
            prefKey ?: return@OnSharedPreferenceChangeListener
            when (prefKey) {
                Prefs.CURRENT_THEME -> {
                    setDefaultDashboardTheme()
                    onThemeChanged()
                }
                Prefs.USES_AMOLED_THEME, Prefs.SHOULD_COLOR_NAVBAR -> onThemeChanged()
            }
        }
    }

    override val prefs: Prefs by lazy { Prefs(this) }
    private val toolbar: Toolbar? by findView(R.id.toolbar)

    open fun getSettingsFragment(): SettingsFragment = SettingsFragment()

    private var preferenceDialog: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs.registerOnSharedPreferenceChangeListener(preferencesListener)
        setContentView(R.layout.activity_fragments)

        setSupportActionBar(toolbar)
        supportActionBar?.let {
            it.setHomeButtonEnabled(true)
            it.setDisplayHomeAsUpEnabled(true)
            it.setDisplayShowHomeEnabled(true)
        }

        toolbar?.tintIcons(resolveColor(R.attr.colorOnPrimary, color(R.color.onPrimary)))
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
        prefs.unregisterOnSharedPreferenceChangeListener(preferencesListener)
        preferenceDialog?.dismiss()
    }
}