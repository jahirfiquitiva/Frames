package dev.jahir.frames.ui.fragments

import android.os.Bundle
import androidx.annotation.CallSuper
import androidx.appcompat.app.AlertDialog
import androidx.preference.Preference
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceScreen
import androidx.preference.SwitchPreference
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dev.jahir.frames.R
import dev.jahir.frames.data.Preferences
import dev.jahir.frames.extensions.context.boolean
import dev.jahir.frames.extensions.context.clearDataAndCache
import dev.jahir.frames.extensions.context.currentVersionCode
import dev.jahir.frames.extensions.context.currentVersionName
import dev.jahir.frames.extensions.context.dataCacheSize
import dev.jahir.frames.extensions.context.getAppName
import dev.jahir.frames.extensions.context.openLink
import dev.jahir.frames.extensions.fragments.mdDialog
import dev.jahir.frames.extensions.fragments.positiveButton
import dev.jahir.frames.extensions.fragments.preferences
import dev.jahir.frames.extensions.fragments.singleChoiceItems
import dev.jahir.frames.extensions.fragments.string
import dev.jahir.frames.extensions.fragments.title
import dev.jahir.frames.extensions.resources.hasContent
import dev.jahir.frames.extensions.utils.setOnCheckedChangeListener
import dev.jahir.frames.extensions.utils.setOnClickListener
import dev.jahir.frames.ui.activities.SettingsActivity
import dev.jahir.frames.ui.fragments.base.BasePreferenceFragment

open class SettingsFragment : BasePreferenceFragment() {

    private var dashboardName: String = "Unknown"
    private var dashboardVersion: String = "-1"
    private var currentThemeKey: Int = -1

    @CallSuper
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)

        currentThemeKey = preferences.currentTheme.value
        val themePreference = findPreference<Preference?>("app_theme")
        themePreference?.setSummary(Preferences.ThemeKey.fromValue(currentThemeKey).stringResId)
        themePreference?.setOnClickListener {
            showDialog {
                title(R.string.app_theme)
                singleChoiceItems(R.array.app_themes, currentThemeKey)
                positiveButton(android.R.string.ok) { dialog ->
                    val listView = (dialog as? AlertDialog)?.listView
                    if ((listView?.checkedItemCount ?: 0) > 0) {
                        val checkedItemPosition = listView?.checkedItemPosition ?: -1
                        currentThemeKey = checkedItemPosition
                        preferences.currentTheme = Preferences.ThemeKey.fromValue(currentThemeKey)
                    }
                    dialog.dismiss()
                }
            }
        }

        val amoledPreference = findPreference<SwitchPreference?>("use_amoled")
        amoledPreference?.isChecked = preferences.usesAmoledTheme
        amoledPreference?.setOnCheckedChangeListener { preferences.usesAmoledTheme = it }

        val coloredNavbarPref = findPreference<SwitchPreference?>("colored_navigation_bar")
        coloredNavbarPref?.isChecked = preferences.shouldColorNavbar
        coloredNavbarPref?.setOnCheckedChangeListener { preferences.shouldColorNavbar = it }

        val animationsPref = findPreference<SwitchPreference?>("interface_animations")
        animationsPref?.isChecked = preferences.animationsEnabled
        animationsPref?.setOnCheckedChangeListener { preferences.animationsEnabled = it }

        val fullResPicturesPref = findPreference<SwitchPreference?>("full_res_previews")
        fullResPicturesPref?.isChecked = preferences.shouldLoadFullResPictures
        fullResPicturesPref?.setOnCheckedChangeListener {
            preferences.shouldLoadFullResPictures = it
        }

        val downloadOnMobilePref = findPreference<SwitchPreference?>("download_on_mobile")
        downloadOnMobilePref?.isChecked = preferences.shouldDownloadOnMobile
        downloadOnMobilePref?.setOnCheckedChangeListener {
            preferences.shouldDownloadOnMobile = it
        }

        val cropPicturesPrefs = findPreference<SwitchPreference?>("crop_pictures")
        cropPicturesPrefs?.isChecked = preferences.shouldCropWallpaperBeforeApply
        cropPicturesPrefs?.setOnCheckedChangeListener {
            preferences.shouldCropWallpaperBeforeApply = it
        }

        val downloadLocationPref = findPreference<Preference?>("download_location")
        downloadLocationPref?.summary = preferences.downloadsFolder.toString()

        val clearCachePref = findPreference<Preference?>("clear_data_cache")
        clearCachePref?.summary =
            string(R.string.clear_data_cache_summary, context?.dataCacheSize ?: "")
        clearCachePref?.setOnClickListener {
            context?.clearDataAndCache()
            clearCachePref.summary =
                string(R.string.clear_data_cache_summary, context?.dataCacheSize ?: "")
        }

        val notificationsPrefs = findPreference<SwitchPreference?>("notifications")
        notificationsPrefs?.isChecked = preferences.notificationsEnabled
        notificationsPrefs?.setOnCheckedChangeListener {
            preferences.notificationsEnabled = it
        }

        if (context?.boolean(R.bool.show_versions_in_settings, true) == true) {
            val appVersionPrefs = findPreference<Preference?>("app_version")
            appVersionPrefs?.title = context?.getAppName()
            appVersionPrefs?.summary =
                "${context?.currentVersionName} (${context?.currentVersionCode})"

            val dashboardVersionPrefs = findPreference<Preference?>("dashboard_version")
            dashboardVersionPrefs?.title = dashboardName
            dashboardVersionPrefs?.summary = dashboardVersion
        } else {
            preferenceScreen?.removePreference(findPreference("versions"))
        }

        setupLegalLinks()
    }

    @Suppress("RemoveExplicitTypeArguments")
    private fun setupLegalLinks() {
        val privacyLink = string(R.string.privacy_policy_link)
        val termsLink = string(R.string.terms_conditions_link)

        val prefsScreen = findPreference<PreferenceScreen?>("preferences")
            ?: findPreference<PreferenceScreen?>("prefs")
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

        fun create(dashboardName: String, dashboardVersion: String) = SettingsFragment().apply {
            this.dashboardName = dashboardName
            this.dashboardVersion = dashboardVersion
        }
    }
}
