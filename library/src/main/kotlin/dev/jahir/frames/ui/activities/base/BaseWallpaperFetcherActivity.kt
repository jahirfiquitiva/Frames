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
import dev.jahir.frames.data.workers.WallpaperDownloader.Companion.DOWNLOAD_IS_LOCAL
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

    internal fun initWallpaperFetcher(wallpaper: Wallpaper?) {
        wallpaperDownloadUrl = wallpaper?.url.orEmpty()
    }

    internal fun startDownload() {
        cancelWorkManagerTasks()
        val newDownloadTask = WallpaperDownloader.buildRequest(wallpaperDownloadUrl)
        newDownloadTask?.let { task ->
            workManager.enqueue(newDownloadTask)
            workManager.getWorkInfoByIdLiveData(task.id)
                .observe(this) { info ->
                    if (info != null && info.state.isFinished) {
                        if (info.state == WorkInfo.State.SUCCEEDED) {
                            val path = info.outputData.getString(DOWNLOAD_PATH_KEY) ?: ""
                            val existed = info.outputData.getBoolean(DOWNLOAD_FILE_EXISTED, false)
                            val isLocal = info.outputData.getBoolean(DOWNLOAD_IS_LOCAL, false)
                            if (existed) onDownloadComplete(path, true)
                            else if (isLocal) onDownloadComplete(path, false)
                            else onDownloadQueued()
                        } else if (info.state == WorkInfo.State.FAILED) {
                            onDownloadError()
                        }
                    }
                }
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
        } catch (_: Exception) {
        }
    }

    private fun onDownloadQueued() {
        try {
            currentSnackbar = snackbar(R.string.download_starting, anchorViewId = snackbarAnchorId)
        } catch (_: Exception) {
        }
        cancelWorkManagerTasks()
    }

    private fun onDownloadComplete(path: String, existing: Boolean) {
        try {
            val file = File(path)
            val fileUri: Uri? = file.getUri(this) ?: Uri.fromFile(file)
            currentSnackbar =
                snackbar(
                    if (existing) R.string.downloaded_previously else R.string.download_successful_short,
                    Snackbar.LENGTH_LONG,
                    snackbarAnchorId
                ) {
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
        } catch (_: Exception) {
        }
        cancelWorkManagerTasks()
    }

    internal fun onDownloadError() {
        try {
            currentSnackbar =
                snackbar(R.string.unexpected_error_occurred, anchorViewId = snackbarAnchorId)
        } catch (_: Exception) {
        }
        cancelWorkManagerTasks()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        try {
            outState.putString(WALLPAPER_URL_KEY, wallpaperDownloadUrl)
        } catch (_: Exception) {
        }
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        try {
            wallpaperDownloadUrl = savedInstanceState.getString(WALLPAPER_URL_KEY, "") ?: ""
        } catch (_: Exception) {
        }
    }

    companion object {
        private const val WALLPAPER_URL_KEY = "wallpaper_download_url"
    }
}
