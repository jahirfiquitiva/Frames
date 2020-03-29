package dev.jahir.frames.ui.fragments.viewer

import android.app.WallpaperManager
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import com.tonyodev.fetch2.Download
import dev.jahir.frames.R
import dev.jahir.frames.data.models.Wallpaper
import dev.jahir.frames.extensions.getUri
import dev.jahir.frames.extensions.prefs
import dev.jahir.frames.utils.ensureBackgroundThread
import java.io.File

class ApplierDialog : DownloadToApplyDialog() {

    private val wm: WallpaperManager? by lazy {
        try {
            WallpaperManager.getInstance(context)
        } catch (e: Exception) {
            null
        }
    }

    override fun onCompleted(download: Download) {
        super.onCompleted(download)
        val text = try {
            requireContext().getString(R.string.applying_wallpaper, wallpaper?.name.orEmpty())
        } catch (e: Exception) {
            try {
                requireContext().getString(R.string.applying_wallpaper_def)
            } catch (e: Exception) {
                ""
            }
        }
        setMessage(text)
        setAsWallpaper(download)
    }

    private fun setAsWallpaper(download: Download) {
        if (applyToOption < 0) {
            showFinalMessage()
            return
        }
        ensureBackgroundThread {
            if (applyToOption in 0..2) {
                val applyFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    when (applyToOption) {
                        0 -> WallpaperManager.FLAG_SYSTEM
                        1 -> WallpaperManager.FLAG_LOCK
                        else -> -1
                    }
                } else -1

                val bitmap = try {
                    BitmapFactory.decodeFile(download.file)
                } catch (e: Exception) {
                    null
                }

                val scaledBitmap = if (context?.prefs?.shouldCropWallpaperBeforeApply == true) {
                    bitmap?.let {
                        try {
                            val wantedHeight = wm?.desiredMinimumHeight ?: 0
                            val ratio = wantedHeight / it.height.toFloat()
                            val wantedWidth = (it.width * ratio).toInt()
                            Bitmap.createScaledBitmap(it, wantedWidth, wantedHeight, true)
                        } catch (e: Exception) {
                            it
                        }
                    }
                } else bitmap

                scaledBitmap?.let { bmp ->
                    wm?.let { wm ->
                        val result = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            if (applyFlag >= 0) {
                                wm.setBitmap(bmp, null, true, applyFlag)
                            } else wm.setBitmap(bmp, null, true)
                        } else {
                            wm.setBitmap(bmp)
                            -1
                        }
                        showFinalMessage(
                            if (result != 0) R.string.applying_applied
                            else R.string.unexpected_error_occurred
                        )
                    } ?: { showFinalMessage() }()
                } ?: { showFinalMessage() }()
            } else {
                val uri = File(filePath).getUri(context)
                uri?.let {
                    val setWall = Intent(Intent.ACTION_ATTACH_DATA)
                    setWall.setDataAndType(it, "image/*")
                    setWall.putExtra("mimeType", "image/*")
                    setWall.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    try {
                        startActivityForResult(
                            Intent.createChooser(setWall, getString(R.string.apply_w_external_app)),
                            WITH_OTHER_APP_CODE
                        )
                    } catch (e: Exception) {
                        showFinalMessage()
                    }
                } ?: { showFinalMessage() }()
            }
        }
    }

    companion object {
        fun create(wallpaper: Wallpaper? = null, option: Int = -1) = ApplierDialog().apply {
            this.applyToOption = option
            this.wallpaper = wallpaper
        }
    }
}