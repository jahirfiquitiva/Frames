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
package jahirfiquitiva.libs.frames.helpers.utils

import android.graphics.BitmapFactory
import jahirfiquitiva.libs.frames.data.models.Dimension
import jahirfiquitiva.libs.frames.data.models.WallpaperInfo
import java.io.BufferedInputStream
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class FramesUrlRequests {
    fun requestJson(url: String): String {
        val result = StringBuilder()
        var urlConnection: HttpURLConnection? = null
        try {
            urlConnection = URL(url).openConnection() as HttpURLConnection
            urlConnection.connectTimeout = 20000
            urlConnection.readTimeout = 20000
            
            urlConnection.connect()
            
            val ins = BufferedInputStream(urlConnection.inputStream)
            val reader = BufferedReader(InputStreamReader(ins))
            var line: String? = null
            while ({ line = reader.readLine(); line }() != null) {
                result.append(line)
            }
            ins.close()
            reader.close()
        } catch (e: Exception) {
        } finally {
            urlConnection?.disconnect()
        }
        return result.toString()
    }
    
    fun requestFileInfo(url: String, onlySize: Boolean): WallpaperInfo {
        var info = WallpaperInfo(0, Dimension(0, 0))
        
        var urlConnection: HttpURLConnection? = null
        try {
            urlConnection = URL(url).openConnection() as HttpURLConnection
            urlConnection.connectTimeout = 15000
            urlConnection.readTimeout = 15000
            
            urlConnection.connect()
            
            info = if (onlySize) {
                WallpaperInfo(urlConnection.contentLength.toLong(), Dimension(0L, 0L))
            } else {
                val options = BitmapFactory.Options()
                options.inJustDecodeBounds = true
                val ins = urlConnection.inputStream
                BitmapFactory.decodeStream(ins, null, options)
                val size = urlConnection.connectTimeout.toLong()
                ins.close()
                WallpaperInfo(
                    size, Dimension(options.outWidth.toLong(), options.outHeight.toLong()))
            }
        } catch (e: Exception) {
        } finally {
            urlConnection?.disconnect()
        }
        
        return info
    }
}