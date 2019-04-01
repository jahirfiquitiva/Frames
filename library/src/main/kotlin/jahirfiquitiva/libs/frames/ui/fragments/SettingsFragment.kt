/*
 * Copyright (c) 2019. Jahir Fiquitiva
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
import android.os.Build
import android.os.Bundle
import android.preference.Preference
import android.preference.PreferenceCategory
import android.preference.PreferenceScreen
import android.preference.SwitchPreference
import androidx.room.Room
import ca.allanwang.kau.utils.openLink
import ca.allanwang.kau.utils.snackbar
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import com.fondesa.kpermissions.extension.listeners
import com.fondesa.kpermissions.extension.permissionsBuilder
import com.fondesa.kpermissions.request.runtime.nonce.PermissionNonce
import com.google.android.material.snackbar.Snackbar
import jahirfiquitiva.libs.frames.R
import jahirfiquitiva.libs.frames.data.models.db.FavoritesDatabase
import jahirfiquitiva.libs.frames.helpers.extensions.clearDataAndCache
import jahirfiquitiva.libs.frames.helpers.extensions.configs
import jahirfiquitiva.libs.frames.helpers.extensions.dataCacheSize
import jahirfiquitiva.libs.frames.helpers.extensions.mdDialog
import jahirfiquitiva.libs.frames.helpers.utils.DATABASE_NAME
import jahirfiquitiva.libs.frames.ui.activities.SettingsActivity
import jahirfiquitiva.libs.frames.ui.fragments.base.PreferenceFragment
import jahirfiquitiva.libs.kext.extensions.activity
import jahirfiquitiva.libs.kext.extensions.boolean
import jahirfiquitiva.libs.kext.extensions.getAppName
import jahirfiquitiva.libs.kext.extensions.hasContent
import jahirfiquitiva.libs.kext.extensions.itemsSingleChoice
import jahirfiquitiva.libs.kext.extensions.string
import jahirfiquitiva.libs.kext.ui.activities.ThemedActivity
import org.jetbrains.anko.doAsync

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
            onDenied {
                activity {
                    it.snackbar(R.string.permission_denied, duration = Snackbar.LENGTH_LONG)
                }
            }
            onPermanentlyDenied {
                activity {
                    it.snackbar(
                        R.string.permission_denied_completely, duration = Snackbar.LENGTH_LONG)
                }
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
        activity {
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
        
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            uiPrefs?.removePreference(navbarPref)
        }
        
        val themePref = findPreference("theme")
        themePref?.setOnPreferenceClickListener {
            clearDialog()
            val currentTheme = configs.currentTheme
            dialog = activity?.mdDialog {
                title(R.string.theme_setting_title)
                listItemsSingleChoice(
                    R.array.themes_options, initialSelection = currentTheme) { _, index, _ ->
                    if (index != currentTheme) {
                        configs.currentTheme = index
                        (activity as? ThemedActivity<*>)?.onThemeChanged()
                    }
                }
                positiveButton(android.R.string.ok)
                negativeButton(android.R.string.cancel)
            }
            dialog?.show()
            false
        }
        
        navbarPref?.isChecked = configs.hasColoredNavbar
        navbarPref?.setOnPreferenceChangeListener { _, any ->
            val tint = any.toString().equals("true", true)
            if (tint != configs.hasColoredNavbar) {
                configs.hasColoredNavbar = tint
                (activity as? ThemedActivity<*>)?.onThemeChanged()
            }
            true
        }
        
        val columns = findPreference("columns")
        if (boolean(R.bool.isFrames)) {
            columns?.setOnPreferenceClickListener {
                clearDialog()
                val currentColumns = configs.columns - 1
                dialog = context?.mdDialog {
                    title(R.string.wallpapers_columns_setting_title)
                    itemsSingleChoice(arrayOf(1, 2, 3, 4, 5), currentColumns) { _, which, _ ->
                        if (which != currentColumns) configs.columns = which + 1
                    }
                    positiveButton(android.R.string.ok)
                    negativeButton(android.R.string.cancel)
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
            if (enable != configs.animationsEnabled)
                configs.animationsEnabled = enable
            true
        }
        
        val hiResPref = findPreference("hi_res_pics") as? SwitchPreference
        hiResPref?.setOnPreferenceChangeListener { _, any ->
            val enable = any.toString().equals("true", true)
            if (enable != configs.fullResGridPictures) {
                configs.fullResGridPictures = enable
                (activity as? SettingsActivity)?.hasClearedFavs = true
            }
            true
        }
        
        val deepSearchPref = findPreference("deep_search") as? SwitchPreference
        deepSearchPref?.setOnPreferenceChangeListener { _, any ->
            val enable = any.toString().equals("true", true)
            if (enable != configs.deepSearchEnabled)
                configs.deepSearchEnabled = enable
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
        clearData?.summary = getString(R.string.data_cache_setting_content, activity?.dataCacheSize)
        clearData?.setOnPreferenceClickListener {
            clearDialog()
            dialog = activity?.mdDialog {
                title(R.string.data_cache_setting_title)
                message(R.string.data_cache_confirmation)
                negativeButton(android.R.string.cancel)
                positiveButton(android.R.string.ok) {
                    activity?.clearDataAndCache()
                    clearData.summary = getString(
                        R.string.data_cache_setting_content,
                        activity?.dataCacheSize)
                }
            }
            dialog?.show()
            true
        }
        
        val clearDatabase = findPreference("clear_database")
        if (boolean(R.bool.isFrames)) {
            clearDatabase?.setOnPreferenceClickListener {
                clearDialog()
                dialog = activity?.mdDialog {
                    title(R.string.clear_favorites_setting_title)
                    message(R.string.clear_favorites_confirmation)
                    negativeButton(android.R.string.cancel)
                    positiveButton(android.R.string.ok) {
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
        notifPref?.isChecked = configs.notificationsEnabled
        notifPref?.setOnPreferenceChangeListener { _, any ->
            val enable = any.toString().equals("true", true)
            if (enable != configs.notificationsEnabled) {
                configs.notificationsEnabled = enable
            }
            true
        }
        
        val privacyLink = try {
            string(R.string.privacy_policy_link, "")
        } catch (e: Exception) {
            ""
        }
        
        val termsLink = try {
            string(R.string.terms_conditions_link, "")
        } catch (e: Exception) {
            ""
        }
        
        val prefsScreen = findPreference("preferences") as? PreferenceScreen
        val legalCategory = findPreference("legal") as? PreferenceCategory
        
        if (privacyLink.hasContent() || termsLink.hasContent()) {
            val privacyPref = findPreference("privacy")
            if (privacyLink.hasContent()) {
                privacyPref?.setOnPreferenceClickListener {
                    try {
                        context?.openLink(privacyLink)
                    } catch (e: Exception) {
                    }
                    true
                }
            } else {
                legalCategory?.removePreference(privacyPref)
            }
            
            val termsPref = findPreference("terms")
            if (termsLink.hasContent()) {
                termsPref?.setOnPreferenceClickListener {
                    try {
                        context?.openLink(termsLink)
                    } catch (e: Exception) {
                    }
                    true
                }
            } else {
                legalCategory?.removePreference(termsPref)
            }
        } else {
            prefsScreen?.removePreference(legalCategory)
        }
    }
    
    fun requestPermission() {
        requestStoragePermission(getString(R.string.permission_request, activity?.getAppName())) {
            (activity as? SettingsActivity)?.showLocationChooserDialog()
        }
    }
    
    private fun showPermissionInformation(explanation: String, nonce: PermissionNonce) {
        activity {
            it.snackbar(explanation) {
                setAction(R.string.allow) {
                    dismiss()
                    nonce.use()
                }
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
            R.string.wallpapers_download_location_setting_content, configs.downloadsFolder)
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
