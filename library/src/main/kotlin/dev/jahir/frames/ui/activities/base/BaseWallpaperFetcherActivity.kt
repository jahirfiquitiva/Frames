package dev.jahir.frames.ui.activities.base

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.google.android.material.snackbar.Snackbar
import dev.jahir.frames.R
import dev.jahir.frames.data.Preferences
import dev.jahir.frames.data.models.Wallpaper
import dev.jahir.frames.data.workers.WallpaperDownloader
import dev.jahir.frames.data.workers.WallpaperDownloader.Companion.DOWNLOAD_FILE_EXISTED
import dev.jahir.frames.data.workers.WallpaperDownloader.Companion.DOWNLOAD_PATH_KEY
import dev.jahir.frames.extensions.context.toast
import dev.jahir.frames.extensions.resources.getMimeType
import dev.jahir.frames.extensions.resources.getUri
import dev.jahir.frames.extensions.views.snackbar
import java.io.File

@Suppress("MemberVisibilityCanBePrivate")
abstract class BaseWallpaperFetcherActivity<out P : Preferences> :
    BaseFavoritesConnectedActivity<P>() {

    internal val workManager: WorkManager by lazy { WorkManager.getInstance(this) }
    internal var wallpaperDownloadUrl: String = ""

    internal fun initDownload(wallpaper: Wallpaper?) {
        wallpaperDownloadUrl = wallpaper?.url.orEmpty()
    }

    internal fun startDownload() {
        cancelWorkManagerTasks()
        val newDownloadTask = WallpaperDownloader.buildRequest(wallpaperDownloadUrl)
        newDownloadTask?.let { task ->
            workManager.enqueue(newDownloadTask)
            workManager.getWorkInfoByIdLiveData(task.id)
                .observe(this, { info ->
                    if (info != null && info.state.isFinished) {
                        if (info.state == WorkInfo.State.SUCCEEDED) {
                            val path = info.outputData.getString(DOWNLOAD_PATH_KEY) ?: ""
                            val existed = info.outputData.getBoolean(DOWNLOAD_FILE_EXISTED, false)
                            if (existed) onDownloadExistent(path)
                            else onDownloadQueued()
                        } else if (info.state == WorkInfo.State.FAILED) {
                            onDownloadError()
                        }
                    }
                })
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cancelWorkManagerTasks()
    }

    fun cancelWorkManagerTasks() {
        try {
            workManager.cancelAllWork()
            workManager.pruneWork()
        } catch (e: Exception) {
        }
    }

    private fun onDownloadQueued() {
        try {
            currentSnackbar = snackbar(R.string.download_starting, anchorViewId = snackbarAnchorId)
        } catch (e: Exception) {
        }
        cancelWorkManagerTasks()
    }

    private fun onDownloadExistent(path: String) {
        try {
            val file = File(path)
            val fileUri: Uri? = file.getUri(this) ?: Uri.fromFile(file)
            currentSnackbar =
                snackbar(R.string.downloaded_previously, Snackbar.LENGTH_LONG, snackbarAnchorId) {
                    fileUri?.let {
                        setAction(R.string.open) {
                            try {
                                startActivity(Intent().apply {
                                    action = Intent.ACTION_VIEW
                                    setDataAndType(fileUri, file.getMimeType("image/*"))
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                })
                            } catch (e: Exception) {
                                toast(R.string.error)
                            }
                        }
                    }
                }
        } catch (e: Exception) {
        }
        cancelWorkManagerTasks()
    }

    internal fun onDownloadError() {
        try {
            currentSnackbar =
                snackbar(R.string.unexpected_error_occurred, anchorViewId = snackbarAnchorId)
        } catch (e: Exception) {
        }
        cancelWorkManagerTasks()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        try {
            outState.putString(WALLPAPER_URL_KEY, wallpaperDownloadUrl)
        } catch (e: Exception) {
        }
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        try {
            wallpaperDownloadUrl = savedInstanceState.getString(WALLPAPER_URL_KEY, "") ?: ""
        } catch (e: Exception) {
        }
    }

    companion object {
        private const val WALLPAPER_URL_KEY = "wallpaper_download_url"
    }
}
