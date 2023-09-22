package dev.jahir.frames.ui.fragments

import android.os.Build
import android.os.Bundle
import androidx.annotation.CallSuper
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.preference.Preference
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceScreen
import androidx.preference.SwitchPreferenceCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dev.jahir.frames.R
import dev.jahir.frames.data.Preferences
import dev.jahir.frames.data.viewmodels.LocalesViewModel
import dev.jahir.frames.extensions.context.boolean
import dev.jahir.frames.extensions.context.clearDataAndCache
import dev.jahir.frames.extensions.context.currentVersionCode
import dev.jahir.frames.extensions.context.currentVersionName
import dev.jahir.frames.extensions.context.dataCacheSize
import dev.jahir.frames.extensions.context.getAppName
import dev.jahir.frames.extensions.context.hasNotificationsPermission
import dev.jahir.frames.extensions.context.openLink
import dev.jahir.frames.extensions.fragments.mdDialog
import dev.jahir.frames.extensions.fragments.positiveButton
import dev.jahir.frames.extensions.fragments.preferences
import dev.jahir.frames.extensions.fragments.singleChoiceItems
import dev.jahir.frames.extensions.fragments.string
import dev.jahir.frames.extensions.fragments.title
import dev.jahir.frames.extensions.resources.hasContent
import dev.jahir.frames.extensions.utils.lazyViewModel
import dev.jahir.frames.extensions.utils.removePreference
import dev.jahir.frames.extensions.utils.setOnCheckedChangeListener
import dev.jahir.frames.extensions.utils.setOnClickListener
import dev.jahir.frames.ui.activities.SettingsActivity
import dev.jahir.frames.ui.activities.base.BasePermissionsRequestActivity
import dev.jahir.frames.ui.fragments.base.BasePreferenceFragment
import java.util.Locale

open class SettingsFragment : BasePreferenceFragment() {

    open val localesViewModel: LocalesViewModel by lazyViewModel()

    private var dashboardName: String = "Unknown"
    private var dashboardVersion: String = "-1"
    private var currentThemeKey: Int = -1

    override fun onResume() {
        super.onResume()
        localesViewModel.loadAppLocales()
    }

    private fun getCurrentLocaleName(locale: Locale): String {
        var localeName = locale.getDisplayName(locale)
        if (localeName.isEmpty()) localeName = locale.displayName
        return localeName
    }

    @CallSuper
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)

        val currentLocale = AppCompatDelegate.getApplicationLocales().get(0)
        val langPreference = findPreference<Preference?>("app_language")
        if (currentLocale != null) {
            langPreference?.summary = string(
                R.string.app_language_summary, getCurrentLocaleName(currentLocale)
            )
        }
        langPreference?.setOnClickListener {
            val locales = localesViewModel.locales
            showDialog {
                title(R.string.app_language)
                singleChoiceItems(locales.map { it.name },
                    locales.indexOfFirst { it.tag == currentLocale?.toLanguageTag() })
                positiveButton(android.R.string.ok) { dialog ->
                    val listView = (dialog as? AlertDialog)?.listView
                    if ((listView?.checkedItemCount ?: 0) > 0) {
                        val checkedItemPosition = listView?.checkedItemPosition ?: -1
                        val localeList =
                            LocaleListCompat.forLanguageTags(locales.getOrNull(checkedItemPosition)?.tag)
                        AppCompatDelegate.setApplicationLocales(
                            if (!localeList.isEmpty) localeList
                            else LocaleListCompat.getEmptyLocaleList()
                        )
                    }
                    dialog.dismiss()
                }
            }
        }

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

        val amoledPreference = findPreference<SwitchPreferenceCompat?>("use_amoled")
        amoledPreference?.isChecked = preferences.usesAmoledTheme
        amoledPreference?.setOnCheckedChangeListener { preferences.usesAmoledTheme = it }

        val useMaterialYouPref = findPreference<SwitchPreferenceCompat?>("use_material_you")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            useMaterialYouPref?.isChecked = preferences.useMaterialYou
            useMaterialYouPref?.setOnCheckedChangeListener { preferences.useMaterialYou = it }
        } else {
            findPreference<PreferenceCategory?>("interface_prefs")?.removePreference(
                useMaterialYouPref
            )
        }

        val coloredNavbarPref = findPreference<SwitchPreferenceCompat?>("colored_navigation_bar")
        coloredNavbarPref?.isChecked = preferences.shouldColorNavbar
        coloredNavbarPref?.setOnCheckedChangeListener { preferences.shouldColorNavbar = it }

        val animationsPref = findPreference<SwitchPreferenceCompat?>("interface_animations")
        animationsPref?.isChecked = preferences.animationsEnabled
        animationsPref?.setOnCheckedChangeListener { preferences.animationsEnabled = it }

        val fullResPicturesPref = findPreference<SwitchPreferenceCompat?>("full_res_previews")
        fullResPicturesPref?.isChecked = preferences.shouldLoadFullResPictures
        fullResPicturesPref?.setOnCheckedChangeListener {
            preferences.shouldLoadFullResPictures = it
        }

        val downloadOnWiFiOnlyPref = findPreference<SwitchPreferenceCompat?>("download_on_wifi_only")
        downloadOnWiFiOnlyPref?.isChecked = preferences.shouldDownloadOnWiFiOnly
        downloadOnWiFiOnlyPref?.setOnCheckedChangeListener {
            preferences.shouldDownloadOnWiFiOnly = it
        }

        val cropPicturesPrefs = findPreference<SwitchPreferenceCompat?>("crop_pictures")
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

        val notificationsPrefs = findPreference<SwitchPreferenceCompat?>("notifications")
        notificationsPrefs?.isChecked = preferences.notificationsEnabled
        notificationsPrefs?.setOnCheckedChangeListener {
            preferences.notificationsEnabled = it
            if (it && context?.hasNotificationsPermission == false) {
                (activity as? BasePermissionsRequestActivity<*>)?.requestNotificationsPermission()
            }
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
