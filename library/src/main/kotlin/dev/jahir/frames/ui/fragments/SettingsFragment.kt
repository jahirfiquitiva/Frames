package dev.jahir.frames.ui.fragments

import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.preference.Preference
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceScreen
import androidx.preference.SwitchPreference
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dev.jahir.frames.R
import dev.jahir.frames.extensions.clearDataAndCache
import dev.jahir.frames.extensions.dataCacheSize
import dev.jahir.frames.extensions.hasContent
import dev.jahir.frames.extensions.mdDialog
import dev.jahir.frames.extensions.openLink
import dev.jahir.frames.extensions.positiveButton
import dev.jahir.frames.extensions.setOnCheckedChangeListener
import dev.jahir.frames.extensions.setOnClickListener
import dev.jahir.frames.extensions.singleChoiceItems
import dev.jahir.frames.extensions.title
import dev.jahir.frames.ui.activities.SettingsActivity
import dev.jahir.frames.ui.activities.base.BaseThemedActivity
import dev.jahir.frames.ui.fragments.base.BasePreferenceFragment
import dev.jahir.frames.utils.Prefs

open class SettingsFragment : BasePreferenceFragment<Prefs>() {

    private var currentThemeKey: Int = -1

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)

        val interfacePreferences = findPreference<PreferenceCategory?>("interface_prefs")

        currentThemeKey = getPrefs().currentTheme.value
        val themePreference = findPreference<Preference?>("app_theme")
        themePreference?.setSummary(Prefs.ThemeKey.fromValue(currentThemeKey).stringResId)
        themePreference?.setOnClickListener {
            showDialog {
                title(R.string.app_theme)
                singleChoiceItems(R.array.app_themes, currentThemeKey) { _, which ->
                    currentThemeKey = which
                }
                positiveButton(android.R.string.ok) {
                    getPrefs().currentTheme = Prefs.ThemeKey.fromValue(currentThemeKey)
                    it.dismiss()
                    (activity as? BaseThemedActivity<*>)?.onThemeChanged()
                }
            }
        }

        val amoledPreference = findPreference<SwitchPreference?>("use_amoled")
        amoledPreference?.isChecked = getPrefs().usesAmoledTheme
        amoledPreference?.setOnCheckedChangeListener {
            getPrefs().usesAmoledTheme = it
            (activity as? BaseThemedActivity<*>)?.onThemeChanged()
        }

        val coloredNavbarPref = findPreference<SwitchPreference?>("colored_navigation_bar")
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
            interfacePreferences?.removePreference(coloredNavbarPref)
        else {
            coloredNavbarPref?.isChecked = getPrefs().shouldColorNavbar
            coloredNavbarPref?.setOnCheckedChangeListener {
                getPrefs().shouldColorNavbar = it
                (activity as? BaseThemedActivity<*>)?.onThemeChanged()
            }
        }

        val animationsPref = findPreference<SwitchPreference?>("interface_animations")
        animationsPref?.isChecked = getPrefs().animationsEnabled
        animationsPref?.setOnCheckedChangeListener { getPrefs().animationsEnabled = it }

        val fullResPicturesPref = findPreference<SwitchPreference?>("full_res_previews")
        fullResPicturesPref?.isChecked = getPrefs().shouldLoadFullResPictures
        fullResPicturesPref?.setOnCheckedChangeListener {
            getPrefs().shouldLoadFullResPictures = it
        }

        val downloadLocationPref = findPreference<Preference?>("download_location")
        downloadLocationPref?.summary =
            getString(R.string.download_location_summary) + "\n${getPrefs().downloadsFolder}"

        val clearCachePref = findPreference<Preference?>("clear_data_cache")
        clearCachePref?.summary =
            getString(R.string.clear_data_cache_summary, context?.dataCacheSize ?: "")
        clearCachePref?.setOnClickListener {
            context?.clearDataAndCache()
            clearCachePref.summary =
                getString(R.string.clear_data_cache_summary, context?.dataCacheSize ?: "")
        }

        val notificationsPrefs = findPreference<SwitchPreference?>("notifications")
        notificationsPrefs?.isChecked = getPrefs().notificationsEnabled
        notificationsPrefs?.setOnCheckedChangeListener {
            getPrefs().notificationsEnabled = it
        }

        setupLegalLinks()
    }

    private fun setupLegalLinks() {
        val privacyLink = try {
            getString(R.string.privacy_policy_link)
        } catch (e: Exception) {
            ""
        }

        val termsLink = try {
            getString(R.string.terms_conditions_link)
        } catch (e: Exception) {
            ""
        }

        val prefsScreen = findPreference<PreferenceScreen?>("prefs")
        val legalCategory = findPreference<PreferenceCategory?>("legal")

        if (privacyLink.hasContent() || termsLink.hasContent()) {
            val privacyPref = findPreference<Preference?>("privacy")
            if (privacyLink.hasContent()) {
                privacyPref?.setOnClickListener {
                    try {
                        context?.openLink(privacyLink)
                    } catch (e: Exception) {
                    }
                }
            } else {
                legalCategory?.removePreference(privacyPref)
            }

            val termsPref = findPreference<Preference?>("terms")
            if (termsLink.hasContent()) {
                termsPref?.setOnClickListener {
                    try {
                        context?.openLink(termsLink)
                    } catch (e: Exception) {
                    }
                }
            } else {
                legalCategory?.removePreference(termsPref)
            }
        } else {
            prefsScreen?.removePreference(legalCategory)
        }
    }

    @Suppress("MemberVisibilityCanBePrivate")
    fun showDialog(options: MaterialAlertDialogBuilder.() -> MaterialAlertDialogBuilder): Boolean {
        showDialog(requireContext().mdDialog(options))
        return true
    }

    @Suppress("MemberVisibilityCanBePrivate")
    fun showDialog(dialog: AlertDialog?): Boolean {
        (activity as? SettingsActivity)?.showDialog(dialog)
        return true
    }

    companion object {
        internal const val TAG = "settings_fragment"
    }
}