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

import jahirfiquitiva.libs.kauextensions.extensions.hasContent
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response

class FramesUrlRequests {
    fun requestJson(url:String):String {
        try {
            val client = OkHttpClient()
            val request = Request.Builder().url(url).build()
            var response:Response? = null
            var json = ""
            try {
                response = client.newCall(request).execute()
                json = response?.body()?.string() ?: ""
            } catch (ignored:Exception) {
            }
            if (json.hasContent()) response?.close()
            return json
        } catch (ignored:Exception) {
        }
        return ""
    }
    
    fun requestFileInfo(url:String, onlySize:Boolean):WallpaperInfo {
        try {
            val client = OkHttpClient()
            val request = Request.Builder().url(url).build()
            var response:Response? = null
            var bytes:ByteArray? = null
            try {
                response = client.newCall(request).execute()
                bytes = response?.body()?.bytes()
            } catch (ignored:Exception) {
            } finally {
                response?.close()
            }
            if (bytes != null) return bytes.toWallpaperInfo(onlySize)
        } catch (ignored:Exception) {
        }
        return WallpaperInfo(0, Dimension(0, 0))
    }
}