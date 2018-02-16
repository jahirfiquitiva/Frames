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
package jahirfiquitiva.libs.frames.ui.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.Menu
import android.view.MenuItem
import android.widget.FrameLayout
import ca.allanwang.kau.utils.snackbar
import ca.allanwang.kau.utils.startLink
import com.afollestad.materialdialogs.folderselector.FolderChooserDialog
import jahirfiquitiva.libs.frames.R
import jahirfiquitiva.libs.frames.helpers.extensions.framesKonfigs
import jahirfiquitiva.libs.frames.ui.fragments.SettingsFragment
import jahirfiquitiva.libs.frames.ui.widgets.CustomToolbar
import jahirfiquitiva.libs.kauextensions.extensions.bind
import jahirfiquitiva.libs.kauextensions.extensions.getActiveIconsColorFor
import jahirfiquitiva.libs.kauextensions.extensions.getPrimaryTextColorFor
import jahirfiquitiva.libs.kauextensions.extensions.getSecondaryTextColorFor
import jahirfiquitiva.libs.kauextensions.extensions.primaryColor
import jahirfiquitiva.libs.kauextensions.extensions.setItemVisibility
import jahirfiquitiva.libs.kauextensions.extensions.tint
import jahirfiquitiva.libs.kauextensions.ui.activities.ActivityWFragments
import java.io.File

open class SettingsActivity : ActivityWFragments(), FolderChooserDialog.FolderCallback {
    
    override fun lightTheme(): Int = R.style.Frames_LightTheme
    override fun darkTheme(): Int = R.style.Frames_DarkTheme
    override fun amoledTheme(): Int = R.style.Frames_AmoledTheme
    override fun transparentTheme(): Int = R.style.Frames_TransparentTheme
    
    override fun autoTintStatusBar(): Boolean = true
    override fun autoTintNavigationBar(): Boolean = true
    
    var hasClearedFavs = false
    private val toolbar: CustomToolbar? by bind(R.id.toolbar)
    private var locationChooserDialog: FolderChooserDialog? = null
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
                getPrimaryTextColorFor(primaryColor, 0.6F),
                getSecondaryTextColorFor(primaryColor, 0.6F),
                getActiveIconsColorFor(primaryColor, 0.6F))
        
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
                getPrimaryTextColorFor(primaryColor, 0.6F),
                getSecondaryTextColorFor(primaryColor, 0.6F),
                getActiveIconsColorFor(primaryColor, 0.6F))
        return super.onCreateOptionsMenu(menu)
    }
    
    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        item?.let {
            when (it.itemId) {
                android.R.id.home -> doFinish()
                R.id.translate -> {
                    try {
                        startLink(getTranslationSite())
                    } catch (ignored: Exception) {
                    }
                }
                else -> {
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }
    
    open fun getTranslationSite(): String = "http://j.mp/FramesTranslations"
    
    override fun onBackPressed() {
        super.onBackPressed()
        doFinish()
    }
    
    fun showLocationChooserDialog() {
        clearDialog()
        locationChooserDialog = FolderChooserDialog.Builder(this)
                .chooseButton(R.string.choose_folder)
                .initialPath(framesKonfigs.downloadsFolder)
                .allowNewFolder(true, R.string.create_folder)
                .build()
        locationChooserDialog?.show(this)
    }
    
    fun clearDialog() {
        locationChooserDialog?.dismiss()
        locationChooserDialog = null
    }
    
    fun doFinish() {
        val intent = Intent()
        intent.putExtra("clearedFavs", hasClearedFavs)
        setResult(22, intent)
        try {
            (fragment as? SettingsFragment)?.clearDialog()
        } catch (ignored: Exception) {
        }
        clearDialog()
        supportFinishAfterTransition()
    }
    
    override fun onFolderChooserDismissed(dialog: FolderChooserDialog) {}
    
    override fun onFolderSelection(dialog: FolderChooserDialog, folder: File) {
        framesKonfigs.downloadsFolder = folder.absolutePath
        (fragment as? SettingsFragment)?.updateDownloadLocation()
    }
}