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
package jahirfiquitiva.libs.frames.ui.widgets

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.support.annotation.DrawableRes
import android.support.annotation.StringRes
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import ca.allanwang.kau.utils.visibleIf
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.request.RequestOptions
import jahirfiquitiva.libs.kauextensions.extensions.secondaryTextColor

open class EmptyViewRecyclerView:RecyclerView {
    var loadingView:View? = null
    var emptyView:View? = null
    var textView:TextView? = null
    
    var loadingText:String = ""
    var emptyText:String = ""
    
    var state:State = State.LOADING
        set(value) {
            if (value != field) {
                field = value
                updateStateViews()
            }
        }
    
    fun setLoadingText(@StringRes res:Int) {
        loadingText = context.getString(res)
    }
    
    fun setEmptyText(@StringRes res:Int) {
        emptyText = context.getString(res)
    }
    
    fun setEmptyImage(img:Bitmap) {
        emptyView?.let {
            if (it is ImageView) it.setImageBitmap(img)
            else throw UnsupportedOperationException(
                    "Cannot set a Bitmap in a View that is not ImageView")
        }
    }
    
    fun setEmptyImage(img:Drawable) {
        emptyView?.let {
            if (it is ImageView) it.setImageDrawable(img)
            else throw UnsupportedOperationException(
                    "Cannot set a Drawable in a View that is not ImageView")
        }
    }
    
    fun setEmptyImage(@DrawableRes res:Int) {
        emptyView?.let {
            if (it is ImageView) {
                Glide.with(context).load(res)
                        .apply(RequestOptions().priority(Priority.IMMEDIATE).dontAnimate())
                        .into(it)
            } else {
                throw UnsupportedOperationException(
                        "Cannot set a Drawable in a View that is not ImageView")
            }
        }
    }
    
    constructor(context:Context):super(context)
    constructor(context:Context, attributeSet:AttributeSet):super(context, attributeSet)
    constructor(context:Context, attributeSet:AttributeSet, defStyleAttr:Int)
            :super(context, attributeSet, defStyleAttr)
    
    private fun setStateInternal() {
        state = if (adapter != null) {
            val items = adapter.itemCount
            if (items > 0) {
                State.NORMAL
            } else {
                State.EMPTY
            }
        } else {
            State.LOADING
        }
    }
    
    private fun updateStateViews() {
        textView?.setTextColor(context.secondaryTextColor)
        textView?.visibleIf(state != State.NORMAL)
        loadingView?.visibleIf(state == State.LOADING)
        emptyView?.visibleIf(state == State.EMPTY)
        visibleIf(state == State.NORMAL)
    }
    
    private val observer:RecyclerView.AdapterDataObserver = object:RecyclerView.AdapterDataObserver() {
        override fun onChanged() {
            super.onChanged()
            setStateInternal()
        }
        
        override fun onItemRangeChanged(positionStart:Int, itemCount:Int) {
            super.onItemRangeChanged(positionStart, itemCount)
            setStateInternal()
        }
        
        override fun onItemRangeChanged(positionStart:Int, itemCount:Int, payload:Any?) {
            super.onItemRangeChanged(positionStart, itemCount, payload)
            setStateInternal()
        }
        
        override fun onItemRangeInserted(positionStart:Int, itemCount:Int) {
            super.onItemRangeInserted(positionStart, itemCount)
            setStateInternal()
        }
        
        override fun onItemRangeRemoved(positionStart:Int, itemCount:Int) {
            super.onItemRangeRemoved(positionStart, itemCount)
            setStateInternal()
        }
        
        override fun onItemRangeMoved(fromPosition:Int, toPosition:Int, itemCount:Int) {
            super.onItemRangeMoved(fromPosition, toPosition, itemCount)
            setStateInternal()
        }
    }
    
    override fun setAdapter(adapter:Adapter<*>?) {
        val oldAdapter = getAdapter()
        oldAdapter?.unregisterAdapterDataObserver(observer)
        super.setAdapter(adapter)
        adapter?.registerAdapterDataObserver(observer)
        updateStateViews()
    }
    
    enum class State {
        EMPTY, NORMAL, LOADING
    }
}