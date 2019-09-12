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
package jahirfiquitiva.libs.frames.ui.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.view.Menu
import android.view.MenuItem
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import ca.allanwang.kau.utils.openLink
import ca.allanwang.kau.utils.snackbar
import ca.allanwang.kau.utils.toast
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.files.folderChooser
import jahirfiquitiva.libs.archhelpers.extensions.mdDialog
import jahirfiquitiva.libs.frames.R
import jahirfiquitiva.libs.frames.helpers.utils.FramesKonfigs
import jahirfiquitiva.libs.frames.ui.fragments.SettingsFragment
import jahirfiquitiva.libs.frames.ui.widgets.CustomToolbar
import jahirfiquitiva.libs.kext.extensions.bind
import jahirfiquitiva.libs.kext.extensions.getActiveIconsColorFor
import jahirfiquitiva.libs.kext.extensions.getPrimaryTextColorFor
import jahirfiquitiva.libs.kext.extensions.getSecondaryTextColorFor
import jahirfiquitiva.libs.kext.extensions.primaryColor
import jahirfiquitiva.libs.kext.extensions.setItemVisibility
import jahirfiquitiva.libs.kext.extensions.tint
import jahirfiquitiva.libs.kext.ui.activities.ActivityWFragments
import java.io.File

open class SettingsActivity : ActivityWFragments<FramesKonfigs>() {
    
    override val prefs: FramesKonfigs by lazy { FramesKonfigs(this) }
    override fun lightTheme(): Int = R.style.LightTheme
    override fun darkTheme(): Int = R.style.DarkTheme
    override fun amoledTheme(): Int = R.style.AmoledTheme
    override fun transparentTheme(): Int = R.style.TransparentTheme
    
    override fun autoTintStatusBar(): Boolean = true
    override fun autoTintNavigationBar(): Boolean = true
    
    var hasClearedFavs = false
    private val toolbar: CustomToolbar? by bind(R.id.toolbar)
    private var locationChooserDialog: MaterialDialog? = null
    private val fragment: Fragment by lazy { settingsFragment() }
    
    open fun settingsFragment(): Fragment = SettingsFragment()
    override fun fragmentsContainer(): Int = R.id.fragments_container
    
    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_collection_settings)
        
        toolbar?.bindToActivity(this)
        
        supportActionBar?.setTitle(R.string.settings)
        toolbar?.tint(
            getPrimaryTextColorFor(primaryColor),
            getSecondaryTextColorFor(primaryColor),
            getActiveIconsColorFor(primaryColor))
        
        val container: FrameLayout? by bind(fragmentsContainer())
        container?.let {
            with(it) { setPadding(paddingLeft, paddingTop, paddingRight, 0) }
        }
        
        changeFragment(fragment, "Settings")
    }
    
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>,
        grantResults: IntArray
                                           ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 42) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showLocationChooserDialog()
            } else {
                snackbar(R.string.permission_denied)
            }
        }
    }
    
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.about_settings_menu, menu)
        menu?.setItemVisibility(R.id.licenses, false)
        toolbar?.tint(
            getPrimaryTextColorFor(primaryColor),
            getSecondaryTextColorFor(primaryColor),
            getActiveIconsColorFor(primaryColor))
        return super.onCreateOptionsMenu(menu)
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> doFinish()
            R.id.translate -> {
                try {
                    openLink(getTranslationSite())
                } catch (ignored: Exception) {
                }
            }
            else -> {
            }
        }
        return super.onOptionsItemSelected(item)
    }
    
    open fun getTranslationSite(): String = "https://crowdin.com/project/Frames"
    
    override fun onBackPressed() {
        super.onBackPressed()
        doFinish()
    }
    
    fun showLocationChooserDialog() {
        clearDialog()
        try {
            locationChooserDialog = mdDialog {
                folderChooser(
                    initialDirectory = try {
                        File(prefs.downloadsFolder)
                    } catch (e: Exception) {
                        @Suppress("DEPRECATION")
                        context.getExternalFilesDir(null)
                            ?: Environment.getExternalStorageDirectory()
                    },
                    allowFolderCreation = true,
                    folderCreationLabel = R.string.create_folder) { dialog, folder ->
                    prefs.downloadsFolder = folder.absolutePath
                    (fragment as? SettingsFragment)?.updateDownloadLocation()
                    dialog.dismiss()
                }
                positiveButton(R.string.choose_folder)
            }
            locationChooserDialog?.show()
        } catch (e: Exception) {
            toast(R.string.error_title)
        }
    }
    
    private fun clearDialog() {
        locationChooserDialog?.dismiss()
        locationChooserDialog = null
    }
    
    private fun doFinish() {
        val intent = Intent()
        intent.putExtra("clearedFavs", hasClearedFavs)
        setResult(22, intent)
        try {
            (fragment as? SettingsFragment)?.clearDialog()
        } catch (ignored: Exception) {
        }
        clearDialog()
        try {
            supportFinishAfterTransition()
        } catch (e: Exception) {
            finish()
        }
    }
}
