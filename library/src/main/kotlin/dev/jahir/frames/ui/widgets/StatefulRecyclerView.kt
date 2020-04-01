package dev.jahir.frames.ui.widgets

import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import dev.jahir.frames.R
import dev.jahir.frames.extensions.gone
import dev.jahir.frames.extensions.prefs
import dev.jahir.frames.extensions.visible
import dev.jahir.frames.extensions.visibleIf

open class StatefulRecyclerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = -1
) : RecyclerView(context, attrs, defStyleAttr) {

    var stateDrawableModifier: StateDrawableModifier? = null

    var loading: Boolean = true
        set(value) {
            field = value
            searching = false
            internalSetState()
        }

    var searching: Boolean = false

    @StringRes
    var loadingText: Int = R.string.loading

    @StringRes
    var emptyText: Int = R.string.nothing_found

    @StringRes
    var noSearchResultsText: Int = R.string.no_results_found

    @DrawableRes
    var emptyDrawable: Int = R.drawable.ic_empty_section

    @DrawableRes
    var noSearchResultsDrawable: Int = R.drawable.ic_empty_results

    private var state: State = State.LOADING
        set(value) {
            if (value != field) {
                field = value
                internalOnStateChanged()
            }
        }

    private var stateRootLayoutId: Int = 0
    private var stateImageViewId: Int = 0
    private var stateProgressBarId: Int = 0
    private var stateTextViewId: Int = 0

    private var stateRootLayout: View? = null
    private var stateImageView: AppCompatImageView? = null
    private var stateProgressBar: ProgressBar? = null
    private var stateTextView: TextView? = null

    private val observer: StatefulAdapterObserver by lazy {
        StatefulAdapterObserver { internalSetState() }
    }

    init {
        isSaveEnabled = true
        init(context, attrs)
    }

    private fun init(context: Context, attributeSet: AttributeSet?) {
        val a = context.obtainStyledAttributes(attributeSet, R.styleable.StatefulRecyclerView, 0, 0)
        try {
            stateRootLayoutId = a.getResourceId(
                R.styleable.StatefulRecyclerView_stateRootLayout,
                R.id.state_root_layout
            )
            stateImageViewId =
                a.getResourceId(R.styleable.StatefulRecyclerView_stateImageView, R.id.state_image)
            stateProgressBarId =
                a.getResourceId(
                    R.styleable.StatefulRecyclerView_stateProgressBar,
                    R.id.state_progress_bar
                )
            stateTextViewId =
                a.getResourceId(R.styleable.StatefulRecyclerView_stateTextView, R.id.state_text)
        } finally {
            a.recycle()
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        (parent as? View)?.let {
            stateRootLayout = it.findViewById(stateRootLayoutId)
            stateImageView = it.findViewById(stateImageViewId)
            stateProgressBar = it.findViewById(stateProgressBarId)
            stateTextView = it.findViewById(stateTextViewId)
        }
        internalOnStateChanged()
    }

    override fun setAdapter(adapter: Adapter<*>?) {
        val oldAdapter = getAdapter()
        oldAdapter?.unregisterAdapterDataObserver(observer)
        super.setAdapter(adapter)
        adapter?.registerAdapterDataObserver(observer)
        internalSetState()
    }

    private fun internalOnStateChanged() {
        stateRootLayout?.visibleIf(state != State.NORMAL)
        visibleIf(state == State.NORMAL)

        stateTextView?.text = context.getString(
            when (state) {
                State.LOADING -> loadingText
                else -> if (searching) noSearchResultsText else emptyText
            }
        )

        stateProgressBar?.visibleIf(state == State.LOADING)
        stateTextView?.visibleIf(state != State.NORMAL)

        val drawable: Drawable? = when (state) {
            State.EMPTY -> ContextCompat.getDrawable(
                context,
                if (searching) noSearchResultsDrawable else emptyDrawable
            )
            else -> null
        }
        stateImageView?.setImageDrawable(
            stateDrawableModifier?.modifyDrawable(drawable) ?: drawable
        )
        if (state == State.EMPTY) {
            stateImageView?.visible(context.prefs.animationsEnabled)
        } else stateImageView?.gone()
    }

    private fun internalSetState() {
        state = when {
            loading -> State.LOADING
            (adapter?.itemCount ?: 0) > 0 -> State.NORMAL
            else -> State.EMPTY
        }
    }

    override fun onSaveInstanceState(): Parcelable? {
        val superState = super.onSaveInstanceState()
        val myState = SavedState(superState)
        myState.loading = this.loading
        myState.stateValue = this.state.value
        return myState
    }

    override fun onRestoreInstanceState(state: Parcelable) {
        val savedState = state as? SavedState
        super.onRestoreInstanceState(savedState?.superState)
        this.loading = savedState?.loading ?: true
        this.state = State.getForValue(savedState?.stateValue ?: -1)
    }

    interface StateDrawableModifier {
        fun modifyDrawable(drawable: Drawable?): Drawable? = drawable
    }

    private enum class State(val value: Int) {
        NORMAL(1), EMPTY(0), LOADING(-1);

        companion object {
            internal fun getForValue(value: Int) = when (value) {
                0 -> EMPTY
                1 -> NORMAL
                else -> LOADING
            }
        }
    }

    private class StatefulAdapterObserver(private val onUpdated: (() -> Unit)? = null) :
        AdapterDataObserver() {
        override fun onChanged() {
            super.onChanged()
            onUpdated?.invoke()
        }

        override fun onItemRangeChanged(positionStart: Int, itemCount: Int) {
            super.onItemRangeChanged(positionStart, itemCount)
            onUpdated?.invoke()
        }

        override fun onItemRangeChanged(positionStart: Int, itemCount: Int, payload: Any?) {
            super.onItemRangeChanged(positionStart, itemCount, payload)
            onUpdated?.invoke()
        }

        override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
            super.onItemRangeInserted(positionStart, itemCount)
            onUpdated?.invoke()
        }

        override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
            super.onItemRangeRemoved(positionStart, itemCount)
            onUpdated?.invoke()
        }

        override fun onItemRangeMoved(fromPosition: Int, toPosition: Int, itemCount: Int) {
            super.onItemRangeMoved(fromPosition, toPosition, itemCount)
            onUpdated?.invoke()
        }
    }

    private class SavedState : BaseSavedState {
        var loading: Boolean = true
        var stateValue: Int = State.LOADING.value

        internal constructor(superState: Parcelable?) : super(superState)
        private constructor(parcel: Parcel?) : super(parcel) {
            loading = (parcel?.readInt() ?: 1) == 1
            stateValue = (parcel?.readInt() ?: 0)
        }

        override fun writeToParcel(out: Parcel, flags: Int) {
            super.writeToParcel(out, flags)
            out.writeInt(if (loading) 1 else 0)
            out.writeInt(stateValue)
        }
    }
}