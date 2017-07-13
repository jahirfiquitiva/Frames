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
import android.content.Context

abstract class ListViewModel<T>:ViewModel() {
    val items = MutableLiveData<ArrayList<T>>()

    open fun loadData(context:Context) {
        if (items.value != null && (items.value?.size ?: 0) > 0)
            items.postValue(items.value)
        items.postValue(loadItems(context))
    }

    abstract protected fun loadItems(context:Context):ArrayList<T>
}