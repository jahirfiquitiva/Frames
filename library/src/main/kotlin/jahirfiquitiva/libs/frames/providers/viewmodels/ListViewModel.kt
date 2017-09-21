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

abstract class ListViewModel<Parameter, Result>:BasicViewModel<Parameter, MutableList<Result>>() {
    
    override fun internalLoad(param:Parameter, forceLoad:Boolean):MutableList<Result>? =
            if (forceLoad) {
                ArrayList(loadItems(param).distinct())
            } else {
                if (items.value != null && (items.value?.size ?: 0) > 0) {
                    val list = ArrayList<Result>()
                    items.value?.let { list.addAll(it.distinct()) }
                    list
                } else {
                    ArrayList(loadItems(param).distinct())
                }
            }
    
    override fun postResult(data:MutableList<Result>) {
        items.value?.clear()
        val list = data.distinct().toMutableList()
        items.postValue(list)
        observer?.onValuePosted(list)
    }
    
    private var observer:CustomObserver<Result>? = null
    
    fun setCustomObserver(observer:CustomObserver<Result>) {
        this.observer = observer
    }
    
    interface CustomObserver<Result> {
        fun onValuePosted(data:MutableList<Result>)
    }
}