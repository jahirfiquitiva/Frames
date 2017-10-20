/*
 * Copyright (c) 2017. Jahir Fiquitiva
 *
 * Licensed under the CreativeCommons Attribution-ShareAlike
 * 4.0 International License. You may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *    http://creativecommons.org/licenses/by-sa/4.0/legalcode
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jahirfiquitiva.libs.frames.ui.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.arch.persistence.room.Room
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.preference.Preference
import android.preference.PreferenceCategory
import android.preference.SwitchPreference
import android.support.design.widget.Snackbar
import ca.allanwang.kau.utils.buildIsLollipopAndUp
import ca.allanwang.kau.utils.snackbar
import com.afollestad.materialdialogs.MaterialDialog
import com.github.stephenvinouze.materialnumberpickercore.MaterialNumberPicker
import jahirfiquitiva.libs.frames.R
import jahirfiquitiva.libs.frames.data.models.db.FavoritesDatabase
import jahirfiquitiva.libs.frames.helpers.extensions.PermissionRequestListener
import jahirfiquitiva.libs.frames.helpers.extensions.buildMaterialDialog
import jahirfiquitiva.libs.frames.helpers.extensions.checkPermission
import jahirfiquitiva.libs.frames.helpers.extensions.clearDataAndCache
import jahirfiquitiva.libs.frames.helpers.extensions.dataCacheSize
import jahirfiquitiva.libs.frames.helpers.extensions.framesKonfigs
import jahirfiquitiva.libs.frames.helpers.extensions.requestPermissions
import jahirfiquitiva.libs.frames.helpers.utils.DATABASE_NAME
import jahirfiquitiva.libs.frames.ui.activities.SettingsActivity
import jahirfiquitiva.libs.frames.ui.fragments.base.PreferenceFragment
import jahirfiquitiva.libs.kauextensions.activities.ThemedActivity
import jahirfiquitiva.libs.kauextensions.extensions.getAppName
import jahirfiquitiva.libs.kauextensions.extensions.getBoolean
import jahirfiquitiva.libs.kauextensions.extensions.hasContent
import jahirfiquitiva.libs.kauextensions.extensions.konfigs
import jahirfiquitiva.libs.kauextensions.extensions.lazyAndroid
import jahirfiquitiva.libs.kauextensions.extensions.secondaryTextColor
import org.jetbrains.anko.doAsync

open class SettingsFragment:PreferenceFragment() {
    
    internal val database:FavoritesDatabase by lazyAndroid {
        Room.databaseBuilder(activity, FavoritesDatabase::class.java,
                             DATABASE_NAME).fallbackToDestructiveMigration().build()
    }
    internal var downloadLocation:Preference? = null
    
    var dialog:MaterialDialog? = null
    
    @SuppressLint("NewApi")
    override fun onCreate(savedInstanceState:Bundle?) {
        super.onCreate(savedInstanceState)
        initPreferences()
    }
    
    open fun initPreferences() {
        addPreferencesFromResource(R.xml.preferences)
        
        val uiPrefs = findPreference("ui_settings") as PreferenceCategory
        val navbarPref = findPreference("color_navbar") as SwitchPreference
        
        if (!buildIsLollipopAndUp) {
            uiPrefs.removePreference(navbarPref)
        }
        
        val themePref = findPreference("theme")
        themePref?.setOnPreferenceClickListener {
            clearDialog()
            val currentTheme = activity.konfigs.currentTheme
            dialog = activity.buildMaterialDialog {
                title(R.string.theme_setting_title)
                items(R.array.themes_options)
                itemsCallbackSingleChoice(currentTheme) { _, _, which, _ ->
                    if (which != currentTheme) {
                        activity.konfigs.currentTheme = which
                        if (activity is ThemedActivity) (activity as ThemedActivity).onThemeChanged()
                    }
                    true
                }
                positiveText(android.R.string.ok)
                negativeText(android.R.string.cancel)
            }
            dialog?.show()
            false
        }
        
        navbarPref.isChecked = activity.konfigs.hasColoredNavbar
        navbarPref.setOnPreferenceChangeListener { _, any ->
            val tint = any.toString().equals("true", true)
            if (tint != activity.konfigs.hasColoredNavbar) {
                activity.konfigs.hasColoredNavbar = tint
                if (activity is ThemedActivity) (activity as ThemedActivity).onThemeChanged()
            }
            true
        }
        
        val columns = findPreference("columns")
        if (context.getBoolean(R.bool.isFrames)) {
            columns?.setOnPreferenceClickListener {
                clearDialog()
                val currentColumns = context.framesKonfigs.columns
                
                val numberPicker = MaterialNumberPicker(
                        context = this.context,
                        minValue = 1,
                        maxValue = 6,
                        value = currentColumns,
                        separatorColor = Color.TRANSPARENT,
                        textColor = this.context.secondaryTextColor,
                        wrapped = true)
                
                dialog = context.buildMaterialDialog {
                    title(R.string.wallpapers_columns_setting_title)
                    customView(numberPicker, false)
                    positiveText(android.R.string.ok)
                    onPositive { dialog, _ ->
                        try {
                            val newColumns = numberPicker.value
                            if (currentColumns != newColumns) context.framesKonfigs.columns = newColumns
                        } catch (ignored:Exception) {
                        }
                        dialog.dismiss()
                    }
                }
                dialog?.show()
                false
            }
        } else {
            uiPrefs.removePreference(columns)
        }
        
        val animationsPref = findPreference("animations") as SwitchPreference
        animationsPref.setOnPreferenceChangeListener { _, any ->
            val enable = any.toString().equals("true", true)
            if (enable != context.framesKonfigs.animationsEnabled)
                context.framesKonfigs.animationsEnabled = enable
            true
        }
        
        val hiResPref = findPreference("hi_res_pics") as SwitchPreference
        hiResPref.setOnPreferenceChangeListener { _, any ->
            val enable = any.toString().equals("true", true)
            if (enable != context.framesKonfigs.fullResGridPictures) {
                context.framesKonfigs.fullResGridPictures = enable
                if (activity is SettingsActivity) {
                    (activity as SettingsActivity).hasClearedFavs = true
                }
            }
            true
        }
        
        val deepSearchPref = findPreference("deep_search") as SwitchPreference
        deepSearchPref.setOnPreferenceChangeListener { _, any ->
            val enable = any.toString().equals("true", true)
            if (enable != context.framesKonfigs.deepSearchEnabled)
                context.framesKonfigs.deepSearchEnabled = enable
            true
        }
        
        val storagePrefs = findPreference("storage_settings") as PreferenceCategory
        
        downloadLocation = findPreference("wallpapers_download_location")
        updateDownloadLocation()
        downloadLocation?.setOnPreferenceClickListener {
            requestPermission()
            true
        }
        
        val clearData = findPreference("clear_data")
        clearData?.summary = getString(R.string.data_cache_setting_content, activity.dataCacheSize)
        clearData?.setOnPreferenceClickListener {
            clearDialog()
            dialog = activity.buildMaterialDialog {
                title(R.string.data_cache_setting_title)
                content(R.string.data_cache_confirmation)
                positiveText(android.R.string.ok)
                negativeText(android.R.string.cancel)
                onPositive { _, _ ->
                    activity.clearDataAndCache()
                    clearData.summary = getString(R.string.data_cache_setting_content,
                                                  activity.dataCacheSize)
                }
            }
            dialog?.show()
            true
        }
        
        val clearDatabase = findPreference("clear_database")
        if (context.getBoolean(R.bool.isFrames)) {
            clearDatabase?.setOnPreferenceClickListener {
                clearDialog()
                dialog = activity.buildMaterialDialog {
                    title(R.string.clear_favorites_setting_title)
                    content(R.string.clear_favorites_confirmation)
                    positiveText(android.R.string.ok)
                    negativeText(android.R.string.cancel)
                    onPositive { _, _ ->
                        doAsync {
                            database.favoritesDao().nukeFavorites()
                        }
                        if (activity is SettingsActivity) {
                            (activity as SettingsActivity).hasClearedFavs = true
                        }
                    }
                }
                dialog?.show()
                true
            }
        } else {
            storagePrefs.removePreference(clearDatabase)
        }
        
        val notifPref = findPreference("enable_notifications") as SwitchPreference
        
        val serviceAvailable = isNotificationsServiceAvailable()
        
        notifPref.isEnabled = serviceAvailable
        notifPref.isChecked = activity.framesKonfigs.notificationsEnabled && serviceAvailable
        notifPref.setOnPreferenceChangeListener { _, any ->
            val enable = any.toString().equals("true", true)
            if (enable != activity.framesKonfigs.notificationsEnabled) {
                activity.framesKonfigs.notificationsEnabled = enable
            }
            true
        }
    }
    
    private fun getNotificationsClass():Class<*>? {
        return try {
            val className = context.getString(R.string.notifications_class)
            if (className.hasContent()) Class.forName(className)
            else null
        } catch (e:Exception) {
            e.printStackTrace()
            null
        }
    }
    
    private fun isNotificationsServiceAvailable():Boolean {
        return try {
            val packageManager = context.packageManager
            val klass = getNotificationsClass()
            val notNull = klass?.let { true } ?: false
            if (notNull) {
                val intent = Intent(context, klass)
                val resolveInfo = packageManager.queryIntentServices(intent,
                                                                     PackageManager.MATCH_DEFAULT_ONLY)
                resolveInfo.size > 0
            } else {
                false
            }
        } catch (ex:Exception) {
            ex.printStackTrace()
            false
        }
    }
    
    fun requestPermission() = activity.checkPermission(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            object:PermissionRequestListener {
                override fun onPermissionRequest(permission:String) =
                        activity.requestPermissions(42, permission)
                
                override fun showPermissionInformation(permission:String) {
                    doShowPermissionInformation()
                }
                
                override fun onPermissionCompletelyDenied() {
                    activity.snackbar(R.string.permission_denied_completely)
                }
                
                override fun onPermissionGranted() {
                    if (activity is SettingsActivity)
                        (activity as SettingsActivity).showLocationChooserDialog()
                }
            })
    
    private fun doShowPermissionInformation() {
        activity.snackbar(
                getString(R.string.permission_request, activity.getAppName()),
                builder = {
                    setAction(R.string.allow, { dismiss() })
                    addCallback(object:Snackbar.Callback() {
                        override fun onDismissed(transientBottomBar:Snackbar?, event:Int) {
                            super.onDismissed(transientBottomBar, event)
                            requestPermission()
                        }
                    })
                })
    }
    
    fun updateDownloadLocation() {
        downloadLocation?.summary = getString(R.string.wallpapers_download_location_setting_content,
                                              activity.framesKonfigs.downloadsFolder)
    }
    
    fun clearDialog() {
        dialog?.dismiss()
        dialog = null
    }
    
    override fun onDestroy() {
        super.onDestroy()
        clearDialog()
    }
}