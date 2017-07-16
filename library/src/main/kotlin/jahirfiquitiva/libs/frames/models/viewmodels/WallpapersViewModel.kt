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

package jahirfiquitiva.libs.frames.models.viewmodels

import android.content.Context
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import jahirfiquitiva.libs.frames.R
import jahirfiquitiva.libs.frames.extensions.framesKonfigs
import jahirfiquitiva.libs.frames.models.Wallpaper
import jahirfiquitiva.libs.kauextensions.extensions.formatCorrectly
import jahirfiquitiva.libs.kauextensions.extensions.toTitleCase
import org.json.JSONArray

class WallpapersViewModel:ListViewModel<Wallpaper, Context>() {
    override fun loadItems(p:Context):ArrayList<Wallpaper> {
        val list = ArrayList<Wallpaper>()
        val volleyRequest = Volley.newRequestQueue(p)
        volleyRequest.add(
                StringRequest(Request.Method.GET, p.getString(R.string.json_url),
                              Response.Listener<String> {
                                  list.clear()
                                  list.addAll(loadWallpapers(p, it))
                              },
                              Response.ErrorListener {
                                  list.clear()
                                  list.addAll(loadWallpapers(p, ""))
                              }))
        volleyRequest.addRequestFinishedListener<StringRequest> {
            postResult(list)
        }
        return list
    }


    private fun loadWallpapers(context:Context, response:String):ArrayList<Wallpaper> {
        if (response.isNotEmpty() && response.isNotBlank()) {
            context.framesKonfigs.backupJson = response
            return buildWallpapersListFromJson(context, JSONArray(response))
        } else {
            val prevResponse = context.framesKonfigs.backupJson
            if (prevResponse.isNotEmpty()) {
                return buildWallpapersListFromJson(context, JSONArray(prevResponse))
            } else {
                return ArrayList()
            }
        }
    }

    private fun buildWallpapersListFromJson(context:Context, json:JSONArray):ArrayList<Wallpaper> {
        val fWallpapers = ArrayList<Wallpaper>()
        for (index in 0..json.length()) {
            if (json.isNull(index)) continue
            val obj = json.getJSONObject(index)
            var name = ""
            try {
                name = obj.getString("name") ?: ""
            } catch(ignored:Exception) {
            }
            var author = ""
            try {
                author = obj.getString("author") ?: ""
            } catch(ignored:Exception) {
            }
            var collections = ""
            try {
                collections = obj.getString("categories")
            } catch(ignored:Exception) {
                try {
                    collections = obj.getString("collections") ?: ""
                } catch (ignored:Exception) {
                }
            }
            var url = ""
            try {
                url = obj.getString("url") ?: ""
            } catch(ignored:Exception) {
            }
            var thumbUrl = ""
            try {
                thumbUrl = obj.getString("thumbUrl") ?: url ?: ""
            } catch (ignored:Exception) {
            }
            name = name.formatCorrectly().replace("_", " ").toTitleCase()
            author = author.formatCorrectly().replace("_", " ").toTitleCase()
            if (name.isNotEmpty() && collections.isNotEmpty() && url.isNotEmpty()) {
                if (thumbUrl.isEmpty()) fWallpapers.add(Wallpaper(name, author, collections, url))
                else fWallpapers.add(Wallpaper(name, author, collections, url, thumbUrl))
            }
        }
        fWallpapers.distinct()
        return fWallpapers
    }

}