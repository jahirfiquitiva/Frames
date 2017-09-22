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
import java.lang.ref.WeakReference
import java.util.concurrent.Executor

open class SimpleAsyncTask<Parameter, Result>(
        private val param:WeakReference<Parameter>,
        private val callback:AsyncTaskCallback<Parameter, Result>) {
    
    private var task:AsyncTask<Unit, Unit, Result>
    
    init {
        task = object:AsyncTask<Unit, Unit, Result>() {
            override fun onPreExecute() {
                super.onPreExecute()
                callback.doBefore()
            }
            
            override fun doInBackground(vararg ignored:Unit?):Result? {
                return try {
                    val actualParam = param.get()
                    if (actualParam != null) callback.doLoad(actualParam)
                    else {
                        callback.onError(NullPointerException(""))
                        null
                    }
                } catch (e:Exception) {
                    callback.onError(e)
                    null
                }
            }
            
            override fun onPostExecute(result:Result?) {
                super.onPostExecute(result)
                if (result != null) callback.onSuccess(result)
                else callback.onError(NullPointerException("Loaded object was null"))
            }
        }
    }
    
    fun execute(executor:Executor? = null) {
        try {
            if (executor != null) task.executeOnExecutor(executor)
            else task.execute()
        } catch (ignored:Exception) {
        }
    }
    
    fun cancel(interrupt:Boolean = false) {
        try {
            task.cancel(interrupt)
        } catch (ignored:Exception) {
        }
    }
    
    abstract class AsyncTaskCallback<in Parameter, Result> {
        open fun doBefore() {}
        abstract fun doLoad(param:Parameter):Result?
        abstract fun onSuccess(result:Result)
        open fun onError(e:Exception?) = e?.printStackTrace()
    }
}