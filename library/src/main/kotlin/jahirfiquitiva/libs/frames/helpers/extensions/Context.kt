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
package jahirfiquitiva.libs.frames.helpers.extensions

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Environment
import androidx.fragment.app.Fragment
import ca.allanwang.kau.utils.darken
import ca.allanwang.kau.utils.lighten
import ca.allanwang.kau.utils.showChangelog
import com.afollestad.materialdialogs.MaterialDialog
import jahirfiquitiva.libs.frames.R
import jahirfiquitiva.libs.frames.helpers.utils.FramesKonfigs
import jahirfiquitiva.libs.kext.extensions.actv
import jahirfiquitiva.libs.kext.extensions.cardBackgroundColor
import jahirfiquitiva.libs.kext.extensions.deleteEverything
import jahirfiquitiva.libs.kext.extensions.drawable
import jahirfiquitiva.libs.kext.extensions.extractColor
import jahirfiquitiva.libs.kext.extensions.isLowRamDevice
import jahirfiquitiva.libs.kext.extensions.secondaryTextColor
import jahirfiquitiva.libs.kext.extensions.usesDarkTheme
import jahirfiquitiva.libs.kext.ui.activities.ThemedActivity
import java.io.File

@Suppress("UNCHECKED_CAST", "DEPRECATION")
internal val Fragment.configs: FramesKonfigs
    get() = (activity as? ThemedActivity<FramesKonfigs>)?.configs
        ?: activity?.let { FramesKonfigs(it) }
        ?: context?.let { FramesKonfigs(it) }
        ?: FramesKonfigs(actv)

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

val Context.maxPreload
    get() = if (isLowRamDevice) 2 else 4

val Context.maxPictureRes
    get() = if (isLowRamDevice) if (runsMinSDK) 30 else 20 else 40

private val runsMinSDK
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
    drawable(if (checked) "ic_heart" else "ic_heart_outline")

inline fun Context.mdDialog(action: MaterialDialog.() -> Unit = {}): MaterialDialog {
    val builder = MaterialDialog(this)
    builder.action()
    return builder
}

fun Context.showChanges() {
    showChangelog(R.xml.changelog, R.string.changelog, R.string.dismiss, secondaryTextColor)
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
            externalCacheDir?.listFiles()?.forEach {
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
