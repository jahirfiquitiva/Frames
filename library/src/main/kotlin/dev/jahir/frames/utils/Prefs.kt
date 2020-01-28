package dev.jahir.frames.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import androidx.annotation.StringRes
import dev.jahir.frames.R
import dev.jahir.frames.extensions.createIfDidNotExist
import dev.jahir.frames.extensions.getDefaultWallpapersDownloadFolder
import java.io.File

@Suppress("MemberVisibilityCanBePrivate")
open class Prefs(
    private val context: Context,
    name: String = PREFS_NAME,
    mode: Int = Context.MODE_PRIVATE
) {
    val prefs: SharedPreferences = context.getSharedPreferences(name, mode)
    @SuppressLint("CommitPrefEdits")
    val prefsEditor: SharedPreferences.Editor = prefs.edit()

    var isFirstRun: Boolean
        get() = prefs.getBoolean(IS_FIRST_RUN, true)
        set(value) = prefsEditor.putBoolean(IS_FIRST_RUN, value).apply()

    var lastVersion: Long
        get() = prefs.getLong(LAST_VERSION, -1L)
        set(value) = prefsEditor.putLong(LAST_VERSION, value).apply()

    var currentTheme: ThemeKey
        get() = ThemeKey.fromValue(prefs.getInt(CURRENT_THEME, ThemeKey.DEFAULT_THEME_KEY.value))
        set(value) = prefsEditor.putInt(CURRENT_THEME, value.value).apply()

    var usesAmoledTheme: Boolean
        get() = prefs.getBoolean(USES_AMOLED_THEME, false)
        set(value) = prefsEditor.putBoolean(USES_AMOLED_THEME, value).apply()

    var shouldColorNavbar: Boolean
        get() = prefs.getBoolean(SHOULD_COLOR_NAVBAR, true)
        set(value) = prefsEditor.putBoolean(SHOULD_COLOR_NAVBAR, value).apply()

    var shouldLoadFullResPictures: Boolean
        get() = prefs.getBoolean(SHOULD_LOAD_FULL_RES_PICTURES, false)
        set(value) = prefsEditor.putBoolean(SHOULD_LOAD_FULL_RES_PICTURES, value).apply()

    var shouldCropWallpaperBeforeApply: Boolean
        get() = prefs.getBoolean(SHOULD_CROP_WALLPAPER_BEFORE_APPLY, false)
        set(value) = prefsEditor.putBoolean(SHOULD_CROP_WALLPAPER_BEFORE_APPLY, value).apply()

    var animationsEnabled: Boolean
        get() = prefs.getBoolean(ANIMATIONS_ENABLED, true)
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
            try {
                context.resources.getBoolean(R.bool.notifications_enabled_by_default)
            } catch (e: Exception) {
                true
            }
        )
        set(value) = prefsEditor.putBoolean(NOTIFICATIONS_ENABLED, value).apply()

    enum class ThemeKey(val value: Int, @StringRes val stringResId: Int) {
        LIGHT(0, R.string.light_theme),
        DARK(1, R.string.dark_theme),
        FOLLOW_SYSTEM(2, R.string.follow_system_theme);

        companion object {
            internal val DEFAULT_THEME_KEY = FOLLOW_SYSTEM

            fun fromValue(value: Int): ThemeKey = when (value) {
                0 -> LIGHT
                1 -> DARK
                else -> FOLLOW_SYSTEM
            }
        }
    }

    companion object {
        private const val PREFS_NAME = "jfdb_confs"
        private const val IS_FIRST_RUN = "first_run"
        private const val LAST_VERSION = "last_version"
        private const val CURRENT_THEME = "current_theme"
        private const val USES_AMOLED_THEME = "uses_amoled_theme"
        private const val SHOULD_COLOR_NAVBAR = "should_color_navbar"
        private const val SHOULD_LOAD_FULL_RES_PICTURES = "should_load_full_res_pictures"
        private const val SHOULD_CROP_WALLPAPER_BEFORE_APPLY = "should_crop_wallpaper_before_apply"
        private const val ANIMATIONS_ENABLED = "animations_enabled"
        private const val DOWNLOADS_FOLDER = "downloads_folder"
        private const val FUNCTIONAL_DASHBOARD = "functional_dashboard"
        private const val NOTIFICATIONS_ENABLED = "notifications_enabled"
    }
}