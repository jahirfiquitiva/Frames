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
package jahirfiquitiva.libs.frames.helpers.utils

import android.os.AsyncTask
import android.util.Log
import com.bumptech.glide.RequestManager
import com.bumptech.glide.request.FutureTarget
import com.bumptech.glide.request.target.Target
import java.io.File
import java.util.*
import java.util.concurrent.*

@Suppress("DEPRECATION")
class GlidePictureDownloader(val glide:RequestManager,
                             val onResult:(result:Result) -> Unit = {}):AsyncTask<String, File, Result>() {
    
    override fun doInBackground(vararg params:String?):Result {
        val requests = arrayOfNulls<FutureTarget<*>>(params.size)
        
        // fire everything into Glide queue
        for (i in 0 until params.size) {
            if (isCancelled) {
                break
            }
            requests[i] = glide
                    .load(params[i])
                    .downloadOnly(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
        }
        
        val result = Result()
        for (i in 0 until params.size) {
            if (isCancelled) {
                for (j in i until params.size) {
                    if (requests[i] != null) glide.clear(requests[i])
                    result.failures.put(params[j] ?: "Error $j", CancellationException())
                }
                break
            }
            try {
                val file = requests[i]?.get(10, TimeUnit.SECONDS) as File
                result.success.put(params[i] ?: "Success $i", file)
                publishProgress(file)
            } catch (e:Exception) {
                result.failures.put(params[i] ?: "Error $i", e)
            } finally {
                glide.clear(requests[i])
            }
        }
        return result
    }
    
    override fun onProgressUpdate(vararg values:File) {
        for (file in values) {
            Log.v("WallpaperDownload", "Finished " + file)
        }
    }
    
    override fun onPostExecute(result:Result?) {
        super.onPostExecute(result)
        result?.let {
            Log.i("WallpaperDownload", String.format(Locale.ROOT, "Downloaded %d files, %d failed.",
                                                     it.success.size, it.failures.size))
            onResult(it)
        }
    }
    
}

class Result {
    var success = HashMap<String, File>()
    var failures = HashMap<String, Exception>()
}