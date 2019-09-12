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
package jahirfiquitiva.libs.frames.ui.activities.base

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.annotation.CallSuper
import androidx.annotation.StringRes
import ca.allanwang.kau.utils.isNetworkAvailable
import com.afollestad.materialdialogs.MaterialDialog
import com.fondesa.kpermissions.extension.listeners
import com.fondesa.kpermissions.extension.permissionsBuilder
import com.fondesa.kpermissions.request.runtime.nonce.PermissionNonce
import com.google.android.material.snackbar.Snackbar
import jahirfiquitiva.libs.archhelpers.extensions.mdDialog
import jahirfiquitiva.libs.frames.R
import jahirfiquitiva.libs.frames.data.models.Wallpaper
import jahirfiquitiva.libs.frames.helpers.utils.FL
import jahirfiquitiva.libs.frames.helpers.utils.FramesKonfigs
import jahirfiquitiva.libs.frames.helpers.utils.REQUEST_CODE
import jahirfiquitiva.libs.frames.ui.fragments.dialogs.QuickActionsBottomSheet
import jahirfiquitiva.libs.frames.ui.fragments.dialogs.WallpaperActionsDialog
import jahirfiquitiva.libs.kext.extensions.formatCorrectly
import jahirfiquitiva.libs.kext.extensions.getAppName
import jahirfiquitiva.libs.kext.extensions.getUri
import jahirfiquitiva.libs.kext.ui.activities.ActivityWFragments
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date

abstract class BaseWallpaperActionsActivity<T : FramesKonfigs> : ActivityWFragments<T>() {
    
    companion object {
        const val DOWNLOAD_ACTION_ID = 1
        const val APPLY_ACTION_ID = 2
    }
    
    private var actionDialog: MaterialDialog? = null
    internal var wallActions: WallpaperActionsDialog? = null
    
    internal abstract var wallpaper: Wallpaper?
    internal abstract val allowBitmapApply: Boolean
    
    override fun autoTintStatusBar(): Boolean = true
    override fun autoTintNavigationBar(): Boolean = true
    
    private val request by lazy {
        permissionsBuilder(Manifest.permission.WRITE_EXTERNAL_STORAGE).build()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        try {
            request.detachAllListeners()
        } catch (e: Exception) {
        }
    }
    
    open fun doItemClick(actionId: Int, customAction: Int = -1) {
        when (actionId) {
            DOWNLOAD_ACTION_ID -> downloadWallpaper(false, customAction)
            APPLY_ACTION_ID -> downloadWallpaper(true, customAction)
        }
    }
    
    fun requestStoragePermission(explanation: String, whenAccepted: () -> Unit) {
        try {
            request.detachAllListeners()
        } catch (e: Exception) {
        }
        request.listeners {
            onAccepted { whenAccepted() }
            onDenied { showSnackbar(R.string.permission_denied, Snackbar.LENGTH_LONG) }
            onPermanentlyDenied {
                showSnackbar(R.string.permission_denied_completely, Snackbar.LENGTH_LONG)
            }
            onShouldShowRationale { _, nonce -> showPermissionInformation(explanation, nonce) }
        }
        request.send()
    }
    
    @SuppressLint("NewApi")
    private fun downloadWallpaper(toApply: Boolean, customAction: Int = -1) {
        if (isNetworkAvailable) {
            requestStoragePermission(getString(R.string.permission_request, getAppName())) {
                checkIfFileExists(toApply, customAction)
            }
        } else {
            if (toApply && allowBitmapApply) showWallpaperApplyOptions(null, customAction)
            else showNotConnectedDialog()
        }
    }
    
    private fun showPermissionInformation(
        explanation: String,
        nonce: PermissionNonce
                                         ) {
        showSnackbar(explanation, Snackbar.LENGTH_LONG) {
            setAction(R.string.allow) {
                dismiss()
                nonce.use()
            }
        }
    }
    
