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

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.support.v4.app.Fragment
import ca.allanwang.kau.utils.darken
import ca.allanwang.kau.utils.lighten
import com.afollestad.materialdialogs.MaterialDialog
import jahirfiquitiva.libs.frames.R
import jahirfiquitiva.libs.frames.helpers.utils.FramesKonfigs
import jahirfiquitiva.libs.kauextensions.extensions.cardBackgroundColor
import jahirfiquitiva.libs.kauextensions.extensions.ctxt
import jahirfiquitiva.libs.kauextensions.extensions.deleteEverything
import jahirfiquitiva.libs.kauextensions.extensions.extractColor
import jahirfiquitiva.libs.kauextensions.extensions.getDrawable
import jahirfiquitiva.libs.kauextensions.extensions.isLowRamDevice
import jahirfiquitiva.libs.kauextensions.extensions.usesDarkTheme
import jahirfiquitiva.libs.kauextensions.ui.activities.ThemedActivity
import java.io.File

@Suppress("DEPRECATION", "UNCHECKED_CAST")
internal val Fragment.configs: FramesKonfigs
    get() = (activity as? ThemedActivity<FramesKonfigs>)?.configs
            ?: activity?.let { FramesKonfigs(it) }
            ?: context?.let { FramesKonfigs(it) }
            ?: FramesKonfigs(ctxt)

val Context.backgroundColor: Int
    @SuppressLint("PrivateResource")
    get() {
        return try {
            extractColor(intArrayOf(android.R.attr.windowBackground))
        } catch (e: Exception) {
            if (usesDarkTheme) Color.parseColor("#303030") else Color.parseColor("#fafa")
        }
    }

val Context.tilesColor: Int
    get() {
        return if (usesDarkTheme) {
            cardBackgroundColor.lighten(0.1F)
        } else cardBackgroundColor.darken(0.1F)
    }

val Context.fastScrollThumbInactiveColor
    get() = if (usesDarkTheme) Color.parseColor("#73ffffff") else Color.parseColor("#73000000")

val Context.maxPreload
    get() = if (isLowRamDevice) 2 else 4

val Context.maxPictureRes
    get() = if (isLowRamDevice) if (runsMinSDK) 30 else 20 else 40

val Context.bestBitmapConfig: Bitmap.Config
    get() = if (isLowRamDevice) Bitmap.Config.RGB_565 else Bitmap.Config.ARGB_8888

val Context.runsMinSDK
    get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN

fun Context.openWallpaper(uri: Uri) {
    sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri))
    val intent = Intent()
    intent.action = Intent.ACTION_VIEW
    intent.setDataAndType(uri, "image/*")
    intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
    startActivity(intent)
}

fun Context.createHeartIcon(checked: Boolean): Drawable? =
        getDrawable(if (checked) "ic_heart" else "ic_heart_outline")

inline fun Context.buildMaterialDialog(action: MaterialDialog.Builder.() -> Unit = {}): MaterialDialog {
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
    FramesKonfigs(this).downloadsFolder = getString(
            R.string.default_download_folder,
            Environment.getExternalStorageDirectory().absolutePath)
}

fun Context.clearCache() {
    try {
        cacheDir?.deleteEverything()
    } catch (ignored: Exception) {
    }
}