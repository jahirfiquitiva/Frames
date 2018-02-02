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
package jahirfiquitiva.libs.frames.ui.fragments.dialogs

import android.app.Dialog
import android.app.DownloadManager
import android.app.WallpaperManager
import android.content.Context
import android.content.DialogInterface
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v4.app.FragmentActivity
import ca.allanwang.kau.utils.materialDialog
import ca.allanwang.kau.utils.snackbar
import com.afollestad.materialdialogs.MaterialDialog
import jahirfiquitiva.libs.frames.R
import jahirfiquitiva.libs.frames.data.models.Wallpaper
import jahirfiquitiva.libs.frames.helpers.extensions.adjustToDeviceScreen
import jahirfiquitiva.libs.frames.helpers.extensions.buildMaterialDialog
import jahirfiquitiva.libs.frames.helpers.extensions.openWallpaper
import jahirfiquitiva.libs.frames.helpers.utils.FL
import jahirfiquitiva.libs.frames.ui.activities.base.BaseWallpaperActionsActivity
import jahirfiquitiva.libs.kauextensions.extensions.actv
import jahirfiquitiva.libs.kauextensions.extensions.ctxt
import jahirfiquitiva.libs.kauextensions.extensions.showToast
import jahirfiquitiva.libs.kauextensions.extensions.withActv
import jahirfiquitiva.libs.kauextensions.helpers.DownloadThread
import java.io.File

@Suppress("DEPRECATION")
class WallpaperActionsDialog : DialogFragment() {
    
    private var wallpaper: Wallpaper? = null
    private var thread: DownloadThread? = null
    
    private var toHomeScreen = false
    private var toLockScreen = false
    private var toBoth = false
    
    private var toOtherApp = false
    
    private val shouldApply
        get() = toHomeScreen || toLockScreen || toBoth || toOtherApp
    
    private var isActive = false
        set(value) {
            if (value != field) field = value
        }
    
    var destBitmap: Bitmap? = null
        private set
    var destFile: File? = null
        private set
    var downloadManager: DownloadManager? = null
        private set
    var downloadId = 0L
        private set
    
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        if (destFile == null && destBitmap == null) {
            return actv.buildMaterialDialog {
                title(R.string.error_title)
                content(R.string.action_error_content)
                positiveText(android.R.string.ok)
                onPositive { dialog, _ ->
                    dialog.dismiss()
                    actv { dismiss(it) }
                }
            }
        }
        wallpaper?.let {
            @Suppress("CascadeIf")
            if (destFile != null) {
                
                destFile?.let {
                    try {
                        if (it.exists()) it.delete()
                    } catch (e: Exception) {
                    }
                }
                
                val fileUri: Uri? = Uri.fromFile(destFile)
                fileUri ?: return actv.buildMaterialDialog()
                
                val request = DownloadManager.Request(Uri.parse(it.url))
                        .setTitle(it.name)
                        .setDescription(getString(R.string.downloading_wallpaper, it.name))
                        .setDestinationUri(fileUri)
                        .setAllowedOverRoaming(false)
                
                if (shouldApply) {
                    request.setVisibleInDownloadsUi(false)
                } else {
                    request.setNotificationVisibility(
                            DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                    request.allowScanningByMediaScanner()
                }
                
                downloadId = downloadManager?.enqueue(request) ?: 0L
                
                ctxt { context ->
                    downloadManager?.let {
                        thread = DownloadThread(
                                context, downloadId, it, destFile,
                                object : DownloadThread.DownloadListener {
                                    override fun onFailure(exception: Exception) {
                                        doOnFailure(exception)
                                    }
                                    
                                    override fun onProgress(progress: Int) {
                                        dialog?.let {
                                            it.setCancelable(progress > 0)
                                            it.setCanceledOnTouchOutside(progress > 0)
                                            (it as? MaterialDialog)?.setProgress(progress)
                                        }
                                    }
                                    
                                    override fun onSuccess(file: File?) {
                                        doOnSuccess()
                                    }
                                })
                    }
                }
                
                return actuallyBuildDialog()
            } else if (destBitmap != null) {
                destBitmap?.let {
                    applyWallpaper(it)
                }
                return buildApplyDialog()
            } else {
                return actv.buildMaterialDialog()
            }
        }
        return actv.buildMaterialDialog()
    }
    
