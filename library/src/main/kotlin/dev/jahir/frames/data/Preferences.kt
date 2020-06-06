package dev.jahir.frames.data

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import androidx.annotation.StringRes
import dev.jahir.frames.R
import dev.jahir.frames.extensions.context.boolean
import dev.jahir.frames.extensions.context.integer
import dev.jahir.frames.extensions.resources.createIfDidNotExist
import dev.jahir.frames.extensions.resources.getDefaultWallpapersDownloadFolder
import java.io.File

@Suppress("MemberVisibilityCanBePrivate")
open class Preferences(private val context: Context) {
    val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    @SuppressLint("CommitPrefEdits")
    val prefsEditor: SharedPreferences.Editor = prefs.edit()

    var lastVersion: Long
        get() = prefs.getLong(LAST_VERSION, -1L)
        set(value) = prefsEditor.putLong(LAST_VERSION, value).apply()

    var currentTheme: ThemeKey
        get() = ThemeKey.fromValue(
            prefs.getInt(
                CURRENT_THEME,
                context.integer(R.integer.default_theme, getDefaultThemeKey().value)
            )
        )
        set(value) = prefsEditor.putInt(CURRENT_THEME, value.value).apply()

    var usesAmoledTheme: Boolean
        get() = prefs.getBoolean(
            USES_AMOLED_THEME,
            context.boolean(R.bool.amoled_theme_enabled_by_default)
        )
        set(value) = prefsEditor.putBoolean(USES_AMOLED_THEME, value).apply()

    var shouldColorNavbar: Boolean
        get() = prefs.getBoolean(SHOULD_COLOR_NAVBAR, true)
        set(value) = prefsEditor.putBoolean(SHOULD_COLOR_NAVBAR, value).apply()

    var shouldLoadFullResPictures: Boolean
        get() = prefs.getBoolean(SHOULD_LOAD_FULL_RES_PICTURES, false)
        set(value) = prefsEditor.putBoolean(SHOULD_LOAD_FULL_RES_PICTURES, value).apply()

    var shouldCropWallpaperBeforeApply: Boolean
        get() = prefs.getBoolean(SHOULD_CROP_WALLPAPER_BEFORE_APPLY, true)
        set(value) = prefsEditor.putBoolean(SHOULD_CROP_WALLPAPER_BEFORE_APPLY, value).apply()

    var animationsEnabled: Boolean
        get() = prefs.getBoolean(
            ANIMATIONS_ENABLED,
            context.boolean(R.bool.animations_enabled_by_default, true)
        )
        set(value) = prefsEditor.putBoolean(ANIMATIONS_ENABLED, value).apply()

    var downloadsFolder: File?
        get() {
            val file = File(
                prefs.getString(
                    DOWNLOADS_FOLDER,
                    context.getDefaultWallpapersDownloadFolder().toString()
                ) ?: context.externalCacheDir.toString()
            )
            file.createIfDidNotExist()
            return file
        }
        set(value) = prefsEditor.putString(DOWNLOADS_FOLDER, value.toString()).apply()

    var functionalDashboard: Boolean
        get() = prefs.getBoolean(FUNCTIONAL_DASHBOARD, false)
        set(value) = prefsEditor.putBoolean(FUNCTIONAL_DASHBOARD, value).apply()

    var notificationsEnabled: Boolean
        get() = prefs.getBoolean(
            NOTIFICATIONS_ENABLED,
            context.boolean(R.bool.notifications_enabled_by_default, true)
        )
        set(value) = prefsEditor.putBoolean(NOTIFICATIONS_ENABLED, value).apply()

    var refreshMuzeiOnWiFiOnly: Boolean
        get() = prefs.getBoolean(REFRESH_MUZEI_ON_WIFI_ONLY, false)
        set(value) = prefsEditor.putBoolean(REFRESH_MUZEI_ON_WIFI_ONLY, value).apply()

    var muzeiCollections: String
        get() = prefs.getString(MUZEI_COLLECTIONS, "").orEmpty()
        set(value) = prefsEditor.putString(MUZEI_COLLECTIONS, value).apply()

    open fun getDefaultThemeKey(): ThemeKey = ThemeKey.FOLLOW_SYSTEM

    fun registerOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) {
        prefs.registerOnSharedPreferenceChangeListener(listener)
    }

    fun unregisterOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) {
        prefs.unregisterOnSharedPreferenceChangeListener(listener)
    }

    enum class ThemeKey(val value: Int, @StringRes val stringResId: Int) {
        LIGHT(0, R.string.light_theme),
        DARK(1, R.string.dark_theme),
        FOLLOW_SYSTEM(2, R.string.follow_system_theme);

        companion object {
            fun fromValue(value: Int): ThemeKey = when (value) {
                0 -> LIGHT
                1 -> DARK
                else -> FOLLOW_SYSTEM
            }
        }
    }

    companion object {
        private const val PREFS_NAME = "jfdb_confs"
        private const val LAST_VERSION = "last_version"
        internal const val CURRENT_THEME = "current_theme"
        internal const val USES_AMOLED_THEME = "uses_amoled_theme"
        internal const val SHOULD_COLOR_NAVBAR = "should_color_navbar"
        private const val SHOULD_LOAD_FULL_RES_PICTURES = "should_load_full_res_pictures"
        private const val SHOULD_CROP_WALLPAPER_BEFORE_APPLY = "should_crop_wallpaper_before_apply"
        private const val ANIMATIONS_ENABLED = "animations_enabled"
        private const val DOWNLOADS_FOLDER = "downloads_folder"
        private const val FUNCTIONAL_DASHBOARD = "functional_dashboard"
        private const val NOTIFICATIONS_ENABLED = "notifications_enabled"
        private const val REFRESH_MUZEI_ON_WIFI_ONLY = "refresh_muzei_on_wifi_only"
        private const val MUZEI_COLLECTIONS = "muzei_collections"
    }
}