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
package jahirfiquitiva.libs.frames.ui.activities

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v7.widget.Toolbar
import android.view.MenuItem
import android.widget.FrameLayout
import ca.allanwang.kau.utils.snackbar
import com.afollestad.materialdialogs.folderselector.FolderChooserDialog
import jahirfiquitiva.libs.frames.R
import jahirfiquitiva.libs.frames.helpers.extensions.framesKonfigs
import jahirfiquitiva.libs.frames.ui.activities.base.BaseActivityWithFragments
import jahirfiquitiva.libs.frames.ui.fragments.SettingsFragment
import jahirfiquitiva.libs.kauextensions.extensions.bind
import jahirfiquitiva.libs.kauextensions.extensions.getActiveIconsColorFor
import jahirfiquitiva.libs.kauextensions.extensions.getPrimaryTextColorFor
import jahirfiquitiva.libs.kauextensions.extensions.getSecondaryTextColorFor
import jahirfiquitiva.libs.kauextensions.extensions.lazyAndroid
import jahirfiquitiva.libs.kauextensions.extensions.primaryColor
import jahirfiquitiva.libs.kauextensions.extensions.tint
import java.io.File

open class SettingsActivity:BaseActivityWithFragments(), FolderChooserDialog.FolderCallback {
    
    override fun lightTheme():Int = R.style.LightTheme
    override fun darkTheme():Int = R.style.DarkTheme
    override fun amoledTheme():Int = R.style.AmoledTheme
    override fun transparentTheme():Int = R.style.TransparentTheme
    
    var hasClearedFavs = false
    private var locationChooserDialog:FolderChooserDialog? = null
    private val fragment:Fragment by lazyAndroid { settingsFragment() }
    
    override fun onCreate(savedInstanceState:Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_collection_settings)
        
        val toolbar:Toolbar by bind(R.id.toolbar)
        
        setSupportActionBar(toolbar)
        supportActionBar?.let {
            with(it) {
                setHomeButtonEnabled(true)
                setDisplayHomeAsUpEnabled(true)
                setDisplayShowHomeEnabled(true)
            }
        }
        
        toolbar.setTitle(R.string.settings)
        toolbar.tint(getPrimaryTextColorFor(primaryColor, 0.6F),
                     getSecondaryTextColorFor(primaryColor, 0.6F),
                     getActiveIconsColorFor(primaryColor, 0.6F))
        
        val container:FrameLayout by bind(fragmentsContainer())
        with(container) {
            setPadding(paddingLeft, paddingTop, paddingRight, 0)
        }
        
        changeFragment(fragment, "Settings")
    }
    
    override fun onRequestPermissionsResult(requestCode:Int, permissions:Array<out String>,
                                            grantResults:IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 42) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showLocationChooserDialog()
            } else {
                snackbar(R.string.permission_denied)
            }
        }
    }
    
    override fun onOptionsItemSelected(item:MenuItem?):Boolean {
        item?.let {
            if (it.itemId == android.R.id.home) {
                doFinish()
            }
        }
        return super.onOptionsItemSelected(item)
    }
    
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
            if (fragment is SettingsFragment) (fragment as SettingsFragment).clearDialog()
        } catch (ignored:Exception) {
        }
        clearDialog()
        supportFinishAfterTransition()
    }
    
    override fun onFolderChooserDismissed(dialog:FolderChooserDialog) {}
    
    override fun onFolderSelection(dialog:FolderChooserDialog, folder:File) {
        framesKonfigs.downloadsFolder = folder.absolutePath
        if (fragment is SettingsFragment) {
            (fragment as SettingsFragment).updateDownloadLocation()
        }
    }
    
    open fun settingsFragment():Fragment = SettingsFragment()
    override fun fragmentsContainer():Int = R.id.fragments_container
}