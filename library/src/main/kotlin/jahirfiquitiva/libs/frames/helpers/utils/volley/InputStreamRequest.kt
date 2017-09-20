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

import com.android.volley.NetworkResponse
import com.android.volley.ParseError
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.HttpHeaderParser

/**
 * A canned request for retrieving the response body at a given URL as a String.
 * Creates a new request with the given method.
 *
 * @param method the request [Method] to use
 * @param url URL to fetch the string at
 * @param listener Listener to receive the String response
 * @param errorListener Error listener, or null to ignore errors
 *
 * Credits:
 * https://github.com/georgiecasey/android-volley-inputstream-as-response/
 */
open class InputStreamRequest(method:Int, url:String,
                              private val mListener:Response.Listener<ByteArray>,
                              errorListener:Response.ErrorListener):
        Request<ByteArray>(method, url, errorListener) {
    
    companion object {
        val lock = Any()
    }
    
    init {
        // this request would never use cache.
        setShouldCache(false)
    }
    
    override fun deliverResponse(response:ByteArray) {
        mListener.onResponse(response)
    }
    
    override fun parseNetworkResponse(response:NetworkResponse?):Response<ByteArray> {
        synchronized(lock, {
            return try {
                doParse(response)
            } catch (e:Exception) {
                e.printStackTrace()
                Response.error(ParseError(e))
            }
        })
    }
    
    private fun doParse(response:NetworkResponse?):Response<ByteArray> {
        return if (response == null)
            Response.error(ParseError(NullPointerException("Response is null")))
        else Response.success(response.data, HttpHeaderParser.parseCacheHeaders(response))
    }
}