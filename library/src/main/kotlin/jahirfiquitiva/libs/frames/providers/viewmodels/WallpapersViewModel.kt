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
package jahirfiquitiva.libs.frames.providers.viewmodels

import android.content.Context
import jahirfiquitiva.libs.archhelpers.viewmodels.ListViewModel
import jahirfiquitiva.libs.frames.R
import jahirfiquitiva.libs.frames.data.models.Wallpaper
import jahirfiquitiva.libs.frames.helpers.extensions.framesKonfigs
import jahirfiquitiva.libs.frames.helpers.utils.FL
import jahirfiquitiva.libs.frames.helpers.utils.FramesUrlRequests
import jahirfiquitiva.libs.kauextensions.extensions.formatCorrectly
import jahirfiquitiva.libs.kauextensions.extensions.getBoolean
import jahirfiquitiva.libs.kauextensions.extensions.hasContent
import jahirfiquitiva.libs.kauextensions.extensions.string
import jahirfiquitiva.libs.kauextensions.extensions.toTitleCase
import org.json.JSONArray
import org.json.JSONObject

class WallpapersViewModel : ListViewModel<Context, Wallpaper>() {
    
    fun updateWallpaper(newWallpaper: Wallpaper) {
        val prevList = ArrayList(getData())
        if (prevList.isNotEmpty()) {
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
    
    override fun internalLoad(param: Context): ArrayList<Wallpaper> =
            loadWallpapers(
                    param, FramesUrlRequests().requestJson(param.getString(R.string.json_url)))
    
    private fun loadWallpapers(context: Context, serverResponse: String): ArrayList<Wallpaper> {
        val prevResponse = context.framesKonfigs.backupJson
        val validPrevResponse = prevResponse.hasContent() && prevResponse != "[]"
        return if (serverResponse.hasContent()) {
            val nResponse = safeParseResponseToJSON(context, serverResponse)
            val nResponseText = nResponse.toString()
            if (validPrevResponse && nResponseText == prevResponse) {
                if (isOldDataValid()) ArrayList(getData())
                else parseListFromJson(context, nResponse)
            } else parseListFromJson(context, nResponse)
        } else {
            if (validPrevResponse) {
                parseListFromJson(context, safeParseResponseToJSON(context, prevResponse))
            } else ArrayList()
        }
    }
    
    private fun safeParseResponseToJSON(context: Context, response: String): JSONArray {
        return try {
            parseResponseToJSON(response, context.getBoolean(R.bool.use_old_json_format))
        } catch (e: Exception) {
            FL.e { e.message }
            try {
                parseResponseToJSON(response, true)
            } catch (e2: Exception) {
                FL.e { e2.message }
                JSONArray("[]")
            }
        }
    }
    
    private fun parseResponseToJSON(response: String, useOldFormat: Boolean): JSONArray {
        return if (response.hasContent()) {
            if (useOldFormat) parseResponseToOldJSON(response)
            else JSONArray(response)
        } else JSONArray("[]")
    }
    
    private fun parseResponseToOldJSON(response: String): JSONArray {
        return try {
            JSONObject(response).getJSONArray("Wallpapers")
        } catch (ignored: Exception) {
            try {
                JSONObject(response).getJSONArray("wallpapers")
            } catch (ignored: Exception) {
                JSONArray("[]")
            }
        }
    }
    
    private fun parseListFromJson(context: Context, json: JSONArray): ArrayList<Wallpaper> {
        context.framesKonfigs.backupJson = json.toString()
        val fWallpapers = ArrayList<Wallpaper>()
        for (index in 0..json.length()) {
            if (json.isNull(index)) continue
            val obj = json.getJSONObject(index)
            val name = obj.string("name")
            val author = obj.string("author")
            val collections = getCollectionsForWallpaper(obj)
            val downloadable = isWallpaperDownloadable(obj)
            val url = obj.string("url")
            val thumbUrl = getWallpaperThumbnailUrl(obj)
            val size = getWallpaperBytes(obj)
            val dimensions = getWallpaperDimensions(obj)
            val copyright = obj.string("copyright")
            val correctName = name.formatCorrectly().replace("_", " ").toTitleCase()
            val correctAuthor = author.formatCorrectly().replace("_", " ").toTitleCase()
            if (correctName.hasContent() && url.hasContent()) {
                fWallpapers.add(
                        Wallpaper(
                                correctName, correctAuthor, collections,
                                downloadable, url,
                                if (thumbUrl.hasContent()) thumbUrl else url,
                                size, dimensions, copyright))
            }
        }
        fWallpapers.distinct()
        return fWallpapers
    }
    
    private fun getCollectionsForWallpaper(obj: JSONObject): String = try {
        obj.getString("collections")
    } catch (ignored: Exception) {
        try {
            obj.getString("categories")
        } catch (ignored: Exception) {
            try {
                obj.getString("category") ?: ""
            } catch (ignored: Exception) {
                ""
            }
        }
    }
    
    private fun isWallpaperDownloadable(obj: JSONObject): Boolean = try {
        obj.getBoolean("downloadable")
    } catch (ignored: Exception) {
        try {
            (obj.getString("downloadable") ?: "true").equals("true", true)
        } catch (ignored: Exception) {
            true
        }
    }
    
    private fun getWallpaperThumbnailUrl(obj: JSONObject): String = try {
        obj.getString("thumbnail")
    } catch (ignored: Exception) {
        try {
            obj.getString("thumbUrl")
        } catch (ignored: Exception) {
            try {
                obj.getString("thumburl")
            } catch (ignored: Exception) {
                try {
                    obj.getString("thumb")
                } catch (ignored: Exception) {
                    try {
                        obj.getString("url-thumb") ?: ""
                    } catch (ignored: Exception) {
                        ""
                    }
                }
            }
        }
    }
    
    private fun getWallpaperBytes(obj: JSONObject): Long = try {
        obj.getLong("size")
    } catch (ignored: Exception) {
        try {
            obj.getString("size").toLong()
        } catch (ignored: Exception) {
            0L
        }
    }
    
    private fun getWallpaperDimensions(obj: JSONObject): String = try {
        obj.getString("dimensions")
    } catch (ignored: Exception) {
        try {
            obj.getString("dimension") ?: ""
        } catch (ignored: Exception) {
            ""
        }
    }
}