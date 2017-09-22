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
package jahirfiquitiva.libs.frames.helpers.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.ByteArrayOutputStream

data class WallpaperInfo(val size:Long, val dimension:Dimension) {
    val isValid:Boolean = size > 0L || dimension.isValid
}

data class Dimension(val width:Long, val height:Long) {
    val isValid:Boolean
        get() = width > 0L && height > 0L
    
    override fun toString():String = "$width x $height px"
}

fun ByteArray.toWallpaperInfo(onlySize:Boolean):WallpaperInfo {
    try {
        if (onlySize) return WallpaperInfo(size.toLong(), Dimension(0L, 0L))
        val bmp = BitmapFactory.decodeByteArray(this, 0, this.size)
        val width = bmp.width
        val height = bmp.height
        return WallpaperInfo(size.toLong(), Dimension(width.toLong(), height.toLong()))
    } catch (ignored:Exception) {
        return WallpaperInfo(0L, Dimension(0L, 0L))
    }
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