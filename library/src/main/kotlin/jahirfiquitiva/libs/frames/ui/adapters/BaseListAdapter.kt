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

import android.support.v7.widget.RecyclerView
import jahirfiquitiva.libs.frames.helpers.extensions.clearChildrenAnimations
import jahirfiquitiva.libs.frames.ui.adapters.presenters.ItemsAdapterPresenter

abstract class BaseListAdapter<T, VH:RecyclerView.ViewHolder>:
        RecyclerView.Adapter<VH>(), ItemsAdapterPresenter<T> {
    
    private var lastAnimatedPosition = -1
    val list = ArrayList<T>()
    
    override fun onBindViewHolder(holder:VH, position:Int) {
        if (position in 0..itemCount) {
            if (position > lastAnimatedPosition) {
                lastAnimatedPosition = position
                doBind(holder, position, true)
            } else {
                doBind(holder, position, false)
            }
        }
    }
    
    abstract fun doBind(holder:VH, position:Int, shouldAnimate:Boolean)
    
    override fun onViewDetachedFromWindow(holder:VH) {
        super.onViewDetachedFromWindow(holder)
        holder.itemView?.clearChildrenAnimations()
    }
    
    override fun getItemCount():Int = list.size
    
    override fun clearList() {
        val size = itemCount
        list.clear()
        notifyItemRangeRemoved(0, size)
    }
    
    override fun addAll(items:ArrayList<T>) {
        val prevSize = itemCount
        list.addAll(items)
        notifyItemRangeInserted(prevSize, items.size)
    }
    
    override fun setItems(items:ArrayList<T>) {
        list.clear()
        list.addAll(items)
        notifyDataSetChanged()
    }
    
    override fun removeItem(item:T) {
        val prevSize = itemCount
        val index = list.indexOf(item)
        if (index < 0) return
        list.remove(item)
        notifyItemRangeRemoved(index, prevSize)
    }
    
    override fun updateItem(item:T) {
        val prevSize = itemCount
        val index = list.indexOf(item)
        if (index < 0) return
        notifyItemRangeChanged(index, prevSize)
    }
    
    override fun addItem(item:T) {
        val prevSize = itemCount
        list.add(item)
        notifyItemRangeInserted(prevSize, itemCount)
    }
}