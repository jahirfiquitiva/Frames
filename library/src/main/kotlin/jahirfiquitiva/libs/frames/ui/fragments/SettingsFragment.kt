/*
 * Copyright (c) 2018. Jahir Fiquitiva
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
import android.os.Bundle
import android.preference.Preference
import android.preference.PreferenceCategory
import android.preference.SwitchPreference
import android.support.design.widget.Snackbar
import ca.allanwang.kau.utils.buildIsLollipopAndUp
import ca.allanwang.kau.utils.snackbar
import com.afollestad.materialdialogs.MaterialDialog
import com.fondesa.kpermissions.extension.listeners
import com.fondesa.kpermissions.extension.permissionsBuilder
import com.fondesa.kpermissions.request.runtime.nonce.PermissionNonce
import jahirfiquitiva.libs.frames.R
import jahirfiquitiva.libs.frames.data.models.db.FavoritesDatabase
import jahirfiquitiva.libs.frames.helpers.extensions.buildMaterialDialog
import jahirfiquitiva.libs.frames.helpers.extensions.clearDataAndCache
import jahirfiquitiva.libs.frames.helpers.extensions.dataCacheSize
import jahirfiquitiva.libs.frames.helpers.extensions.framesKonfigs
import jahirfiquitiva.libs.frames.helpers.utils.DATABASE_NAME
import jahirfiquitiva.libs.frames.ui.activities.SettingsActivity
import jahirfiquitiva.libs.frames.ui.fragments.base.PreferenceFragment
import jahirfiquitiva.libs.kauextensions.extensions.actv
import jahirfiquitiva.libs.kauextensions.extensions.boolean
import jahirfiquitiva.libs.kauextensions.extensions.ctxt
import jahirfiquitiva.libs.kauextensions.extensions.getAppName
import jahirfiquitiva.libs.kauextensions.extensions.konfigs
import jahirfiquitiva.libs.kauextensions.extensions.withActv
import jahirfiquitiva.libs.kauextensions.ui.activities.ThemedActivity
import org.jetbrains.anko.doAsync

@Suppress("DEPRECATION")
open class SettingsFragment : PreferenceFragment() {
    
    internal var database: FavoritesDatabase? = null
    internal var downloadLocation: Preference? = null
    
    var dialog: MaterialDialog? = null
    
    private val request by lazy {
        permissionsBuilder(Manifest.permission.WRITE_EXTERNAL_STORAGE).build()
    }
    
    fun requestStoragePermission(explanation: String, whenAccepted: () -> Unit) {
        try {
            request.detachAllListeners()
        } catch (e: Exception) {
        }
        request.listeners {
            onAccepted { whenAccepted() }
            onDenied { withActv { snackbar(R.string.permission_denied, Snackbar.LENGTH_LONG) } }
            onPermanentlyDenied {
                withActv { snackbar(R.string.permission_denied_completely, Snackbar.LENGTH_LONG) }
            }
            onShouldShowRationale { _, nonce -> showPermissionInformation(explanation, nonce) }
        }
        request.send()
    }
    
    @SuppressLint("NewApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initDatabase()
        initPreferences()
    }
    
    private fun initDatabase() {
        actv {
            if (boolean(R.bool.isFrames) && database == null) {
                database = Room.databaseBuilder(
                        it, FavoritesDatabase::class.java,
                        DATABASE_NAME).fallbackToDestructiveMigration().build()
            }
        }
    }
    
    open fun initPreferences() {
        addPreferencesFromResource(R.xml.preferences)
        
        val uiPrefs = findPreference("ui_settings") as? PreferenceCategory
        val navbarPref = findPreference("color_navbar") as? SwitchPreference
        
        if (!buildIsLollipopAndUp) {
            uiPrefs?.removePreference(navbarPref)
        }
        
        val themePref = findPreference("theme")
        themePref?.setOnPreferenceClickListener {
            clearDialog()
            val currentTheme = actv.konfigs.currentTheme
            dialog = actv.buildMaterialDialog {
                title(R.string.theme_setting_title)
                items(R.array.themes_options)
                itemsCallbackSingleChoice(currentTheme) { _, _, which, _ ->
                    if (which != currentTheme) {
                        actv {
                            it.konfigs.currentTheme = which
                            (it as? ThemedActivity)?.onThemeChanged()
                        }
                    }
                    true
                }
                positiveText(android.R.string.ok)
                negativeText(android.R.string.cancel)
            }
            dialog?.show()
            false
        }
        
        navbarPref?.isChecked = actv.konfigs.hasColoredNavbar
        navbarPref?.setOnPreferenceChangeListener { _, any ->
            val tint = any.toString().equals("true", true)
            if (tint != actv.konfigs.hasColoredNavbar) {
                actv.konfigs.hasColoredNavbar = tint
                (actv as? ThemedActivity)?.onThemeChanged()
            }
            true
        }
        
        val columns = findPreference("columns")
        if (boolean(R.bool.isFrames)) {
            columns?.setOnPreferenceClickListener {
                clearDialog()
                val currentColumns = ctxt.framesKonfigs.columns - 1
                dialog = ctxt.buildMaterialDialog {
                    title(R.string.wallpapers_columns_setting_title)
                    items("1", "2", "3", "4", "5")
                    itemsCallbackSingleChoice(currentColumns) { _, _, which, _ ->
                        if (which != currentColumns)
                            ctxt.framesKonfigs.columns = which + 1
                        true
                    }
                    positiveText(android.R.string.ok)
                    negativeText(android.R.string.cancel)
                }
                dialog?.show()
                false
            }
        } else {
            uiPrefs?.removePreference(columns)
        }
        
        val animationsPref = findPreference("animations") as? SwitchPreference
        animationsPref?.setOnPreferenceChangeListener { _, any ->
            val enable = any.toString().equals("true", true)
            if (enable != ctxt.framesKonfigs.animationsEnabled)
                ctxt.framesKonfigs.animationsEnabled = enable
            true
        }
        
        val hiResPref = findPreference("hi_res_pics") as? SwitchPreference
        hiResPref?.setOnPreferenceChangeListener { _, any ->
            val enable = any.toString().equals("true", true)
            if (enable != ctxt.framesKonfigs.fullResGridPictures) {
                ctxt.framesKonfigs.fullResGridPictures = enable
                (actv as? SettingsActivity)?.hasClearedFavs = true
            }
            true
        }
        
        val deepSearchPref = findPreference("deep_search") as? SwitchPreference
        deepSearchPref?.setOnPreferenceChangeListener { _, any ->
            val enable = any.toString().equals("true", true)
            if (enable != ctxt.framesKonfigs.deepSearchEnabled)
                ctxt.framesKonfigs.deepSearchEnabled = enable
            true
        }
        
        val storagePrefs = findPreference("storage_settings") as? PreferenceCategory
        
        downloadLocation = findPreference("wallpapers_download_location")
        updateDownloadLocation()
        downloadLocation?.setOnPreferenceClickListener {
            requestPermission()
            true
        }
        
        val clearData = findPreference("clear_data")
        clearData?.summary = getString(R.string.data_cache_setting_content, actv.dataCacheSize)
        clearData?.setOnPreferenceClickListener {
            clearDialog()
            dialog = actv.buildMaterialDialog {
                title(R.string.data_cache_setting_title)
                content(R.string.data_cache_confirmation)
                positiveText(android.R.string.ok)
                negativeText(android.R.string.cancel)
                onPositive { _, _ ->
                    actv.clearDataAndCache()
                    clearData.summary = getString(
                            R.string.data_cache_setting_content,
                            actv.dataCacheSize)
                }
            }
            dialog?.show()
            true
        }
        
        val clearDatabase = findPreference("clear_database")
        if (boolean(R.bool.isFrames)) {
            clearDatabase?.setOnPreferenceClickListener {
                clearDialog()
                dialog = actv.buildMaterialDialog {
                    title(R.string.clear_favorites_setting_title)
                    content(R.string.clear_favorites_confirmation)
                    positiveText(android.R.string.ok)
                    negativeText(android.R.string.cancel)
                    onPositive { _, _ ->
                        doAsync {
                            database?.favoritesDao()?.nukeFavorites()
                            (activity as? SettingsActivity)?.hasClearedFavs = true
                        }
                    }
                }
                dialog?.show()
                true
            }
        } else {
            storagePrefs?.removePreference(clearDatabase)
        }
        
        val notifPref = findPreference("enable_notifications") as? SwitchPreference
        notifPref?.isChecked = actv.framesKonfigs.notificationsEnabled
        notifPref?.setOnPreferenceChangeListener { _, any ->
            val enable = any.toString().equals("true", true)
            if (enable != actv.framesKonfigs.notificationsEnabled) {
                actv.framesKonfigs.notificationsEnabled = enable
            }
            true
        }
    }
    
    fun requestPermission() {
        requestStoragePermission(getString(R.string.permission_request, actv.getAppName())) {
            (activity as? SettingsActivity)?.showLocationChooserDialog()
        }
    }
    
    private fun showPermissionInformation(explanation: String, nonce: PermissionNonce) {
        withActv {
            snackbar(explanation) {
                setAction(R.string.allow, {
                    dismiss()
                    nonce.use()
                })
                addCallback(
                        object : Snackbar.Callback() {
                            override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                                super.onDismissed(transientBottomBar, event)
                                requestPermission()
                            }
                        })
            }
        }
    }
    
    fun updateDownloadLocation() {
        downloadLocation?.summary = getString(
                R.string.wallpapers_download_location_setting_content,
                actv.framesKonfigs.downloadsFolder)
    }
    
    fun clearDialog() {
        dialog?.dismiss()
        dialog = null
    }
    
    override fun onDestroy() {
        super.onDestroy()
        clearDialog()
        try {
            request.detachAllListeners()
        } catch (e: Exception) {
        }
    }
}