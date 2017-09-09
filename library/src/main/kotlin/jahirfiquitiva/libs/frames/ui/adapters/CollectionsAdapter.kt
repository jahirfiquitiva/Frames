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
package jahirfiquitiva.libs.frames.ui.adapters

import android.view.ViewGroup
import ca.allanwang.kau.utils.inflate
import com.bumptech.glide.RequestManager
import jahirfiquitiva.libs.frames.R
import jahirfiquitiva.libs.frames.data.models.Collection
import jahirfiquitiva.libs.frames.helpers.utils.diff.CollectionsDiffCallback
import jahirfiquitiva.libs.frames.ui.adapters.viewholders.CollectionHolder

class CollectionsAdapter(private val manager:RequestManager,
                         private val listener:(Collection) -> Unit):
        BaseListAdapter<Collection, CollectionHolder>() {
    
    override fun doBind(holder:CollectionHolder, position:Int, shouldAnimate:Boolean) =
            holder.setItem(manager, list[position], listener)
    
    override fun onCreateViewHolder(parent:ViewGroup?, viewType:Int):CollectionHolder? =
            parent?.inflate(R.layout.item_collection)?.let { CollectionHolder(it) }
    
    override fun updateItems(newItems:ArrayList<Collection>, detectMoves:Boolean) {
        updateItems(newItems, CollectionsDiffCallback(list, newItems), detectMoves)
    }
}