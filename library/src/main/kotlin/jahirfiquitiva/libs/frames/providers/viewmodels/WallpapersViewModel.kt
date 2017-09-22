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
import jahirfiquitiva.libs.frames.R
import jahirfiquitiva.libs.frames.data.models.Wallpaper
import jahirfiquitiva.libs.frames.helpers.extensions.framesKonfigs
import jahirfiquitiva.libs.frames.helpers.utils.FramesUrlRequests
import jahirfiquitiva.libs.kauextensions.extensions.formatCorrectly
import jahirfiquitiva.libs.kauextensions.extensions.getBoolean
import jahirfiquitiva.libs.kauextensions.extensions.hasContent
import jahirfiquitiva.libs.kauextensions.extensions.toTitleCase
import org.json.JSONArray
import org.json.JSONObject

class WallpapersViewModel:ListViewModel<Context, Wallpaper>() {
    
    fun updateWallpaper(newWallpaper:Wallpaper) {
        val prevList = ArrayList(items.value)
        if (prevList.size > 0) {
            val pos = prevList.indexOf(newWallpaper)
            if (pos >= 0) {
                val old = prevList[pos]
                if (newWallpaper.hasChangedFrom(old)) {
                    prevList[pos] = newWallpaper
                    postResult(prevList)
                }
            }
        }
    }
    
    override fun loadItems(param:Context):MutableList<Wallpaper> {
        val prevResponse = param.framesKonfigs.backupJson
        val response = FramesUrlRequests().requestJson(param.getString(
                R.string.json_url))
        val oldValue = items.value
        val formattedResponse = buildJSONArrayFromResponse(param, response).toString()
        val areTheSame = formattedResponse.equals(prevResponse, true)
        return if (areTheSame) {
            if (oldValue != null) {
                if (oldValue.size > 0) oldValue
                else loadWallpapers(param, response)
            } else {
                loadWallpapers(param, response)
            }
        } else {
            loadWallpapers(param, response)
        }
    }
    
    private fun loadWallpapers(context:Context, response:String = ""):ArrayList<Wallpaper> =
            if (response.hasContent()) {
                buildWallpapersListFromResponse(context, response)
            } else {
                val prevResponse = context.framesKonfigs.backupJson
                if (prevResponse.hasContent()) {
                    buildWallpapersListFromResponse(context, prevResponse)
                } else {
                    ArrayList()
                }
            }
    
    private fun buildWallpapersListFromResponse(context:Context,
                                                response:String):ArrayList<Wallpaper> {
        if (!(response.hasContent())) return buildWallpapersListFromJson(JSONArray("[]"))
        val jsonArray = buildJSONArrayFromResponse(context, response)
        context.framesKonfigs.backupJson = jsonArray.toString()
        return buildWallpapersListFromJson(jsonArray)
    }
    
    private fun buildJSONArrayFromResponse(context:Context, response:String):JSONArray {
        val shouldUseOldFormat = context.getBoolean(R.bool.use_old_json_format)
        return try {
            buildJSONArrayFromResponse(response, shouldUseOldFormat)
        } catch (e:Exception) {
            e.printStackTrace()
            try {
                buildJSONArrayFromResponse(response, true)
            } catch (e2:Exception) {
                e2.printStackTrace()
                JSONArray("[]")
            }
        }
    }
    
    private fun buildJSONArrayFromResponse(response:String, useOldFormat:Boolean):JSONArray {
        return if (response.hasContent()) {
            if (useOldFormat) {
                buildJSONArrayFromOldFormat(response)
            } else {
                JSONArray(response)
            }
        } else {
            JSONArray("[]")
        }
    }
    
    private fun buildJSONArrayFromOldFormat(response:String):JSONArray {
        return try {
            JSONObject(response).getJSONArray("Wallpapers")
        } catch (ignored:Exception) {
            try {
                JSONObject(response).getJSONArray("wallpapers")
            } catch (ignored:Exception) {
                JSONArray("[]")
            }
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
                collections = obj.getString("collections")
            } catch (ignored:Exception) {
                try {
                    collections = obj.getString("categories")
                } catch (ignored:Exception) {
                    try {
                        collections = obj.getString("category") ?: ""
                    } catch (ignored:Exception) {
                    }
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
                thumbUrl = obj.getString("thumbnail")
            } catch (ignored:Exception) {
                try {
                    thumbUrl = obj.getString("thumbUrl")
                } catch (ignored:Exception) {
                    try {
                        thumbUrl = obj.getString("thumb")
                    } catch (ignored:Exception) {
                        try {
                            thumbUrl = obj.getString("url-thumb") ?: ""
                        } catch (ignored:Exception) {
                        }
                    }
                }
            }
            var size = 0L
            try {
                size = obj.getLong("size")
            } catch (ignored:Exception) {
                try {
                    size = obj.getString("size").toLong()
                } catch (ignored:Exception) {
                }
            }
            var dimensions = ""
            try {
                dimensions = obj.getString("dimensions")
            } catch (ignored:Exception) {
                try {
                    dimensions = obj.getString("dimension") ?: ""
                } catch (ignored:Exception) {
                }
            }
            var copyright = ""
            try {
                copyright = obj.getString("copyright") ?: ""
            } catch (ignored:Exception) {
            }
            name = name.formatCorrectly().replace("_", " ").toTitleCase()
            author = author.formatCorrectly().replace("_", " ").toTitleCase()
            if (name.hasContent() && url.hasContent()) {
                fWallpapers.add(
                        Wallpaper(name, author, collections, downloadable, url,
                                  if (thumbUrl.hasContent()) thumbUrl else url, size,
                                  dimensions, copyright))
            }
        }
        fWallpapers.distinct()
        return fWallpapers
    }
}