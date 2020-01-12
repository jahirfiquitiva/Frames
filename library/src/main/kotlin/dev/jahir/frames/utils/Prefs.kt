package dev.jahir.frames.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences

@Suppress("MemberVisibilityCanBePrivate")
open class Prefs(context: Context, name: String = PREFS_NAME, mode: Int = Context.MODE_PRIVATE) {
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

    enum class ThemeKey(val value: Int) {
        LIGHT(0), DARK(1), FOLLOW_SYSTEM(2);

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
    }
}