    private fun actuallyBuildDialog(): MaterialDialog {
        val dialog = actv.buildMaterialDialog {
            content(
                    actv.getString(
                            if (shouldApply && !toOtherApp) R.string.applying_wallpaper
                            else R.string.downloading_wallpaper,
                            wallpaper?.name.orEmpty()))
            progress(false, 100)
            positiveText(android.R.string.cancel)
            cancelable(false)
            canceledOnTouchOutside(false)
            onPositive { _, _ ->
                stopActions()
                actv { dismiss(it) }
            }
        }
        thread?.start()
        return dialog
    }
    
    private fun buildApplyDialog(): MaterialDialog {
        val dialog = actv.buildMaterialDialog {
            content(actv.getString(R.string.applying_wallpaper, wallpaper?.name))
            progress(true, 0)
        }
        thread?.start()
        return dialog
    }
    
    private fun doOnSuccess() {
        destFile?.let {
            if (shouldApply) {
                try {
                    val resource = BitmapFactory.decodeFile(it.absolutePath)
                    resource?.let { applyWallpaper(it) }
                } catch (e: Exception) {
                    FL.e { e.message }
                }
            } else {
                showDownloadResult(it)
            }
        }
        actv { dismiss(it) }
    }
    
    private fun doOnFailure(e: Exception) {
        stopActions()
        FL.e { e.message }
        try {
            if (isVisible || isActive) {
                try {
                    withActv {
                        materialDialog {
                            title(R.string.error_title)
                            content(R.string.action_error_content)
                            positiveText(android.R.string.ok)
                            onPositive { dialog, _ ->
                                dialog.dismiss()
                                actv { dismiss(it) }
                            }
                        }
                    }
                } catch (e: Exception) {
                    FL.e { e.message }
                    (actv as? BaseWallpaperActionsActivity)?.properlyCancelDialog()
                    withActv { showToast(R.string.action_error_content) }
                }
            } else {
                (actv as? BaseWallpaperActionsActivity)?.properlyCancelDialog()
                withActv { showToast(R.string.action_error_content) }
            }
        } catch (e: Exception) {
            FL.e { e.message }
        }
    }
    
    fun stopActions() {
        try {
            /* TODO: Test safety
            thread?.cancel()
            downloadManager?.remove(downloadId)
            */
        } catch (e: Exception) {
            FL.e { e.message }
        }
    }
    
    private fun showDownloadResult(dest: File) {
        try {
            actv { activity ->
                (activity as? BaseWallpaperActionsActivity)?.reportWallpaperDownloaded(dest) ?: {
                    activity.snackbar(getString(R.string.download_successful, dest.toString())) {
                        setAction(
                                R.string.open, {
                            activity.openWallpaper(Uri.fromFile(dest))
                        })
                    }
                }()
            }
        } catch (e: Exception) {
            FL.e { e.message }
            withActv { showToast(R.string.download_successful_short) }
        }
    }
    
