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
import jahirfiquitiva.libs.frames.extensions.runInAThread
import jahirfiquitiva.libs.frames.models.Wallpaper
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

class WallpapersViewModel:BaseViewModel<ArrayList<Wallpaper>>() {
    override fun loadItems(context:Context):ArrayList<Wallpaper> {
        val wallpapers = ArrayList<Wallpaper>()
        val volleyRequest = Volley.newRequestQueue(context)
        volleyRequest.add(
                StringRequest(Request.Method.GET, context.getString(R.string.json_url),
                              Response.Listener<String> { response ->
                                  if (response.isNotEmpty())
                                      wallpapers.addAll(buildWallpapersListFromJson(context,
                                                                                    JSONObject(
                                                                                            response)))
                              }, Response.ErrorListener {
                    if (context.framesKonfigs.backupJson.isNotEmpty())
                        wallpapers.addAll(buildWallpapersListFromJson(context,
                                                                      JSONObject(
                                                                              context.framesKonfigs.backupJson)))
                }))
        return wallpapers
    }

    private fun buildWallpapersListFromJson(context:Context, json:JSONObject):ArrayList<Wallpaper> {
        val fWallpapers = ArrayList<Wallpaper>()
        val wallsList:JSONArray = json.getJSONArray("wallpapers")
        for (index in 0..wallsList.length()) {
            if (wallsList.isNull(index)) continue
            val obj = wallsList.getJSONObject(index)
            val name = obj.getString("name") ?: ""
            val author = obj.getString("author") ?: ""
            val categories = obj.getString("categories") ?: ""
            val url = obj.getString("url") ?: ""
            val thumbUrl = obj.getString("thumbUrl") ?: url ?: ""
            if (name.isNotEmpty() && categories.isNotEmpty() && url.isNotEmpty()) {
                if (thumbUrl.isEmpty()) fWallpapers.add(Wallpaper(name, author, categories, url))
                else fWallpapers.add(Wallpaper(name, author, categories, url, thumbUrl))
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