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
package jahirfiquitiva.libs.frames.helpers.extensions

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Environment
import com.afollestad.materialdialogs.MaterialDialog
import com.bumptech.glide.Priority
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import jahirfiquitiva.libs.frames.R
import jahirfiquitiva.libs.frames.helpers.utils.FramesKonfigs
import jahirfiquitiva.libs.frames.helpers.utils.PREFERENCES_NAME
import jahirfiquitiva.libs.kauextensions.extensions.deleteEverything
import jahirfiquitiva.libs.kauextensions.extensions.getDrawable
import jahirfiquitiva.libs.kauextensions.extensions.isLowRamDevice
import java.io.File

val Context.maxPreload
    get() = if (isLowRamDevice) 2 else 4

val Context.maxPictureRes
    get() = if (isLowRamDevice) if (runsMinSDK) 30 else 20 else 40

val Context.bestBitmapConfig: Bitmap.Config
    get() = if (isLowRamDevice) Bitmap.Config.RGB_565 else Bitmap.Config.ARGB_8888

val Context.runsMinSDK
    get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN

fun Context.openWallpaper(uri: Uri) {
    val intent = Intent()
    intent.action = Intent.ACTION_VIEW
    intent.setDataAndType(uri, "image/*")
    intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
    startActivity(intent)
}

fun Context.createHeartIcon(checked: Boolean): Drawable? =
        (if (checked) "ic_heart" else "ic_heart_outline").getDrawable(this)

val Context.framesKonfigs: FramesKonfigs
    get() = FramesKonfigs.newInstance(PREFERENCES_NAME, this)

inline fun Context.buildMaterialDialog(action: MaterialDialog.Builder.() -> Unit): MaterialDialog {
    val builder = MaterialDialog.Builder(this)
    builder.action()
    return builder.build()
}

val Context.dataCacheSize: String
    get() {
        var cache: Long = 0
        var extCache: Long = 0
        
        try {
            cacheDir.listFiles().forEach {
                cache += if (it.isDirectory) it.dirSize else it.length()
            }
        } catch (ignored: Exception) {
        }
        
        try {
            externalCacheDir.listFiles().forEach {
                extCache += if (it.isDirectory) it.dirSize else it.length()
            }
        } catch (ignored: Exception) {
        }
        
        val finalResult = ((cache + extCache) / 1024).toDouble()
        
        return if (finalResult > 1024) String.format("%.2f", finalResult / 1024) + " MB"
        else String.format("%.2f", finalResult) + " KB"
    }

fun Context.clearDataAndCache() {
    val appDir = File(cacheDir?.parent)
    appDir.let {
        if (it.exists()) {
            it.list().forEach {
                if (!it.equals("lib", true)) {
                    File(appDir, it).deleteEverything()
                }
            }
        }
    }
    clearCache()
    framesKonfigs.downloadsFolder = getString(
            R.string.default_download_folder,
            Environment.getExternalStorageDirectory().absolutePath)
}

fun Context.clearCache() {
    try {
        cacheDir?.deleteEverything()
    } catch (ignored: Exception) {
    }
}

val Context.basicOptions: RequestOptions
    get() {
        return RequestOptions()
                .format(
                        if (isLowRamDevice) DecodeFormat.PREFER_RGB_565
                        else DecodeFormat.PREFER_ARGB_8888)
                .disallowHardwareConfig()
    }

val Context.urlOptions: RequestOptions
    get() = basicOptions.diskCacheStrategy(DiskCacheStrategy.RESOURCE)

val Context.resourceOptions: RequestOptions
    get() = basicOptions.diskCacheStrategy(DiskCacheStrategy.NONE)

val Context.thumbnailOptions: RequestOptions
    get() = urlOptions.priority(Priority.IMMEDIATE)

val Context.wallpaperOptions: RequestOptions
    get() = urlOptions.priority(Priority.HIGH)