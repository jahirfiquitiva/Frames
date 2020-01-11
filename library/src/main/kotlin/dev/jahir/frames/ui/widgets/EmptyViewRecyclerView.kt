package dev.jahir.frames.ui.widgets

import android.content.Context
import android.util.AttributeSet
import androidx.recyclerview.widget.RecyclerView
import dev.jahir.frames.extensions.visibleIf

@Suppress("unused")
class EmptyViewRecyclerView @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyleAttr: Int = 0
) : RecyclerView(context, attributeSet, defStyleAttr) {

    var stateChangeListener: StateChangeListener? = null
    var emptyView: EmptyView? = null
    var state: State = State.LOADING
        set(value) {
            if (value != field) {
                val items = adapter?.itemCount ?: 0
                field = if (value == State.NORMAL && items <= 0) State.EMPTY else value
                updateStateViews()
            }
        }

    fun setState(newState: State, emptyView: EmptyView? = null) {
        this.emptyView = emptyView
        this.state = newState
    }

    private fun setStateInternal() {
        state = adapter?.let {
            when {
                // TODO: Double check
                // state == State.LOADING -> State.LOADING
                it.itemCount <= 0 -> State.EMPTY
                else -> State.NORMAL
            }
        } ?: State.LOADING
    }

    private fun updateStateViews() {
        stateChangeListener?.onStateChanged(state, emptyView)
        emptyView?.showIf(state != State.NORMAL)
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

    enum class State { EMPTY, NORMAL, LOADING }

    interface StateChangeListener {
        fun onStateChanged(state: State, emptyView: EmptyView?)
    }
}