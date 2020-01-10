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
        get() = prefs.getLong(LAST_VERSION, -1)
        set(value) = prefsEditor.putLong(LAST_VERSION, value).apply()

    var shouldColorNavbar: Boolean
        get() = prefs.getBoolean(SHOULD_COLOR_NAVBAR, true)
        set(value) = prefsEditor.putBoolean(SHOULD_COLOR_NAVBAR, value).apply()

    var shouldLoadFullResPictures: Boolean
        get() = prefs.getBoolean(SHOULD_LOAD_FULL_RES_PICTURES, false)
        set(value) = prefsEditor.putBoolean(SHOULD_LOAD_FULL_RES_PICTURES, value).apply()

    var shouldCropWallpaperBeforeApply: Boolean
        get() = prefs.getBoolean(SHOULD_CROP_WALLPAPER_BEFORE_APPLY, false)
        set(value) = prefsEditor.putBoolean(SHOULD_CROP_WALLPAPER_BEFORE_APPLY, value).apply()

    companion object {
        private const val PREFS_NAME = "frames_prefs"
        const val IS_FIRST_RUN = "first_run"
        const val LAST_VERSION = "last_version"
        const val SHOULD_COLOR_NAVBAR = "should_color_navbar"
        const val SHOULD_LOAD_FULL_RES_PICTURES = "should_load_full_res_pictures"
        const val SHOULD_CROP_WALLPAPER_BEFORE_APPLY = "should_crop_wallpaper_before_apply"
    }
}