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
package jahirfiquitiva.libs.frames.ui.activities.base

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.support.annotation.StringRes
import android.support.design.widget.Snackbar
import ca.allanwang.kau.utils.isNetworkAvailable
import com.afollestad.materialdialogs.MaterialDialog
import jahirfiquitiva.libs.frames.R
import jahirfiquitiva.libs.frames.data.models.Wallpaper
import jahirfiquitiva.libs.frames.helpers.extensions.PermissionRequestListener
import jahirfiquitiva.libs.frames.helpers.extensions.buildMaterialDialog
import jahirfiquitiva.libs.frames.helpers.extensions.checkPermission
import jahirfiquitiva.libs.frames.helpers.extensions.framesKonfigs
import jahirfiquitiva.libs.frames.helpers.extensions.openWallpaper
import jahirfiquitiva.libs.frames.helpers.extensions.requestPermissions
import jahirfiquitiva.libs.frames.ui.fragments.dialogs.WallpaperActionsFragment
import jahirfiquitiva.libs.kauextensions.activities.ThemedActivity
import jahirfiquitiva.libs.kauextensions.extensions.formatCorrectly
import jahirfiquitiva.libs.kauextensions.extensions.getAppName
import jahirfiquitiva.libs.kauextensions.extensions.getUri
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

abstract class WallpaperActionsActivity:ThemedActivity() {
    
    private var actionDialog:MaterialDialog? = null
    internal var wallActions:WallpaperActionsFragment? = null
    
    internal val DOWNLOAD_ACTION_ID = 1
    internal val APPLY_ACTION_ID = 2
    
    internal abstract var wallpaper:Wallpaper?
    internal abstract val allowBitmapApply:Boolean
    