    private fun checkIfFileExists(toApply: Boolean, customAction: Int) {
        wallpaper?.let {
            properlyCancelDialog()
            val folder = File(prefs.downloadsFolder)
            folder.mkdirs()
            val extension = it.url.substring(it.url.lastIndexOf("."))
            var correctExtension = getWallpaperExtension(extension)
            val fileName = it.name.formatCorrectly().replace(" ", "_")
            if (toApply) correctExtension = "_temp$correctExtension"
            val dest = File(folder, fileName + correctExtension)
            if (dest.exists()) {
                if (toApply) {
                    if (customAction == 3) {
                        applyForOption(
                            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) 1 else 3, dest)
                    } else {
                        showWallpaperApplyOptions(dest, customAction)
                    }
                } else {
                    actionDialog = mdDialog {
                        message(R.string.file_exists)
                        positiveButton(R.string.file_create_new) {
                            val time = getCurrentTimeStamp().formatCorrectly().replace(" ", "_")
                            val newDest = File(folder, fileName + "_" + time + correctExtension)
                            if (toApply) showWallpaperApplyOptions(newDest, customAction)
                            else startDownload(newDest)
                        }
                        negativeButton(R.string.file_replace) {
                            if (toApply) showWallpaperApplyOptions(dest, customAction)
                            else startDownload(dest)
                        }
                    }
                    actionDialog?.show()
                }
            } else {
                if (toApply) showWallpaperApplyOptions(dest, customAction)
                else startDownload(dest)
            }
        }
    }
    
    private fun startDownload(dest: File) {
        wallpaper?.let {
            properlyCancelDialog()
            wallActions = WallpaperActionsDialog.create(this, it, dest)
            wallActions?.show(this)
        }
    }
    
    fun reportWallpaperDownloaded(dest: File) {
        runOnUiThread {
            properlyCancelDialog()
            showSnackbar(
                getString(R.string.download_successful, dest.toString()),
                Snackbar.LENGTH_LONG)
        }
    }
    
    @SuppressLint("SimpleDateFormat")
    private fun getCurrentTimeStamp(): String {
        val sdfDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        return sdfDate.format(Date())
    }
    
    private fun getWallpaperExtension(currentExt: String): String {
        val validExtensions = arrayOf(".jpg", ".jpeg", ".png")
        validExtensions.forEach {
            if (currentExt.contains(it, true)) return it
        }
        return ".png"
    }
    
    private fun showWallpaperApplyOptions(dest: File?, customAction: Int = -1) {
        properlyCancelDialog()
        if (customAction < 0) {
            QuickActionsBottomSheet.show(this, wallpaper, false, dest != null)
        } else {
            applyForOption(customAction, dest)
        }
    }
    
    private fun applyForOption(index: Int, dest: File? = null) {
        val rightPosition =
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) index + 2 else index
        if (dest != null) {
            applyWallpaper(
                dest, rightPosition == 0, rightPosition == 1, rightPosition == 2,
                rightPosition == 3)
        } else {
            if (allowBitmapApply)
                applyBitmapWallpaper(
                    rightPosition == 0, rightPosition == 1, rightPosition == 2,
                    rightPosition == 3)
        }
    }
    
    abstract fun applyBitmapWallpaper(
        toHomeScreen: Boolean, toLockScreen: Boolean, toBoth: Boolean,
        toOtherApp: Boolean
                                     )
    
    private fun applyWallpaper(
        dest: File,
        toHomeScreen: Boolean, toLockScreen: Boolean, toBoth: Boolean,
        toOtherApp: Boolean
                              ) {
        wallpaper?.let {
            properlyCancelDialog()
            wallActions = WallpaperActionsDialog.create(
                this, it, dest, arrayOf(toHomeScreen, toLockScreen, toBoth, toOtherApp))
            wallActions?.show(this)
        }
    }
    
    fun showWallpaperAppliedSnackbar(
        toHomeScreen: Boolean, toLockScreen: Boolean,
        toBoth: Boolean
                                    ) {
        properlyCancelDialog()
        showSnackbar(
            getString(
                R.string.apply_successful,
                getString(
                    when {
                        toBoth -> R.string.home_lock_screen
                        toHomeScreen -> R.string.home_screen
                        toLockScreen -> R.string.lock_screen
                        else -> R.string.empty
                    }).toLowerCase()), Snackbar.LENGTH_LONG)
    }
    
    private var file: File? = null
    
    fun applyWallpaperWithOtherApp(dest: File) {
        try {
            dest.getUri(this)?.let {
                file = dest
                val setWall = Intent(Intent.ACTION_ATTACH_DATA)
                setWall.setDataAndType(it, "image/*")
                setWall.putExtra("mimeType", "image/*")
                setWall.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                startActivityForResult(
                    Intent.createChooser(setWall, getString(R.string.apply_with_other_app)),
                    WallpaperActionsDialog.TO_OTHER_APP_CODE)
            } ?: dest.delete()
        } catch (e: Exception) {
            FL.e(e.message)
        }
    }
    
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == WallpaperActionsDialog.TO_OTHER_APP_CODE) {
            try {
                file?.delete()
                file = null
            } catch (e: Exception) {
                FL.e(e.message)
            }
        }
    }
    
    private fun showNotConnectedDialog() {
        properlyCancelDialog()
        actionDialog = mdDialog {
            title(R.string.muzei_not_connected_title)
            message(R.string.not_connected_content)
            positiveButton(android.R.string.ok)
        }
        actionDialog?.show()
    }
    
    @CallSuper
    internal open fun properlyCancelDialog() {
        wallActions?.stopActions()
        wallActions?.dismiss(this)
        wallActions = null
        actionDialog?.dismiss()
        actionDialog = null
    }
    
    private fun showSnackbar(
        @StringRes text: Int,
        duration: Int = Snackbar.LENGTH_SHORT,
        defaultToToast: Boolean = true,
        settings: Snackbar.() -> Unit = {}
                            ) {
        showSnackbar(getString(text), duration, defaultToToast, settings)
    }
    
    abstract fun showSnackbar(
        text: String,
        duration: Int = Snackbar.LENGTH_SHORT,
        defaultToToast: Boolean = true,
        settings: Snackbar.() -> Unit = {}
                             )
    
    override fun startActivityForResult(intent: Intent?, requestCode: Int) {
        intent?.putExtra(REQUEST_CODE, requestCode)
        super.startActivityForResult(intent, requestCode)
    }
    
    @SuppressLint("RestrictedApi")
    override fun startActivityForResult(intent: Intent?, requestCode: Int, options: Bundle?) {
        intent?.putExtra(REQUEST_CODE, requestCode)
        super.startActivityForResult(intent, requestCode, options)
    }
}
