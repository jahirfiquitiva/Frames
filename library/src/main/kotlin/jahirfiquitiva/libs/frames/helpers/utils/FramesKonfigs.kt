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
package jahirfiquitiva.libs.frames.helpers.utils

import android.content.Context
import android.os.Environment
import ca.allanwang.kau.utils.boolean
import jahirfiquitiva.libs.frames.R
import jahirfiquitiva.libs.kauextensions.extensions.isInHorizontalMode
import jahirfiquitiva.libs.kauextensions.helpers.Konfigurations

open class FramesKonfigs(private val cntxt: Context) : Konfigurations("jfdb_confs", cntxt) {
    var backupJson: String
        get() = prefs.getString(BACKUP_JSON, "[]")
        set(value) = prefsEditor.putString(BACKUP_JSON, value).apply()
    
    var columns: Int
        get() = prefs.getInt(COLUMNS_NUMBER, if (cntxt.isInHorizontalMode) 3 else 2)
        set(value) = prefsEditor.putInt(COLUMNS_NUMBER, value).apply()
    
    var downloadsFolder: String
        get() = prefs.getString(
                DOWNLOADS_FOLDER,
                cntxt.getString(
                        R.string.default_download_folder,
                        Environment.getExternalStorageDirectory().absolutePath))
        set(value) = prefsEditor.putString(DOWNLOADS_FOLDER, value).apply()
    
    var fullResGridPictures: Boolean
        get() = prefs.getBoolean(FULL_RES_GRID_PICTURES, false)
        set(value) = prefsEditor.putBoolean(FULL_RES_GRID_PICTURES, value).apply()
    
    var deepSearchEnabled: Boolean
        get() = prefs.getBoolean(DEEP_SEARCH_ENABLED, true)
        set(value) = prefsEditor.putBoolean(DEEP_SEARCH_ENABLED, value).apply()
    
    var functionalDashboard: Boolean
        get() = prefs.getBoolean(FUNCTIONAL_DASHBOARD, false)
        set(value) = prefsEditor.putBoolean(FUNCTIONAL_DASHBOARD, value).apply()
    
    var notificationsEnabled: Boolean
        get() = prefs.getBoolean(
                NOTIFICATIONS_ENABLED, cntxt.boolean(R.bool.notifications_enabled_by_default))
        set(value) = prefsEditor.putBoolean(NOTIFICATIONS_ENABLED, value).apply()
    
    var refreshMuzeiOnWiFiOnly: Boolean
        get() = prefs.getBoolean(REFRESH_MUZEI_ON_WIFI_ONLY, false)
        set(value) = prefsEditor.putBoolean(REFRESH_MUZEI_ON_WIFI_ONLY, value).apply()
    
    var muzeiRefreshInterval: Int
        get() = prefs.getInt(MUZEI_REFRESH_INTERVAL, 10)
        set(value) = prefsEditor.putInt(MUZEI_REFRESH_INTERVAL, value).apply()
    
    var muzeiCollections: String
        get() = prefs.getString(MUZEI_COLLECTIONS, "")
        set(value) = prefsEditor.putString(MUZEI_COLLECTIONS, value).apply()
}