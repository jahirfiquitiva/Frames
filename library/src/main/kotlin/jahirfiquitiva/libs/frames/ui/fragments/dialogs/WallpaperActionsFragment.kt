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
package jahirfiquitiva.libs.frames.ui.fragments.dialogs

import android.app.Dialog
import android.app.DownloadManager
import android.app.WallpaperManager
import android.content.Context
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
import jahirfiquitiva.libs.frames.helpers.extensions.bestBitmapConfig
import jahirfiquitiva.libs.frames.helpers.extensions.buildMaterialDialog
import jahirfiquitiva.libs.frames.helpers.extensions.openWallpaper
import jahirfiquitiva.libs.frames.helpers.utils.DownloadThread
import jahirfiquitiva.libs.frames.ui.activities.ViewerActivity
import jahirfiquitiva.libs.kauextensions.extensions.getUri
import jahirfiquitiva.libs.kauextensions.extensions.printError
import jahirfiquitiva.libs.kauextensions.extensions.showToast
import java.io.File

class WallpaperActionsFragment:DialogFragment() {
    
    private var wallpaper:Wallpaper? = null
    private var thread:DownloadThread? = null
    
    private var toHomeScreen = false
    private var toLockScreen = false
    private var toBoth = false
    
    private val shouldApply
        get() = toHomeScreen || toLockScreen || toBoth
    
    var destBitmap:Bitmap? = null
        private set
    var destFile:File? = null
        private set
    var downloadManager:DownloadManager? = null
        private set
    var downloadId = 0L
        private set
    
    override fun onCreateDialog(savedInstanceState:Bundle?):Dialog {
        wallpaper?.let {
            when {
                destFile != null -> {
                    val request = DownloadManager.Request(Uri.parse(it.url))
                            .setTitle(it.name)
                            .setDescription(
                                    context.getString(R.string.downloading_wallpaper, it.name))
                            .setDestinationUri(Uri.fromFile(destFile))
                    
                    if (shouldApply)
                        request.setVisibleInDownloadsUi(false)
                    else
                        request.allowScanningByMediaScanner()
                    
                    downloadId = downloadManager?.enqueue(request) ?: 0L
                    
                    thread = DownloadThread(this, object:DownloadThread.DownloadListener {
                        override fun onFailure() {
                            doOnFailure()
                        }
                        
                        override fun onProgress(progress:Int) {
                            dialog?.let {
                                (it as? MaterialDialog)?.setProgress(progress)
                            }
                        }
                        
                        override fun onSuccess() {
                            doOnSuccess()
                        }
                    })
                    
                    return actuallyBuildDialog()
                }
                destBitmap != null -> {
                    destBitmap?.let {
                        applyWallpaper(it)
                    }
                    return buildApplyDialog()
                }
                else -> return activity.buildMaterialDialog { }
            }
        }
        return activity.buildMaterialDialog { }
    }
    
    private fun actuallyBuildDialog():MaterialDialog {
        val dialog = activity.buildMaterialDialog {
            content(activity.getString(
                    if (shouldApply) R.string.applying_wallpaper else R.string.downloading_wallpaper,
                    wallpaper?.name))
            progress(false, 100)
            positiveText(android.R.string.cancel)
            onPositive { _, _ ->
                stopActions()
                dismiss(activity)
            }
        }
        thread?.start()
        return dialog
    }
    
    private fun buildApplyDialog():MaterialDialog {
        val dialog = activity.buildMaterialDialog {
            content(activity.getString(R.string.applying_wallpaper, wallpaper?.name))
            progress(true, 0)
        }
        thread?.start()
        return dialog
    }
    
    private fun doOnSuccess() {
        if (activity != null) {
            activity?.let {
                destFile?.let {
                    if (shouldApply) {
                        val options = BitmapFactory.Options()
                        options.inPreferredConfig = activity.bestBitmapConfig
                        try {
                            val resource = BitmapFactory.decodeFile(it.absolutePath, options)
                            try {
                                it.delete()
                            } catch (e:Exception) {
                                e.printStackTrace()
                            }
                            resource?.let { applyWallpaper(it) }
                        } catch (e:Exception) {
                            e.printStackTrace()
                        }
                    } else {
                        showDownloadResult(it)
                    }
                }
                dismiss(it)
            }
        } else dismiss()
    }
    
    private fun doOnFailure() {
        stopActions()
        try {
            if (isVisible) {
                activity.materialDialog {
                    title(R.string.error_title)
                    content(R.string.action_error_content)
                    positiveText(android.R.string.ok)
                    onPositive { dialog, _ ->
                        dialog.dismiss()
                        dismiss(activity)
                    }
                }
            } else {
                activity.showToast(R.string.action_error_content)
            }
        } catch (e:Exception) {
            e.printStackTrace()
        }
    }
    
    fun stopActions() {
        try {
            thread?.cancel()
            downloadManager?.remove(downloadId)
        } catch (e:Exception) {
            e.printStackTrace()
        }
    }
    
