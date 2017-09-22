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

import android.animation.ArgbEvaluator
import android.animation.TypeEvaluator
import android.animation.ValueAnimator
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.support.annotation.ColorInt
import ca.allanwang.kau.utils.dimenPixelSize
import com.afollestad.materialdialogs.MaterialDialog
import com.bumptech.glide.Priority
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import jahirfiquitiva.libs.frames.R
import jahirfiquitiva.libs.frames.helpers.utils.FramesKonfigs
import jahirfiquitiva.libs.frames.helpers.utils.PREFERENCES_NAME
import jahirfiquitiva.libs.kauextensions.extensions.getDrawable
import jahirfiquitiva.libs.kauextensions.extensions.usesDarkTheme
import java.io.File

val Context.maxPreload
    get() = if (isLowRamDevice) 2 else 4

val Context.maxPictureRes
    get() = if (isLowRamDevice) if (runsMinSDK) 30 else 20 else 40

val Context.bestBitmapConfig:Bitmap.Config
    get() = if (isLowRamDevice) Bitmap.Config.RGB_565 else Bitmap.Config.ARGB_8888

val Context.runsMinSDK
    get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN

val Context.isLowRamDevice:Boolean
    get() {
        val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val lowRAMDevice:Boolean
        lowRAMDevice = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            activityManager.isLowRamDevice
        } else {
            val memInfo = ActivityManager.MemoryInfo()
            activityManager.getMemoryInfo(memInfo)
            memInfo.lowMemory
        }
        return lowRAMDevice
    }

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
    get() = if (usesDarkTheme) Color.parseColor("#3dffffff") else Color.parseColor("#3d000000")

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

val Context.basicOptions:RequestOptions
    get() {
        return RequestOptions()
                .format(if (isLowRamDevice) DecodeFormat.PREFER_RGB_565
                        else DecodeFormat.PREFER_ARGB_8888)
                .disallowHardwareConfig()
    }

val Context.urlOptions:RequestOptions
    get() = basicOptions.diskCacheStrategy(DiskCacheStrategy.RESOURCE)

val Context.resourceOptions:RequestOptions
    get() = basicOptions.diskCacheStrategy(DiskCacheStrategy.NONE)

val Context.thumbnailOptions:RequestOptions
    get() = urlOptions.priority(Priority.IMMEDIATE)

val Context.wallpaperOptions:RequestOptions
    get() = urlOptions.priority(Priority.HIGH)

fun <T> createAnimator(
        evaluator:TypeEvaluator<*>,
        vararg values:T,
        onConfig:ValueAnimator.() -> Unit = {},
        onUpdate:(T) -> Unit
                      ):ValueAnimator =
        ValueAnimator.ofObject(evaluator, *values).apply {
            addUpdateListener {
                @Suppress("UNCHECKED_CAST")
                onUpdate(it.animatedValue as T)
            }
            onConfig(this)
        }


fun animateSmoothly(@ColorInt startColorId:Int, @ColorInt endColorId:Int,
                    doUpdate:(Int) -> Unit):ValueAnimator =
        createAnimator(ArgbEvaluator(),
                       startColorId, endColorId,
                       onConfig = {
                           duration = 1000
                           repeatMode = ValueAnimator.REVERSE
                           repeatCount = ValueAnimator.INFINITE
                           start()
                       },
                       onUpdate = doUpdate)