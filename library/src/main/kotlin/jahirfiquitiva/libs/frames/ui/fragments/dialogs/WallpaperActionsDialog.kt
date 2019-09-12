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
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import ca.allanwang.kau.utils.snackbar
import ca.allanwang.kau.utils.toast
import com.afollestad.materialdialogs.MaterialDialog
import jahirfiquitiva.libs.archhelpers.extensions.mdDialog
import jahirfiquitiva.libs.frames.R
import jahirfiquitiva.libs.frames.data.models.Wallpaper
import jahirfiquitiva.libs.frames.helpers.extensions.adjustToDeviceScreen
import jahirfiquitiva.libs.frames.helpers.utils.FL
import jahirfiquitiva.libs.frames.ui.activities.base.BaseWallpaperActionsActivity
import jahirfiquitiva.libs.kext.extensions.activity
import jahirfiquitiva.libs.kext.extensions.actv
import jahirfiquitiva.libs.kext.extensions.context
import jahirfiquitiva.libs.kext.helpers.DownloadThread
import java.io.File
import java.util.Locale

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
    private var downloadId = 0L
    
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        if (destFile == null && destBitmap == null) {
            return actv.mdDialog {
                title(R.string.error_title)
                message(R.string.action_error_content)
                positiveButton(android.R.string.ok) {
                    it.dismiss()
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
                fileUri ?: return actv.mdDialog()
                
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
                
                context { ctxt ->
                    downloadManager?.let {
                        thread = DownloadThread(
                            ctxt, downloadId, it, destFile,
                            object : DownloadThread.DownloadListener {
                                override fun onFailure(exception: Exception) {
                                    doOnFailure(exception)
                                }
                                
                                override fun onProgress(progress: Int) {
                                    dialog?.let {
                                        it.setCancelable(progress > 0)
                                        it.setCanceledOnTouchOutside(progress > 0)
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
                return actv.mdDialog()
            }
        }
        return actv.mdDialog()
    }
    
    private fun actuallyBuildDialog(): MaterialDialog {
        val dialog = actv.mdDialog {
            message(
                text = getString(
                    if (shouldApply && !toOtherApp) R.string.applying_wallpaper
                    else R.string.downloading_wallpaper,
                    wallpaper?.name.orEmpty()))
            positiveButton(android.R.string.cancel) {
                stopActions()
                it.dismiss()
            }
            cancelable(false)
            cancelOnTouchOutside(false)
        }
        thread?.start()
        return dialog
    }
    
    private fun buildApplyDialog(): MaterialDialog {
        val dialog = actv.mdDialog {
            message(text = getString(R.string.applying_wallpaper, wallpaper?.name))
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
                    FL.e(e.message)
                }
            } else {
                showDownloadResult(it)
            }
        }
        activity { dismiss(it) }
    }
    
    private fun doOnFailure(e: Exception) {
        stopActions()
        FL.e(e.message)
        try {
            if (isVisible || isActive) {
                try {
                    activity {
                        it.mdDialog {
                            title(R.string.error_title)
                            message(R.string.action_error_content)
                            positiveButton(android.R.string.ok) {
                                it.dismiss()
                            }
                        }
                    }
                } catch (e: Exception) {
                    FL.e(e.message)
                    (activity as? BaseWallpaperActionsActivity<*>)?.properlyCancelDialog()
                    activity { it.toast(R.string.action_error_content) }
                }
            } else {
                (activity as? BaseWallpaperActionsActivity<*>)?.properlyCancelDialog()
                activity { it.toast(R.string.action_error_content) }
            }
        } catch (e: Exception) {
            FL.e(e.message)
        }
    }
    
    fun stopActions() {
        try {
            /* TODO: Test safety
            thread?.cancel()
            downloadManager?.remove(downloadId)
            */
        } catch (e: Exception) {
            FL.e(e.message)
        }
    }
    
    private fun showDownloadResult(dest: File) {
        try {
            (activity as? BaseWallpaperActionsActivity<*>)?.reportWallpaperDownloaded(dest) ?: {
                activity?.snackbar(getString(R.string.download_successful, dest.toString()))
            }()
        } catch (e: Exception) {
            FL.e(e.message)
            activity?.toast(R.string.download_successful_short)
        }
    }
    
    private fun applyWallpaper(resource: Bitmap) {
        if (toOtherApp) {
            destFile?.let {
                (activity as? BaseWallpaperActionsActivity<*>)?.applyWallpaperWithOtherApp(it)
            }
        } else {
            try {
                destFile?.delete()
            } catch (e: Exception) {
                FL.e(e.message)
            }
            
            val wm = WallpaperManager.getInstance(activity)
            val finalResource = try {
                resource.adjustToDeviceScreen(activity)
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
                                finalResource, null, true, WallpaperManager.FLAG_SYSTEM)
                            toLockScreen -> wm.setBitmap(
                                finalResource, null, true, WallpaperManager.FLAG_LOCK)
                            else -> FL.e("The unexpected case has happened :O")
                        }
                    }
                } else {
                    wm.setBitmap(finalResource)
                }
                
                showAppliedResult()
            } catch (e: Exception) {
                FL.e(e.message)
            }
        }
    }
    
    private fun showAppliedResult() {
        activity { dismiss(it) }
        try {
            (activity as? BaseWallpaperActionsActivity<*>)?.showWallpaperAppliedSnackbar(
                toHomeScreen, toLockScreen, toBoth) ?: activity?.snackbar(
                getString(
                    R.string.apply_successful,
                    getString(
                        when {
                            toBoth -> R.string.home_lock_screen
                            toHomeScreen -> R.string.home_screen
                            toLockScreen -> R.string.lock_screen
                            else -> R.string.empty
                        }).toLowerCase(Locale.ROOT)))
        } catch (e: Exception) {
            FL.e(e.message)
            activity?.toast(R.string.apply_successful_short)
        }
    }
    
    companion object {
        private const val TAG = "icon_dialog"
        private const val WALLPAPER = "wallpaper"
        private const val TO_HOME_SCREEN = "to_home_screen"
        private const val TO_LOCK_SCREEN = "to_lock_screen"
        private const val TO_BOTH = "to_both"
        private const val TO_OTHER_APP = "to_other_app"
        const val TO_OTHER_APP_CODE = 73
        
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
                    FL.e(e.message)
                }
            }
        
        fun show(context: FragmentActivity, wallpaper: Wallpaper, destFile: File) {
            create(context, wallpaper, destFile).show(context)
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
            create(context, wallpaper, destFile, null, whatTo).show(context)
        }
        
        fun show(
            context: FragmentActivity,
            wallpaper: Wallpaper,
            destBitmap: Bitmap,
            whatTo: Array<Boolean>
                ) {
            if (whatTo.size < 4) return
            create(context, wallpaper, destBitmap, whatTo).show(context)
        }
    }
    
    fun show(activity: FragmentActivity) {
        dismiss(activity)
        show(activity.supportFragmentManager, TAG)
    }
    
    fun dismiss(activity: FragmentActivity) {
        try {
            val frag = activity.supportFragmentManager.findFragmentByTag(TAG)
            (frag as? WallpaperActionsDialog)?.internalDismiss()
        } catch (ignored: Exception) {
        }
        internalDismiss()
    }
    
    private fun internalDismiss() {
        try {
            dismiss()
        } catch (ignored: Exception) {
            try {
                dismissAllowingStateLoss()
            } catch (ignored: Exception) {
            }
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
    
    override fun onDismiss(dialog: DialogInterface) {
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
