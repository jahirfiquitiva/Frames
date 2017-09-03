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

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.content.Context
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import jahirfiquitiva.libs.frames.R
import jahirfiquitiva.libs.frames.data.models.Wallpaper
import jahirfiquitiva.libs.frames.helpers.configs.isLowRamDevice
import jahirfiquitiva.libs.frames.helpers.extensions.framesKonfigs
import jahirfiquitiva.libs.frames.helpers.utils.AsyncTaskManager
import jahirfiquitiva.libs.kauextensions.extensions.formatCorrectly
import jahirfiquitiva.libs.kauextensions.extensions.getBoolean
import jahirfiquitiva.libs.kauextensions.extensions.hasContent
import jahirfiquitiva.libs.kauextensions.extensions.toTitleCase
import org.json.JSONArray
import org.json.JSONObject

class WallpapersViewModel:ViewModel() {
    
    val items = MutableLiveData<ArrayList<Wallpaper>>()
    private val REQUEST_TAG = "WVM"
    private var queue:RequestQueue? = null
    private var task:AsyncTaskManager<Unit, Context>? = null
    private var observer:ListViewModel.CustomObserver<ArrayList<Wallpaper>>? = null
    
    fun setCustomObserver(observer:ListViewModel.CustomObserver<ArrayList<Wallpaper>>) {
        this.observer = observer
    }
    
    fun loadData(parameter:Context, forceLoad:Boolean = false) {
        stopTask(true)
        task = AsyncTaskManager(parameter, {},
                                { internalLoad(parameter, forceLoad) }, {})
        task?.execute()
    }
    
    private fun loadItems(param:Context) {
        if (queue == null) queue = Volley.newRequestQueue(param)
        val request = WallsRequest(param, Request.Method.GET, param.getString(R.string.json_url),
                                   Response.Listener {
                                       postResult(loadWallpapers(param, it))
                                   },
                                   Response.ErrorListener {
                                       postResult(loadWallpapers(param, ""))
                                   })
        request.tag = REQUEST_TAG
        request.retryPolicy = DefaultRetryPolicy(2000, 2, 1.5F)
        queue?.add(request)
        queue?.start()
    }
    
    fun stopTask(interrupt:Boolean = false) {
        queue?.cancelAll(REQUEST_TAG)
        task?.cancelTask(interrupt)
    }
    
    private fun internalLoad(param:Context, forceLoad:Boolean = false) {
        if (forceLoad) {
            loadItems(param)
        } else {
            if (items.value != null && (items.value?.size ?: 0) > 0) {
                val list = ArrayList<Wallpaper>()
                items.value?.let { list.addAll(it.distinct()) }
                postResult(list)
            } else {
                loadItems(param)
            }
        }
    }
    
    internal fun postResult(data:ArrayList<Wallpaper>) {
        items.postValue(ArrayList(data.distinct()))
        observer?.onValuePosted(ArrayList(data.distinct()))
    }
    
    private fun loadWallpapers(context:Context, response:String):ArrayList<Wallpaper> =
            if (response.hasContent()) {
                buildWallpapersListFromResponse(context, response, true)
            } else {
                val prevResponse = context.framesKonfigs.backupJson
                if (prevResponse.hasContent()) {
                    buildWallpapersListFromResponse(context, prevResponse, true)
                } else {
                    ArrayList()
                }
            }
    
    fun buildWallpapersListFromResponse(context:Context, response:String,
                                        shouldSaveResult:Boolean = false):ArrayList<Wallpaper> {
        val shouldUseOldFormat = context.getBoolean(R.bool.use_old_json_format)
        val jsonArray = if (shouldUseOldFormat) {
            if (response.hasContent()) {
                try {
                    JSONObject(response).getJSONArray("Wallpapers")
                } catch (ignored:Exception) {
                    try {
                        JSONObject(response).getJSONArray("wallpapers")
                    } catch (ignored:Exception) {
                        JSONArray("[]")
                    }
                }
            } else {
                JSONArray("[]")
            }
        } else {
            JSONArray(response)
        }
        if (shouldSaveResult) context.framesKonfigs.backupJson = jsonArray.toString()
        return buildWallpapersListFromJson(jsonArray)
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
                thumbUrl = obj.getString("thumbUrl")
            } catch (ignored:Exception) {
                try {
                    thumbUrl = obj.getString("thumbnail")
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
    
    private inner class WallsRequest(private val context:Context,
                                     method:Int,
                                     url:String,
                                     listener:Response.Listener<String>,
                                     errorListener:Response.ErrorListener):
            StringRequest(method, url, listener, errorListener) {
        override fun getPriority():Priority {
            return if (context.isLowRamDevice) {
                Priority.HIGH
            } else {
                Priority.IMMEDIATE
            }
        }
    }
}