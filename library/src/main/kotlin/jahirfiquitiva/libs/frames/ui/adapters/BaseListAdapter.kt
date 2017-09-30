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

import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import jahirfiquitiva.libs.frames.helpers.extensions.clearChildrenAnimations
import jahirfiquitiva.libs.frames.helpers.utils.ListDiffCallback
import jahirfiquitiva.libs.frames.ui.adapters.presenters.ItemsAdapterPresenter
import jahirfiquitiva.libs.frames.ui.adapters.viewholders.GlideSectionedViewHolder
import jahirfiquitiva.libs.frames.ui.adapters.viewholders.GlideViewHolder

abstract class BaseListAdapter<T, VH:RecyclerView.ViewHolder>(private val maxLoad:Int):
        RecyclerView.Adapter<VH>(), ItemsAdapterPresenter<T> {
    
    private var lastAnimatedPosition = -1
    val list = ArrayList<T>()
    
    var actualItemCount = maxLoad
        private set
    
    fun allowMoreItemsLoad() {
        val prevSize = itemCount
        val newCount = actualItemCount + maxLoad
        actualItemCount = if (newCount >= list.size) list.size else newCount
        notifyItemRangeInserted(prevSize, itemCount)
    }
    
    override fun getItemCount():Int {
        return if (actualItemCount <= 0) {
            list.size
        } else {
            if (actualItemCount <= list.size) actualItemCount
            else list.size
        }
    }
    
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
    
    override fun onBindViewHolder(holder:VH, position:Int, payloads:MutableList<Any>?) {
        if (payloads != null) {
            if (payloads.isNotEmpty()) {
                doBind(holder, position, true)
            } else {
                onBindViewHolder(holder, position)
            }
        } else {
            onBindViewHolder(holder, position)
        }
    }
    
    abstract fun doBind(holder:VH, position:Int, shouldAnimate:Boolean)
    
    override fun onViewRecycled(holder:VH) {
        super.onViewRecycled(holder)
        if (holder is GlideViewHolder) holder.doOnRecycle()
        else if (holder is GlideSectionedViewHolder) holder.doOnRecycle()
    }
    
    override fun onViewDetachedFromWindow(holder:VH) {
        super.onViewDetachedFromWindow(holder)
        holder.itemView?.clearChildrenAnimations()
    }
    
    override fun clearList() {
        val size = itemCount
        list.clear()
        notifyItemRangeRemoved(0, size)
    }
    
    override fun addAll(newItems:ArrayList<T>) {
        val prevSize = itemCount
        list.addAll(newItems)
        notifyItemRangeInserted(prevSize, newItems.size)
    }
    
    override fun setItems(newItems:ArrayList<T>) {
        list.clear()
        list.addAll(newItems)
        notifyDataSetChanged()
    }
    
    override fun updateItems(newItems:ArrayList<T>, detectMoves:Boolean) {
        updateItems(newItems, object:ListDiffCallback<T>(list, newItems) {}, detectMoves)
    }
    
    override fun updateItems(newItems:ArrayList<T>, callback:ListDiffCallback<T>,
                             detectMoves:Boolean) {
        val result = DiffUtil.calculateDiff(callback, detectMoves)
        list.clear()
        list.addAll(newItems)
        result.dispatchUpdatesTo(this)
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
    
    override fun addItem(newItem:T) {
        val prevSize = itemCount
        list.add(newItem)
        notifyItemRangeInserted(prevSize, itemCount)
    }
    
    
}