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

package jahirfiquitiva.libs.frames.models.viewmodels

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import jahirfiquitiva.libs.frames.utils.AsyncTaskManager

abstract class ListViewModel<T, P>:ViewModel() {
    val items = MutableLiveData<ArrayList<T>>()
    var param:P? = null
    var task:AsyncTaskManager<ArrayList<T>, P>? = null

    fun loadData(p:P, forceLoad:Boolean = false) {
        param = p
        task = AsyncTaskManager(p, {},
                                { internalLoad(it, forceLoad) },
                                { postResult(it) })
        task?.execute()
    }

    fun stopTask(interrupt:Boolean = false) {
        task?.cancelTask(interrupt)
    }

    private fun internalLoad(p:P, forceLoad:Boolean = false):ArrayList<T> {
        if (forceLoad) {
            return ArrayList(loadItems(p).distinct())
        } else {
            if (items.value != null && (items.value?.size ?: 0) > 0) {
                val list = ArrayList<T>()
                items.value?.let { list.addAll(it.distinct()) }
                return list
            } else {
                return ArrayList(loadItems(p).distinct())
            }
        }
    }

    open fun postResult(data:ArrayList<T>) {
        items.postValue(ArrayList(data.distinct()))
    }

    abstract protected fun loadItems(p:P):ArrayList<T>
}