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
package jahirfiquitiva.libs.frames.providers.viewmodels

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley
import jahirfiquitiva.libs.frames.data.models.Wallpaper
import jahirfiquitiva.libs.frames.helpers.utils.AsyncTaskManager
import jahirfiquitiva.libs.frames.helpers.utils.volley.FramesFileRequest
import java.io.ByteArrayOutputStream

class WallpaperInfoViewModel:ViewModel() {
    
    val info = MutableLiveData<WallpaperInfo>()
    private val REQUEST_TAG = "WIVM"
    private var queue:RequestQueue? = null
    private var task:AsyncTaskManager<Unit, Context>? = null
    
    fun loadData(parameter:Context, wallpaper:Wallpaper, forceLoad:Boolean = false) {
        stopTask(true)
        task = AsyncTaskManager(parameter, {},
                                { internalLoad(parameter, wallpaper, forceLoad) }, {})
        task?.execute()
    }
    
    private fun loadItems(param:Context, wallpaper:Wallpaper) {
        if (queue == null) queue = Volley.newRequestQueue(param)
        queue?.add(FramesFileRequest(param, wallpaper.url, REQUEST_TAG,
                                     onSuccess = {
                                         try {
                                             postResult(it.toWallpaperInfo())
                                         } catch (ignored:Exception) {
                                             postResult(WallpaperInfo(0L, Dimension(0L, 0L)))
                                         }
                                     },
                                     onError = {
                                         postResult(WallpaperInfo(0L, Dimension(0L, 0L)))
                                     }).createRequest())
        queue?.start()
    }
    
    fun stopTask(interrupt:Boolean = false) {
        queue?.cancelAll(REQUEST_TAG)
        task?.cancelTask(interrupt)
    }
    
    private fun internalLoad(param:Context, wallpaper:Wallpaper, forceLoad:Boolean = false) {
        if (forceLoad) {
            loadItems(param, wallpaper)
        } else {
            val value = info.value
            if (value == null) loadItems(param, wallpaper)
            else postResult(value)
        }
    }
    
    internal fun postResult(data:WallpaperInfo) {
        info.postValue(data)
    }
}

fun ByteArray.toWallpaperInfo():WallpaperInfo =
        try {
            val bmp = BitmapFactory.decodeByteArray(this, 0, this.size)
            val width = bmp.width
            val height = bmp.height
            WallpaperInfo(size.toLong(), Dimension(width.toLong(), height.toLong()))
        } catch (ignored:Exception) {
            WallpaperInfo(0L, Dimension(0L, 0L))
        }

fun Bitmap.toByteArray(compressFormat:Bitmap.CompressFormat = Bitmap.CompressFormat.JPEG,
                       quality:Int = 25):ByteArray {
    val stream = ByteArrayOutputStream()
    return try {
        compress(compressFormat, quality, stream)
        val bytes = stream.toByteArray()
        bytes
    } catch (ignored:Exception) {
        ByteArray(0)
    } finally {
        stream.flush()
        stream.close()
    }
}

fun Bitmap.toWallpaperInfo():WallpaperInfo =
        try {
            val size = toByteArray(Bitmap.CompressFormat.PNG, 100).size
            if (size > 1) {
                WallpaperInfo(size.toLong(), Dimension(width.toLong(), height.toLong()))
            } else WallpaperInfo(0L, Dimension(0L, 0L))
        } catch (ignored:Exception) {
            WallpaperInfo(0L, Dimension(0L, 0L))
        }

data class WallpaperInfo(val size:Long, val dimension:Dimension)
data class Dimension(val width:Long, val height:Long) {
    val isValid:Boolean
        get() = width > 0L && height > 0L
    
    override fun toString():String = "$width x $height px"
}