/*
 * Copyright (c) 2018. Jahir Fiquitiva
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
import android.graphics.drawable.Animatable
import android.graphics.drawable.Drawable
import android.support.annotation.DrawableRes
import android.support.annotation.StringRes
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import ca.allanwang.kau.utils.drawable
import ca.allanwang.kau.utils.gone
import ca.allanwang.kau.utils.postDelayed
import ca.allanwang.kau.utils.visible
import ca.allanwang.kau.utils.visibleIf
import jahirfiquitiva.libs.kauextensions.extensions.activeIconsColor
import jahirfiquitiva.libs.kauextensions.extensions.applyColorFilter
import jahirfiquitiva.libs.kauextensions.extensions.hasContent
import jahirfiquitiva.libs.kauextensions.extensions.secondaryTextColor
import jahirfiquitiva.libs.kauextensions.extensions.setDecodedBitmap

open class EmptyViewRecyclerView : RecyclerView {
    
    var loadingView: View? = null
    var emptyView: View? = null
    var textView: TextView? = null
    
    var loadingText: String = ""
    var emptyText: String = ""
    
    var state: State = State.LOADING
        set(value) {
            if (value != field) {
                field = value
                updateStateViews()
            }
        }
    
    fun setLoadingText(@StringRes res: Int) {
        loadingText = context.getString(res)
    }
    
    fun setEmptyText(@StringRes res: Int) {
        emptyText = context.getString(res)
    }
    
    fun setEmptyImage(image: Bitmap?) {
        emptyView?.let {
            if (it is ImageView) {
                it.setImageBitmap(image)
                updateEmptyState()
            } else {
                throw UnsupportedOperationException(
                        "Cannot set a Drawable in a View that is not ImageView")
            }
        }
    }
    
    fun setEmptyImage(image: Drawable?) {
        emptyView?.let {
            if (it is ImageView) {
                it.setImageDrawable(image)
                updateEmptyState()
            } else {
                throw UnsupportedOperationException(
                        "Cannot set a Drawable in a View that is not ImageView")
            }
        }
    }
    
    fun setEmptyImage(@DrawableRes res: Int) {
        emptyView?.let {
            if (it is ImageView) {
                try {
                    it.setDecodedBitmap(res)
                    updateEmptyState()
                } catch (e: Exception) {
                    try {
                        it.setImageDrawable(context.drawable(res))
                        updateEmptyState()
                    } catch (e: Exception) {
                    }
                }
            } else {
                throw UnsupportedOperationException(
                        "Cannot set a Drawable in a View that is not ImageView")
            }
        }
    }
    
    constructor(context: Context) : super(context)
    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet)
    constructor(context: Context, attributeSet: AttributeSet, defStyleAttr: Int)
            : super(context, attributeSet, defStyleAttr)
    
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
        val rightText = when (state) {
            State.LOADING -> loadingText
            State.EMPTY -> emptyText
            else -> ""
        }
        if (rightText.hasContent()) textView?.text = rightText
        textView?.setTextColor(context.secondaryTextColor)
        textView?.visibleIf(state != State.NORMAL && rightText.hasContent())
        loadingView?.visibleIf(state == State.LOADING)
        updateEmptyState()
        visibleIf(state == State.NORMAL)
    }
    
    private val observer: RecyclerView.AdapterDataObserver = object :
            RecyclerView.AdapterDataObserver() {
        override fun onChanged() {
            super.onChanged()
            setStateInternal()
        }
        
        override fun onItemRangeChanged(positionStart: Int, itemCount: Int) {
            super.onItemRangeChanged(positionStart, itemCount)
            setStateInternal()
        }
        
        override fun onItemRangeChanged(positionStart: Int, itemCount: Int, payload: Any?) {
            super.onItemRangeChanged(positionStart, itemCount, payload)
            setStateInternal()
        }
        
        override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
            super.onItemRangeInserted(positionStart, itemCount)
            setStateInternal()
        }
        
        override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
            super.onItemRangeRemoved(positionStart, itemCount)
            setStateInternal()
        }
        
        override fun onItemRangeMoved(fromPosition: Int, toPosition: Int, itemCount: Int) {
            super.onItemRangeMoved(fromPosition, toPosition, itemCount)
            setStateInternal()
        }
    }
    
    override fun setAdapter(adapter: Adapter<*>?) {
        val oldAdapter = getAdapter()
        oldAdapter?.unregisterAdapterDataObserver(observer)
        super.setAdapter(adapter)
        adapter?.registerAdapterDataObserver(observer)
        setStateInternal()
    }
    
    private fun View.showAndAnimate() {
        (this as? ImageView)?.drawable?.applyColorFilter(context.activeIconsColor)
        visible()
        (this as? ImageView)?.let {
            postDelayed(250) { (it.drawable as? Animatable)?.start() }
        }
    }
    
    fun updateEmptyState() {
        if (state == State.EMPTY) {
            emptyView?.showAndAnimate()
        } else {
            emptyView?.gone()
        }
    }
    
    enum class State {
        EMPTY, NORMAL, LOADING
    }
}