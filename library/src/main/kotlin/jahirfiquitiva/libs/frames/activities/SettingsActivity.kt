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
package jahirfiquitiva.libs.frames.activities

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v7.widget.Toolbar
import android.view.MenuItem
import android.widget.FrameLayout
import android.widget.TextView
import ca.allanwang.kau.utils.snackbar
import com.afollestad.materialdialogs.folderselector.FolderChooserDialog
import jahirfiquitiva.libs.frames.R
import jahirfiquitiva.libs.frames.activities.base.BaseActivityWithFragments
import jahirfiquitiva.libs.frames.extensions.PERMISSION_REQUEST_CODE
import jahirfiquitiva.libs.frames.extensions.framesKonfigs
import jahirfiquitiva.libs.frames.fragments.SettingsFragment
import jahirfiquitiva.libs.kauextensions.extensions.getActiveIconsColorFor
import jahirfiquitiva.libs.kauextensions.extensions.getPrimaryTextColorFor
import jahirfiquitiva.libs.kauextensions.extensions.getSecondaryTextColorFor
import jahirfiquitiva.libs.kauextensions.extensions.primaryColor
import jahirfiquitiva.libs.kauextensions.extensions.tint
import java.io.File

open class SettingsActivity:BaseActivityWithFragments(), FolderChooserDialog.FolderCallback {
    
    override fun lightTheme():Int = R.style.LightTheme
    override fun darkTheme():Int = R.style.DarkTheme
    override fun amoledTheme():Int = R.style.AmoledTheme
    override fun transparentTheme():Int = R.style.TransparentTheme
    
    var hasClearedFavs = false
    var locationChooserDialog:FolderChooserDialog? = null
    private lateinit var fragment:SettingsFragment
    
    override fun onCreate(savedInstanceState:Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_with_fragments)
        
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        val toolbarTitle = findViewById<TextView>(R.id.toolbar_title)
        
        setSupportActionBar(toolbar)
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        
        toolbar.tint(getPrimaryTextColorFor(primaryColor, 0.6F),
                     getSecondaryTextColorFor(primaryColor, 0.6F),
                     getActiveIconsColorFor(primaryColor, 0.6F))
        toolbarTitle.setTextColor(getPrimaryTextColorFor(primaryColor, 0.6F))
        toolbarTitle.text = getString(R.string.settings)
        
        val container = findViewById<FrameLayout>(fragmentsContainer())
        container?.let {
            it.setPadding(it.paddingLeft, it.paddingTop, it.paddingRight, 0)
        }
        
        fragment = SettingsFragment()
        changeFragment(fragment, "Settings")
    }
    
    override fun onRequestPermissionsResult(requestCode:Int, permissions:Array<out String>,
                                            grantResults:IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
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
        clearDialog()
        ActivityCompat.finishAfterTransition(this)
    }
    
    override fun onFolderChooserDismissed(dialog:FolderChooserDialog) {
        // Do nothing
    }
    
    override fun onFolderSelection(dialog:FolderChooserDialog, folder:File) {
        framesKonfigs.downloadsFolder = folder.absolutePath
        fragment.updateDownloadLocation()
    }
    
    override fun fragmentsContainer():Int = R.id.fragments_container
    
    override fun hasBottomBar():Boolean = true
    
}