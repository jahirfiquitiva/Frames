package dev.jahir.frames.ui.widgets

import android.content.Context
import android.util.AttributeSet
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import dev.jahir.frames.extensions.visibleIf

open class EmptyViewRecyclerView : RecyclerView {

    var emptyView: EmptyView? = null
    var emptyText: String = ""

    var state: State = State.LOADING
        set(value) {
            if (value != field) {
                val items = adapter?.itemCount ?: 0
                field = if (value == State.NORMAL && items <= 0) State.EMPTY else value
                updateStateViews()
            }
        }

    fun setEmptyText(@StringRes res: Int) {
        emptyText = context.getString(res)
    }

    fun setEmptyImage(@DrawableRes res: Int) {
        emptyView?.let {
            it.setImageDrawable(ContextCompat.getDrawable(context, res))
            updateStateViews()
        }
    }

    constructor(context: Context) : super(context)
    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet)
    constructor(context: Context, attributeSet: AttributeSet, defStyleAttr: Int)
            : super(context, attributeSet, defStyleAttr)

    private fun setStateInternal() {
        state = if (adapter != null) {
            val items = adapter?.itemCount ?: 0
            if (items > 0) {
                State.NORMAL
            } else {
                if (state == State.LOADING) State.LOADING else State.EMPTY
            }
        } else {
            State.LOADING
        }
    }

    private fun updateStateViews() {
        when (state) {
            State.LOADING -> emptyView?.setLoading()
            State.EMPTY -> emptyView?.setEmpty(emptyText)
            else -> emptyView?.hide()
        }
        visibleIf(state == State.NORMAL)
    }

    private val observer: AdapterDataObserver = object : AdapterDataObserver() {
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

    enum class State {
        EMPTY, NORMAL, LOADING
    }
}