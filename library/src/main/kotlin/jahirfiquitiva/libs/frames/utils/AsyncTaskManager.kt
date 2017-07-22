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
package jahirfiquitiva.libs.frames.utils

import android.os.AsyncTask

open class AsyncTaskManager<T, P>(val param:P,
                                  val onPreEx:() -> Unit,
                                  val loadStuff:(p:P) -> T,
                                  val onReady:(T) -> Unit) {

    private var task:AsyncTask<Unit, Unit, T>

    init {
        task = object:AsyncTask<Unit, Unit, T>() {
            override fun onPreExecute() {
                super.onPreExecute()
                onPreEx()
            }

            override fun doInBackground(vararg p0:Unit?):T {
                return loadStuff(param)
            }

            override fun onPostExecute(result:T?) {
                result?.let {
                    super.onPostExecute(it)
                    onReady(it)
                }
            }
        }
    }

    fun execute() {
        try {
            task.execute()
        } catch (ignored:Exception) {
        }
    }

    fun cancelTask(interrupt:Boolean = false) {
        try {
            task.cancel(interrupt)
        } catch (ignored:Exception) {
        }
    }
}