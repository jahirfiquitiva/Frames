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
import com.bumptech.glide.request.FutureTarget
import jahirfiquitiva.libs.frames.R
import jahirfiquitiva.libs.frames.extensions.downloadOnly
import jahirfiquitiva.libs.frames.extensions.framesKonfigs
import jahirfiquitiva.libs.frames.models.Wallpaper
import jahirfiquitiva.libs.kauextensions.extensions.runInAThread
import org.json.JSONArray
import java.io.File

class WallpapersViewModel:ListViewModel<Wallpaper>() {

    override fun loadItems(context:Context):ArrayList<Wallpaper> {
        val wallz = ArrayList<Wallpaper>()
        val volleyRequest = Volley.newRequestQueue(context)
        volleyRequest.add(
                StringRequest(Request.Method.GET, context.getString(R.string.json_url),
                              Response.Listener<String> { response ->
                                  if (response.isNotEmpty())
                                      context.framesKonfigs.backupJson = response
                                      items.postValue(
                                              buildWallpapersListFromJson(context,
                                                                          JSONArray(response)))
                              }, Response.ErrorListener {
                    if (context.framesKonfigs.backupJson.isNotEmpty())
                        items.postValue(
                                buildWallpapersListFromJson(context,
                                                            JSONArray(
                                                                    context.framesKonfigs.backupJson)))
                }))
        return wallz
    }

    private fun buildWallpapersListFromJson(context:Context, json:JSONArray):ArrayList<Wallpaper> {
        val fWallpapers = ArrayList<Wallpaper>()
        val wallsList = json
        // val wallsList:JSONArray = json.getJSONArray("wallpapers")
        for (index in 0..wallsList.length()) {
            if (wallsList.isNull(index)) continue
            val obj = wallsList.getJSONObject(index)
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
            if (name.isNotEmpty() && collections.isNotEmpty() && url.isNotEmpty()) {
                if (thumbUrl.isEmpty()) fWallpapers.add(Wallpaper(name, author, collections, url))
                else fWallpapers.add(Wallpaper(name, author, collections, url, thumbUrl))
            }
        }
        context.runInAThread {
            fWallpapers.forEach {
                val target:FutureTarget<File> = context.downloadOnly(it.url)
                try {
                    target.get()
                } catch (ignored:Exception) {
                }
            }
        }
        return fWallpapers
    }

}