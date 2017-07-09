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

package jahirfiquitiva.libs.frames.models

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.content.Context
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.bumptech.glide.request.FutureTarget
import jahirfiquitiva.libs.frames.R
import jahirfiquitiva.libs.frames.extensions.downloadOnly
import jahirfiquitiva.libs.frames.extensions.runInAThread
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

class WallpaperViewModel:ViewModel() {
    var categories:MutableLiveData<HashMap<String, ArrayList<Wallpaper>>>? = null
    var wallpapers:MutableLiveData<ArrayList<Wallpaper>>? = null

    fun loadItems(context:Context) {
        val volleyRequest = Volley.newRequestQueue(context)
        volleyRequest.add(
                StringRequest(Request.Method.GET, context.getString(R.string.json_url),
                              Response.Listener<String> { response ->
                                  if (response.isNotEmpty()) {
                                      val wallsAsList = buildWallpapersListFromJson(
                                              JSONObject(response))
                                      context.runInAThread {
                                          wallsAsList.forEach {
                                              val target:FutureTarget<File> = context.downloadOnly(
                                                      it.url)
                                              try {
                                                  target.get()
                                              } catch (ignored:Exception) {
                                              }
                                          }
                                      }
                                      wallpapers?.postValue(wallsAsList)
                                      categories?.postValue(createCategoriesMapFrom(wallsAsList))
                                  }
                              }, Response.ErrorListener {
                    // TODO: Manage error
                })
                         )
    }

    private fun buildWallpapersListFromJson(json:JSONObject):ArrayList<Wallpaper> {
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
        return fWallpapers
    }

    private fun createCategoriesMapFrom(wallpapers:ArrayList<Wallpaper>):
            HashMap<String, ArrayList<Wallpaper>> {
        val categoriesMap = HashMap<String, ArrayList<Wallpaper>>()
        for ((index, wallpaper) in wallpapers.withIndex()) {
            val categories = wallpaper.categories
            if (categories.isNotEmpty()) {
                val categoriesList = categories.split(",")
                if (categoriesList.isNotEmpty()) {
                    categoriesList.forEach {
                        val wallsList = ArrayList<Wallpaper>()
                        if (categoriesMap.containsKey(it)) {
                            categoriesMap[it]?.let { wallsInCategory ->
                                wallsList.addAll(wallsInCategory)
                            }
                        }
                        wallsList.add(wallpapers[index])
                        categoriesMap.put(it, wallsList)
                    }
                }
            }
        }
        return categoriesMap
    }

}