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

package jahirfiquitiva.libs.frames.ui.adapters.presenters

import jahirfiquitiva.libs.frames.helpers.utils.diff.BaseDiffCallback

interface ItemsAdapterPresenter<T> {
    fun clearList()
    fun addAll(newItems:ArrayList<T>)
    fun setItems(newItems:ArrayList<T>)
    fun updateItems(newItems:ArrayList<T>, detectMoves:Boolean)
    fun updateItems(newItems:ArrayList<T>, callback:BaseDiffCallback<T>, detectMoves:Boolean)
    fun removeItem(item:T)
    fun updateItem(item:T)
    fun addItem(newItem:T)
}