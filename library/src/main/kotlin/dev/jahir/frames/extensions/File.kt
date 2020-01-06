package dev.jahir.frames.extensions

import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.util.Log
import androidx.core.content.FileProvider
import dev.jahir.frames.R
import java.io.File

fun File.getUri(context: Context): Uri? {
    return try {
        FileProvider.getUriForFile(context, context.packageName + ".fileProvider", this)
    } catch (e: Exception) {
        try {
            Uri.fromFile(this)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

@Suppress("DEPRECATION")
fun Context.getExternalStorageFolder(): File? {
    val externalStorage = try {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            Environment.getExternalStorageDirectory()
        } else {
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
    val appStorage = try {
        getExternalFilesDir(null)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
    return if (appStorage?.absolutePath.orEmpty().contains(packageName)) externalStorage
    else appStorage
}

fun Context.getDefaultWallpapersDownloadFolder(): File? {
    val externalFolder = getExternalStorageFolder()
    val folder = File("$externalFolder${File.separator}${getString(R.string.app_name)}")
    try {
        if (!folder.exists()) {
            Log.d("Frames", "Creating folder: $folder")
            folder.mkdirs()
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return folder
}

fun Context.getWallpapersDownloadFolder(): File? {
    // TODO: Check Preferences
    return getDefaultWallpapersDownloadFolder()
}