package dev.jahir.frames.ui.decorations

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import dev.jahir.frames.extensions.dpToPx
import dev.jahir.frames.ui.adapters.WallpaperDetailsAdapter
import kotlin.math.roundToInt

open class DetailsGridSpacingItemDecoration(
    private val spacing: Int
) : RecyclerView.ItemDecoration() {

    private val colorsSideSpacing = 16.dpToPx
    private val colorsTopBottomSpacing = 12.dpToPx

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        super.getItemOffsets(outRect, view, parent, state)
        internalOffsetsSetup(outRect, view, parent)
    }

    open fun internalOffsetsSetup(outRect: Rect, view: View, parent: RecyclerView) {
        val absolutePosition = parent.getChildAdapterPosition(view)
        val actualPosition =
            (parent.adapter as? WallpaperDetailsAdapter)?.getRelativePosition(absolutePosition)
        if ((actualPosition?.relativePos() ?: -1) >= 0) {
            if ((actualPosition?.section() ?: 0) != 1) {
                outRect.left = spacing
                outRect.right = spacing - 2 * spacing / 1
            } else {
                val position = actualPosition?.relativePos() ?: -1
                if (position < 0) return

                val column = position % 3
                val row = position / 3
                val rowCount =
                    ((parent.adapter as? WallpaperDetailsAdapter)?.getItemCount(1) ?: 0) / 3
                val halfSpacing = (spacing / 2.0).roundToInt()

                outRect.left = if (column == 0) colorsSideSpacing else halfSpacing
                outRect.right = if (column == 2) colorsSideSpacing else halfSpacing
                outRect.top = if (row == 0) colorsTopBottomSpacing else halfSpacing
                outRect.bottom = if (row == rowCount - 1) colorsTopBottomSpacing else halfSpacing
            }
        }
    }
}