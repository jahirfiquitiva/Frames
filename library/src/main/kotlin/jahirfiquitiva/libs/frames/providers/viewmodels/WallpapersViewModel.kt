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
        val prevList = ArrayList(getData())
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
    
    override fun loadData(param:Context):MutableList<Wallpaper> =
            loadWallpapers(param,
                           FramesUrlRequests().requestJson(param.getString(R.string.json_url)))
    
    private fun loadWallpapers(context:Context, serverResponse:String):MutableList<Wallpaper> {
        val prevResponse = context.framesKonfigs.backupJson
        val validPrevResponse = prevResponse.hasContent() && prevResponse != "[]"
        return if (serverResponse.hasContent()) {
            val nResponse = safeParseResponseToJSON(context, serverResponse)
            val nResponseText = nResponse.toString()
            if (validPrevResponse && nResponseText == prevResponse) {
                if (isOldDataValid) ArrayList(getData())
                else parseListFromJson(context, nResponse)
            } else parseListFromJson(context, nResponse)
        } else {
            if (validPrevResponse) {
                parseListFromJson(context, safeParseResponseToJSON(context, prevResponse))
            } else ArrayList()
        }
    }
    
    private fun safeParseResponseToJSON(context:Context, response:String):JSONArray {
        return try {
            parseResponseToJSON(response, context.getBoolean(R.bool.use_old_json_format))
        } catch (e:Exception) {
            e.printStackTrace()
            try {
                parseResponseToJSON(response, true)
            } catch (e2:Exception) {
                e2.printStackTrace()
                JSONArray("[]")
            }
        }
    }
    
    private fun parseResponseToJSON(response:String, useOldFormat:Boolean):JSONArray {
        return if (response.hasContent()) {
            if (useOldFormat) parseResponseToOldJSON(response)
            else JSONArray(response)
        } else JSONArray("[]")
    }
    
    private fun parseResponseToOldJSON(response:String):JSONArray {
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
    
    private fun parseListFromJson(context:Context, json:JSONArray):MutableList<Wallpaper> {
        context.framesKonfigs.backupJson = json.toString()
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