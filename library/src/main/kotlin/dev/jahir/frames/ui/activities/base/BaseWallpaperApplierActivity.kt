package dev.jahir.frames.ui.activities.base

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.Observer
import androidx.work.WorkInfo
import com.google.android.material.snackbar.Snackbar
import dev.jahir.frames.R
import dev.jahir.frames.data.Preferences
import dev.jahir.frames.data.workers.WallpaperApplier
import dev.jahir.frames.data.workers.WallpaperApplier.Companion.APPLY_EXTERNAL_KEY
import dev.jahir.frames.data.workers.WallpaperApplier.Companion.APPLY_OPTION_KEY
import dev.jahir.frames.data.workers.WallpaperDownloader.Companion.DOWNLOAD_PATH_KEY
import dev.jahir.frames.extensions.context.string
import dev.jahir.frames.extensions.resources.getUri
import dev.jahir.frames.extensions.views.snackbar
import java.io.File

@Suppress("MemberVisibilityCanBePrivate")
abstract class BaseWallpaperApplierActivity<out P : Preferences> :
    BaseWallpaperFetcherActivity<P>() {

    fun startApply(applyOption: Int) {
        cancelWorkManagerTasks()
        val newApplyTask = WallpaperApplier.buildRequest(wallpaperDownloadUrl, applyOption)
        newApplyTask?.let { task ->
            workManager.enqueue(newApplyTask)
            workManager.getWorkInfoByIdLiveData(task.id)
                .observe(this, Observer { info ->
                    if (info != null) {
                        if (info.state.isFinished) {
                            if (info.state == WorkInfo.State.SUCCEEDED) {
                                val option = info.outputData.getInt(APPLY_OPTION_KEY, -1)
                                if (option == APPLY_EXTERNAL_KEY) {
                                    onWallpaperReadyToBeApplied(
                                        info.outputData.getString(DOWNLOAD_PATH_KEY) ?: ""
                                    )
                                } else onWallpaperApplied()
                            } else if (info.state == WorkInfo.State.FAILED) {
                                onDownloadError()
                            }
                        } else if (info.state == WorkInfo.State.ENQUEUED) {
                            onWallpaperApplicationEnqueued(applyOption)
                        }
                    }
                })
        }
    }

    private fun onWallpaperApplicationEnqueued(applyOption: Int) {
        try {
            currentSnackbar = snackbar(
                if (applyOption == APPLY_EXTERNAL_KEY) R.string.applying_preparing
                else R.string.applying_wallpaper_def,
                Snackbar.LENGTH_INDEFINITE,
                snackbarAnchorId
            )
        } catch (e: Exception) {
        }
    }

    private fun onWallpaperApplied() {
        try {
            currentSnackbar = snackbar(R.string.applying_applied, anchorViewId = snackbarAnchorId)
        } catch (e: Exception) {
        }
        cancelWorkManagerTasks()
    }

    open fun onWallpaperReadyToBeApplied(path: String) {
        currentSnackbar?.dismiss()
        val file = File(path)
        val fileUri: Uri? = file.getUri(this) ?: Uri.fromFile(file)
        fileUri?.let {
            val setWall = Intent(Intent.ACTION_ATTACH_DATA)
            setWall.setDataAndType(it, "image/*")
            setWall.putExtra("mimeType", "image/*")
            setWall.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

            val externalApplyLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {result->
                if (result.resultCode != 0) onWallpaperApplied()
                else onDownloadError()
            }
            val chooserIntent = Intent.createChooser(setWall, string(R.string.apply_w_external_app))
            try {
                externalApplyLauncher.launch(chooserIntent)
            } catch (e: Exception) {
                onDownloadError()
            }
        } ?: { onDownloadError() }()
        cancelWorkManagerTasks()
    }

    companion object {
        private const val APPLY_WITH_OTHER_APP_CODE = 575
    }
}