    private fun showDownloadResult(dest:File) {
        try {
            if (activity is ViewerActivity) {
                (activity as ViewerActivity).showWallpaperDownloadedSnackbar(dest)
            } else {
                activity.snackbar(getString(R.string.download_successful, dest.toString()),
                                  builder = {
                                      setAction(R.string.open, {
                                          destFile?.getUri(activity)?.let {
                                              activity.openWallpaper(it)
                                          }
                                      })
                                  })
            }
        } catch (e:Exception) {
            e.printStackTrace()
            activity.showToast(R.string.download_successful_short)
        }
    }
    
    private fun applyWallpaper(resource:Bitmap) {
        val wm = WallpaperManager.getInstance(activity)
        val finalResource = try {
            resource.adjustToDeviceScreen(activity)
        } catch (ignored:Exception) {
            resource
        }
        
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                if (toBoth) {
                    wm.setBitmap(finalResource, null, true)
                } else {
                    when {
                        toHomeScreen -> wm.setBitmap(finalResource, null, true,
                                                     WallpaperManager.FLAG_SYSTEM)
                        toLockScreen -> wm.setBitmap(finalResource, null, true,
                                                     WallpaperManager.FLAG_LOCK)
                        else -> activity.printError("The unexpected case has happened :O")
                    }
                }
            } else {
                wm.setBitmap(finalResource)
            }
            
            showAppliedResult()
        } catch (e:Exception) {
            e.printStackTrace()
        }
    }
    
    private fun showAppliedResult() {
        dismiss(activity)
        try {
            if (activity is ViewerActivity) {
                (activity as ViewerActivity).showWallpaperAppliedSnackbar(toHomeScreen,
                                                                          toLockScreen, toBoth)
            } else {
                activity.snackbar(getString(R.string.apply_successful,
                                            getString(when {
                                                          toBoth -> R.string.home_lock_screen
                                                          toHomeScreen -> R.string.home_screen
                                                          toLockScreen -> R.string.lock_screen
                                                          else -> R.string.empty
                                                      }).toLowerCase()))
            }
        } catch (e:Exception) {
            e.printStackTrace()
            activity.showToast(R.string.apply_successful_short)
        }
    }
    
    companion object {
        private val TAG = "icon_dialog"
        private val WALLPAPER = "wallpaper"
        private val TO_HOME_SCREEN = "to_home_screen"
        private val TO_LOCK_SCREEN = "to_lock_screen"
        private val TO_BOTH = "to_both"
        
        fun invoke(context:FragmentActivity, wallpaper:Wallpaper, destFile:File?,
                   destBitmap:Bitmap?, toHomeScreen:Boolean, toLockScreen:Boolean,
                   toBoth:Boolean):WallpaperActionsFragment =
                WallpaperActionsFragment().apply {
                    this.downloadManager =
                            context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager?
                    this.wallpaper = wallpaper
                    this.destFile = destFile
                    this.destBitmap = destBitmap
                    this.toHomeScreen = toHomeScreen
                    this.toLockScreen = toLockScreen
                    this.toBoth = toBoth
                }
    }
    
    fun show(context:FragmentActivity, wallpaper:Wallpaper, destFile:File) {
        dismiss(context)
        invoke(context, wallpaper, destFile, null, false, false, false)
                .show(context.supportFragmentManager, TAG)
    }
    
    fun show(context:FragmentActivity, wallpaper:Wallpaper, destFile:File,
             toHomeScreen:Boolean, toLockScreen:Boolean, toBoth:Boolean) {
        dismiss(context)
        invoke(context, wallpaper, destFile, null, toHomeScreen, toLockScreen, toBoth)
                .show(context.supportFragmentManager, TAG)
    }
    
    fun show(context:FragmentActivity, wallpaper:Wallpaper, destBitmap:Bitmap,
             toHomeScreen:Boolean, toLockScreen:Boolean, toBoth:Boolean) {
        dismiss(context)
        invoke(context, wallpaper, null, destBitmap, toHomeScreen, toLockScreen, toBoth)
                .show(context.supportFragmentManager, TAG)
    }
    
    fun dismiss(context:FragmentActivity) {
        try {
            val frag = context.supportFragmentManager.findFragmentByTag(TAG)
            if (frag != null) (frag as WallpaperActionsFragment).dismiss()
        } catch (ignored:Exception) {
        }
        try {
            dismiss()
        } catch (ignored:Exception) {
        }
    }
    
    override fun onActivityCreated(savedInstanceState:Bundle?) {
        super.onActivityCreated(savedInstanceState)
        savedInstanceState?.let {
            wallpaper = it.getParcelable(WALLPAPER)
            toHomeScreen = it.getBoolean(TO_HOME_SCREEN)
            toLockScreen = it.getBoolean(TO_LOCK_SCREEN)
            toBoth = it.getBoolean(TO_BOTH)
        }
    }
    
    override fun onSaveInstanceState(outState:Bundle?) {
        outState?.let {
            it.putParcelable(WALLPAPER, wallpaper)
            it.putBoolean(TO_HOME_SCREEN, toHomeScreen)
            it.putBoolean(TO_LOCK_SCREEN, toLockScreen)
            it.putBoolean(TO_BOTH, toBoth)
        }
        super.onSaveInstanceState(outState)
    }
}