    override fun onRequestPermissionsResult(requestCode:Int, permissions:Array<out String>,
                                            grantResults:IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 41 || requestCode == 42) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                checkIfFileExists(requestCode == 41)
            } else {
                showSnackbar(R.string.permission_denied, Snackbar.LENGTH_LONG)
            }
        }
    }
    
    open fun doItemClick(actionId:Int) {
        when (actionId) {
            DOWNLOAD_ACTION_ID -> downloadWallpaper(false)
            APPLY_ACTION_ID -> downloadWallpaper(true)
        }
    }
    
    @SuppressLint("NewApi")
    private fun downloadWallpaper(toApply:Boolean) {
        if (isNetworkAvailable) {
            checkPermission(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    object:PermissionRequestListener {
                        override fun onPermissionRequest(permission:String) =
                                requestPermissions(if (toApply) 41 else 42, permission)
                        
                        override fun showPermissionInformation(permission:String) =
                                showPermissionInformation(toApply)
                        
                        override fun onPermissionCompletelyDenied() =
                                showSnackbar(R.string.permission_denied_completely,
                                             Snackbar.LENGTH_LONG)
                        
                        override fun onPermissionGranted() = checkIfFileExists(toApply)
                    })
        } else {
            if (toApply && allowBitmapApply) showWallpaperApplyOptions(null)
            else showNotConnectedDialog()
        }
    }
    
    private fun showPermissionInformation(toApply:Boolean) {
        showSnackbar(getString(R.string.permission_request, getAppName()),
                     Snackbar.LENGTH_LONG, {
                         setAction(R.string.allow, {
                             dismiss()
                             downloadWallpaper(toApply)
                         })
                     })
    }
    
    private fun checkIfFileExists(toApply:Boolean) {
        wallpaper?.let {
            properlyCancelDialog()
            val folder = File(framesKonfigs.downloadsFolder)
            folder.mkdirs()
            val extension = it.url.substring(it.url.lastIndexOf("."))
            var correctExtension = getWallpaperExtension(extension)
            val fileName = it.name.formatCorrectly()
            if (toApply) correctExtension = ".temp" + correctExtension
            val dest = File(folder, fileName + correctExtension)
            if (dest.exists()) {
                actionDialog = buildMaterialDialog {
                    content(R.string.file_exists)
                    negativeText(R.string.file_replace)
                    positiveText(R.string.file_create_new)
                    onPositive { _, _ ->
                        val time = getCurrentTimeStamp().formatCorrectly().replace(" ", "_")
                        val newDest = File(folder, fileName + "_" + time + correctExtension)
                        if (toApply) showWallpaperApplyOptions(newDest)
                        else startDownload(newDest)
                    }
                    onNegative { _, _ ->
                        if (toApply) showWallpaperApplyOptions(dest)
                        else startDownload(dest)
                    }
                }
                actionDialog?.show()
            } else {
                if (toApply) showWallpaperApplyOptions(dest)
                else startDownload(dest)
            }
        }
    }
    
    private fun startDownload(dest:File) {
        wallpaper?.let {
            properlyCancelDialog()
            wallActions = WallpaperActionsFragment()
            wallActions?.show(this, it, dest)
        }
    }
    
    fun showWallpaperDownloadedSnackbar(dest:File) {
        sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(dest)))
        runOnUiThread {
            properlyCancelDialog()
            showSnackbar(getString(R.string.download_successful, dest.toString()),
                         Snackbar.LENGTH_LONG, {
                             setAction(R.string.open, {
                                 dest.getUri(this@WallpaperActionsActivity)?.let {
                                     openWallpaper(it)
                                 }
                             })
                         })
        }
    }
    
    @SuppressLint("SimpleDateFormat")
    private fun getCurrentTimeStamp():String {
        val sdfDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        return sdfDate.format(Date())
    }
    
    private fun getWallpaperExtension(currenExt:String):String {
        val validExtensions = arrayOf(".jpg", ".jpeg", ".png")
        validExtensions.forEach {
            if (currenExt.contains(it, true)) return it
        }
        return ".png"
    }
    
    private fun showWallpaperApplyOptions(dest:File?) {
        properlyCancelDialog()
        actionDialog = buildMaterialDialog {
            title(R.string.apply_to)
            items(getString(R.string.home_screen), getString(R.string.lock_screen),
                  getString(R.string.home_lock_screen))
            itemsCallback { _, _, position, _ ->
                if (dest != null) {
                    applyWallpaper(dest, position == 0, position == 1, position == 2)
                } else {
                    if (allowBitmapApply)
                        applyBitmapWallpaper(position == 0, position == 1, position == 2)
                }
            }
        }
        actionDialog?.show()
    }
    
    abstract fun applyBitmapWallpaper(toHomeScreen:Boolean, toLockScreen:Boolean, toBoth:Boolean)
    
    private fun applyWallpaper(dest:File,
                               toHomeScreen:Boolean, toLockScreen:Boolean, toBoth:Boolean) {
        wallpaper?.let {
            properlyCancelDialog()
            wallActions = WallpaperActionsFragment()
            wallActions?.show(this, it, dest, toHomeScreen, toLockScreen, toBoth)
        }
    }
    
    fun showWallpaperAppliedSnackbar(toHomeScreen:Boolean, toLockScreen:Boolean, toBoth:Boolean) {
        properlyCancelDialog()
        showSnackbar(getString(R.string.apply_successful,
                               getString(when {
                                             toBoth -> R.string.home_lock_screen
                                             toHomeScreen -> R.string.home_screen
                                             toLockScreen -> R.string.lock_screen
                                             else -> R.string.empty
                                         }).toLowerCase()), Snackbar.LENGTH_LONG)
    }
    
    private fun showNotConnectedDialog() {
        properlyCancelDialog()
        actionDialog = buildMaterialDialog {
            title(R.string.muzei_not_connected_title)
            content(R.string.not_connected_content)
            positiveText(android.R.string.ok)
        }
        actionDialog?.show()
    }
    
    internal fun properlyCancelDialog() {
        wallActions?.stopActions()
        wallActions?.dismiss(this)
        wallActions = null
        actionDialog?.dismiss()
        actionDialog = null
    }
    
    private fun showSnackbar(@StringRes text:Int, duration:Int, settings:Snackbar.() -> Unit = {}) {
        showSnackbar(getString(text), duration, settings)
    }
    
    abstract fun showSnackbar(text:String, duration:Int, settings:Snackbar.() -> Unit = {})
}