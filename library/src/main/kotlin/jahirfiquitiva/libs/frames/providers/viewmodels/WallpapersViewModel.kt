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

import android.content.Context
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import jahirfiquitiva.libs.frames.R
import jahirfiquitiva.libs.frames.helpers.extensions.framesKonfigs
import jahirfiquitiva.libs.frames.data.models.Wallpaper
import jahirfiquitiva.libs.kauextensions.extensions.formatCorrectly
import jahirfiquitiva.libs.kauextensions.extensions.hasContent
import jahirfiquitiva.libs.kauextensions.extensions.toTitleCase
import org.json.JSONArray

class WallpapersViewModel:ListViewModel<Context, Wallpaper>() {
    
    
    override fun loadItems(param:Context):ArrayList<Wallpaper> {
        val volley = Volley.newRequestQueue(param)
        val request = StringRequest(Request.Method.GET, param.getString(R.string.json_url),
                                    Response.Listener<String> {
                                        postResult(loadWallpapers(param, it))
                                    },
                                    Response.ErrorListener {
                                        postResult(ArrayList())
                                    })
        request.retryPolicy = DefaultRetryPolicy(5000, 2, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
        volley.add(request)
        volley.start()
        return ArrayList()
    }
    
    
    private fun loadWallpapers(context:Context, response:String):ArrayList<Wallpaper> =
            if (response.hasContent()) {
                context.framesKonfigs.backupJson = response
                buildWallpapersListFromJson(JSONArray(response))
            } else {
                val prevResponse = context.framesKonfigs.backupJson
                if (prevResponse.hasContent()) {
                    buildWallpapersListFromJson(JSONArray(prevResponse))
                } else {
                    ArrayList()
                }
            }
    
    private fun buildWallpapersListFromJson(json:JSONArray):ArrayList<Wallpaper> {
        val fWallpapers = ArrayList<Wallpaper>()
        for (index in 0..json.length()) {
            if (json.isNull(index)) continue
            val obj = json.getJSONObject(index)
            var name = ""
            try {
                name = obj.getString("name") ?: ""
            } catch (ignored:Exception) {
            }
            var author = ""
            try {
                author = obj.getString("author") ?: ""
            } catch (ignored:Exception) {
            }
            var collections = ""
            try {
                collections = obj.getString("categories")
            } catch (ignored:Exception) {
                try {
                    collections = obj.getString("collections") ?: ""
                } catch (ignored:Exception) {
                }
            }
            var downloadable = true
            try {
                downloadable = obj.getBoolean("downloadable")
            } catch (ignored:Exception) {
                try {
                    downloadable = (obj.getString("downloadable") ?: "true").equals("true", true)
                } catch (ignored:Exception) {
                }
            }
            var url = ""
            try {
                url = obj.getString("url") ?: ""
            } catch (ignored:Exception) {
            }
            var thumbUrl = ""
            try {
                thumbUrl = obj.getString("thumbUrl") ?: url ?: ""
            } catch (ignored:Exception) {
            }
            name = name.formatCorrectly().replace("_", " ").toTitleCase()
            author = author.formatCorrectly().replace("_", " ").toTitleCase()
            if (name.hasContent()) {
                if (thumbUrl.hasContent())
                    fWallpapers.add(
                            Wallpaper(name, author, collections, downloadable, url, thumbUrl))
                else fWallpapers.add(
                        Wallpaper(name, author, collections, downloadable, url))
            }
        }
        fWallpapers.distinct()
        return fWallpapers
    }
    
}