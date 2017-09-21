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
package jahirfiquitiva.libs.frames.helpers.utils.volley

import android.content.Context
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.VolleyError

class FramesFileRequest(private val context:Context?, private val url:String,
                        private val tag:String,
                        private val onSuccess:(ByteArray) -> Unit = {},
                        private val onError:(VolleyError) -> Unit = {}) {
    fun createRequest():Request<ByteArray>? {
        val request = SimpleBytesRequest(context, Request.Method.GET, url,
                                         Response.Listener { onSuccess(it) },
                                         Response.ErrorListener { onError(it) })
        request.tag = tag
        request.retryPolicy = DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS * 2,
                                                 DefaultRetryPolicy.DEFAULT_MAX_RETRIES * 3,
                                                 DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
        return request
    }
}

class FramesJsonRequest(private val context:Context?, private val url:String,
                        private val tag:String,
                        private val onSuccess:(String) -> Unit = {},
                        private val onError:() -> Unit = {}) {
    fun createRequest():Request<String>? {
        val request = SimpleStringRequest(context, Request.Method.GET, url,
                                          Response.Listener { onSuccess(it) },
                                          Response.ErrorListener { onError() })
        request.tag = tag
        request.retryPolicy = DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS,
                                                 DefaultRetryPolicy.DEFAULT_MAX_RETRIES * 2,
                                                 DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
        return request
    }
}