    private fun applyWallpaper(resource: Bitmap) {
        if (toOtherApp) {
            destFile?.let {
                (actv as? BaseWallpaperActionsActivity)?.applyWallpaperWithOtherApp(it)
            }
        } else {
            try {
                destFile?.delete()
            } catch (e: Exception) {
                FL.e { e.message }
            }
            
            val wm = WallpaperManager.getInstance(activity)
            val finalResource = try {
                resource.adjustToDeviceScreen(actv)
            } catch (ignored: Exception) {
                resource
            }
            
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    if (toBoth) {
                        wm.setBitmap(finalResource, null, true)
                    } else {
                        when {
                            toHomeScreen -> wm.setBitmap(
                                    finalResource, null, true,
                                    WallpaperManager.FLAG_SYSTEM)
                            toLockScreen -> wm.setBitmap(
                                    finalResource, null, true,
                                    WallpaperManager.FLAG_LOCK)
                            else -> FL.e(null, { "The unexpected case has happened :O" })
                        }
                    }
                } else {
                    wm.setBitmap(finalResource)
                }
                
                showAppliedResult()
            } catch (e: Exception) {
                FL.e { e.message }
            }
        }
    }
    
    private fun showAppliedResult() {
        actv { dismiss(it) }
        try {
            actv {
                (it as? BaseWallpaperActionsActivity)?.showWallpaperAppliedSnackbar(
                        toHomeScreen, toLockScreen, toBoth) ?: it.snackbar(
                        getString(
                                R.string.apply_successful,
                                getString(
                                        when {
                                            toBoth -> R.string.home_lock_screen
                                            toHomeScreen -> R.string.home_screen
                                            toLockScreen -> R.string.lock_screen
                                            else -> R.string.empty
                                        }).toLowerCase()))
            }
        } catch (e: Exception) {
            FL.e { e.message }
            actv { it.showToast(R.string.apply_successful_short) }
        }
    }
    
    companion object {
        private val TAG = "icon_dialog"
        private val WALLPAPER = "wallpaper"
        private val TO_HOME_SCREEN = "to_home_screen"
        private val TO_LOCK_SCREEN = "to_lock_screen"
        private val TO_BOTH = "to_both"
        private val TO_OTHER_APP = "to_other_app"
        val TO_OTHER_APP_CODE = 73
        
        fun create(
                context: FragmentActivity,
                wallpaper: Wallpaper,
                destFile: File?,
                destBitmap: Bitmap? = null,
                whatTo: Array<Boolean>
                  ): WallpaperActionsDialog =
                WallpaperActionsDialog().apply {
                    this.downloadManager =
                            context.getSystemService(Context.DOWNLOAD_SERVICE) as? DownloadManager?
                    this.wallpaper = wallpaper
                    this.destFile = destFile
                    this.destBitmap = destBitmap
                    try {
                        this.toHomeScreen = whatTo[0]
                        this.toLockScreen = whatTo[1]
                        this.toBoth = whatTo[2]
                        this.toOtherApp = whatTo[3]
                    } catch (e: Exception) {
                        FL.e { e.message }
                    }
                }
        
        fun show(context: FragmentActivity, wallpaper: Wallpaper, destFile: File) {
            create(context, wallpaper, destFile).show(context.supportFragmentManager, TAG)
        }
        
        fun create(
                context: FragmentActivity,
                wallpaper: Wallpaper,
                destFile: File
                  ) = create(
                context, wallpaper, destFile, null, arrayOf(false, false, false, false))
        
        fun create(
                context: FragmentActivity,
                wallpaper: Wallpaper,
                destFile: File,
                whatTo: Array<Boolean>
                  ) = create(context, wallpaper, destFile, null, whatTo)
        
        fun create(
                context: FragmentActivity,
                wallpaper: Wallpaper,
                destBitmap: Bitmap?,
                whatTo: Array<Boolean>
                  ) = create(context, wallpaper, null, destBitmap, whatTo)
        
        fun show(
                context: FragmentActivity,
                wallpaper: Wallpaper,
                destFile: File,
                whatTo: Array<Boolean>
                ) {
            if (whatTo.size < 4) return
            create(context, wallpaper, destFile, null, whatTo)
                    .show(context.supportFragmentManager, TAG)
        }
        
        fun show(
                context: FragmentActivity,
                wallpaper: Wallpaper,
                destBitmap: Bitmap,
                whatTo: Array<Boolean>
                ) {
            if (whatTo.size < 4) return
            create(context, wallpaper, destBitmap, whatTo)
                    .show(context.supportFragmentManager, TAG)
        }
    }
    
    fun show(activity: FragmentActivity) {
        show(activity.supportFragmentManager, TAG)
    }
    
    fun dismiss(activity: FragmentActivity) {
        try {
            val frag = activity.supportFragmentManager.findFragmentByTag(TAG)
            if (frag != null) (frag as? WallpaperActionsDialog)?.dismiss()
        } catch (ignored: Exception) {
        }
        try {
            dismiss()
        } catch (ignored: Exception) {
        }
    }
    
    override fun onStart() {
        super.onStart()
        isActive = true
    }
    
    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        isActive = isVisibleToUser
    }
    
    override fun onDismiss(dialog: DialogInterface?) {
        super.onDismiss(dialog)
        isActive = false
    }
    
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        savedInstanceState?.let {
            wallpaper = it.getParcelable(WALLPAPER)
            toHomeScreen = it.getBoolean(TO_HOME_SCREEN)
            toLockScreen = it.getBoolean(TO_LOCK_SCREEN)
            toBoth = it.getBoolean(TO_BOTH)
            toOtherApp = it.getBoolean(TO_OTHER_APP)
        }
    }
    
    override fun onSaveInstanceState(outState: Bundle) {
        with(outState) {
            putParcelable(WALLPAPER, wallpaper)
            putBoolean(TO_HOME_SCREEN, toHomeScreen)
            putBoolean(TO_LOCK_SCREEN, toLockScreen)
            putBoolean(TO_BOTH, toBoth)
            putBoolean(TO_OTHER_APP, toOtherApp)
        }
        super.onSaveInstanceState(outState)
    }
}