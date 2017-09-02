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
package jahirfiquitiva.libs.frames.helpers.extensions

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Environment
import ca.allanwang.kau.utils.dimenPixelSize
import com.afollestad.materialdialogs.MaterialDialog
import jahirfiquitiva.libs.frames.R
import jahirfiquitiva.libs.frames.helpers.utils.FramesKonfigs
import jahirfiquitiva.libs.frames.helpers.utils.PREFERENCES_NAME
import jahirfiquitiva.libs.kauextensions.extensions.dividerColor
import jahirfiquitiva.libs.kauextensions.extensions.getDrawable
import java.io.File

fun Context.getStatusBarHeight(force:Boolean = false):Int {
    var result = 0
    val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
    if (resourceId > 0) {
        result = resources.getDimensionPixelSize(resourceId)
    }
    val dimenResult = dimenPixelSize(R.dimen.status_bar_height)
    //if our dimension is 0 return 0 because on those devices we don't need the height
    return if (dimenResult == 0 && !force) {
        0
    } else {
        //if our dimens is > 0 && the result == 0 use the dimenResult else the result
        if (result == 0) dimenResult else result
    }
}

fun Context.openWallpaper(uri:Uri) {
    val intent = Intent()
    intent.action = Intent.ACTION_VIEW
    intent.setDataAndType(uri, "image/*")
    intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
    startActivity(intent)
}

val Context.thumbnailColor
    get() = dividerColor

fun Context.createHeartIcon(checked:Boolean):Drawable =
        (if (checked) "ic_heart" else "ic_heart_outline").getDrawable(this)

val Context.framesKonfigs:FramesKonfigs
    get() = FramesKonfigs.newInstance(PREFERENCES_NAME, this)

fun Context.run(f:() -> Unit):Runnable = Runnable { f() }

inline fun Context.buildMaterialDialog(action:MaterialDialog.Builder.() -> Unit):MaterialDialog {
    val builder = MaterialDialog.Builder(this)
    builder.action()
    return builder.build()
}

val Context.dataCacheSize:String
    get() {
        var cache:Long = 0
        var extCache:Long = 0
        
        try {
            cacheDir.listFiles().forEach {
                cache += if (it.isDirectory) it.dirSize else it.length()
            }
        } catch (ignored:Exception) {
        }
        
        try {
            externalCacheDir.listFiles().forEach {
                extCache += if (it.isDirectory) it.dirSize else it.length()
            }
        } catch (ignored:Exception) {
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
                if (!(it.equals("lib", true))) {
                    deleteFile(File(appDir, it))
                }
            }
        }
    }
    clearCache()
    framesKonfigs.downloadsFolder = getString(R.string.default_download_folder,
                                              Environment.getExternalStorageDirectory().absolutePath)
}

fun Context.clearCache() {
    try {
        cacheDir?.let {
            deleteFile(it)
        }
    } catch (ignored:Exception) {
    }
}

fun Context.deleteFile(f:File) {
    if (f.isDirectory) {
        f.list().forEach {
            deleteFile(File(f, it))
        }
    } else {
        f.delete()
    }
}