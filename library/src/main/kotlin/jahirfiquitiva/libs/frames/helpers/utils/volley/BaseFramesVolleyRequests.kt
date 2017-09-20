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
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import jahirfiquitiva.libs.frames.helpers.configs.isLowRamDevice

class SimpleBytesRequest(private val context:Context?, method:Int, url:String,
                         listener:Response.Listener<ByteArray>,
                         errorListener:Response.ErrorListener):
        InputStreamRequest(method, url, listener, errorListener) {
    override fun getPriority():Priority {
        if (context == null) return Priority.HIGH
        return if (context.isLowRamDevice) {
            Priority.HIGH
        } else {
            Priority.IMMEDIATE
        }
    }
}

class SimpleStringRequest(private val context:Context?, method:Int, url:String,
                          listener:Response.Listener<String>,
                          errorListener:Response.ErrorListener):
        StringRequest(method, url, listener, errorListener) {
    override fun getPriority():Priority {
        if (context == null) return Priority.HIGH
        return if (context.isLowRamDevice) {
            Priority.HIGH
        } else {
            Priority.IMMEDIATE
        }
    }
}