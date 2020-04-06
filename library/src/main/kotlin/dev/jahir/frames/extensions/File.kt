package dev.jahir.frames.extensions

import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import androidx.core.content.FileProvider
import java.io.File

fun File.getUri(context: Context?): Uri? {
    context ?: return null
    return try {
        FileProvider.getUriForFile(context, context.packageName + ".fileProvider", this)
    } catch (e: Exception) {
        try {
            Uri.fromFile(this)
        } catch (e: Exception) {
            null
        }
    }
}

@Suppress("DEPRECATION")
fun Context.getExternalStorageFolder(): File? {
    val externalStorage = try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        } else {
            Environment.getExternalStorageDirectory()
        }
    } catch (e: Exception) {
        null
    }
    val appStorage = try {
        getExternalFilesDir(null)
    } catch (e: Exception) {
        null
    }
    return if (appStorage?.absolutePath.orEmpty().contains(packageName)) externalStorage
    else appStorage
}

fun File.createIfDidNotExist(): Boolean = try {
    if (!exists()) mkdirs() else true
} catch (e: Exception) {
    false
}

fun Context.getDefaultWallpapersDownloadFolder(): File? {
    val externalFolder = getExternalStorageFolder()
    val folder = File("$externalFolder${File.separator}${getAppName()}")
    folder.createIfDidNotExist()
    return folder
}

val File.dirSize: Long
    get() {
        return try {
            if (exists()) {
                var result: Long = 0
                listFiles()?.forEach { result += if (it.isDirectory) it.dirSize else it.length() }
                result
            } else 0
        } catch (e: Exception) {
            0
        }
    }

fun File.deleteEverything() {
    if (isDirectory) {
        list()?.forEach { File(this, it).deleteEverything() }
    } else delete()
}