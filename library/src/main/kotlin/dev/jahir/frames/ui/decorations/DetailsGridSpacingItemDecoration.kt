package dev.jahir.frames.ui.decorations

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import dev.jahir.frames.extensions.MAX_FRAMES_PALETTE_COLORS
import dev.jahir.frames.extensions.dpToPx
import dev.jahir.frames.ui.adapters.WallpaperDetailsAdapter
import kotlin.math.roundToInt

open class DetailsGridSpacingItemDecoration(
    private val spacing: Int
) : RecyclerView.ItemDecoration() {

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
            if ((actualPosition?.section() ?: 0) == 1) {
                val position = actualPosition?.relativePos() ?: -1
                if (position < 0) return

                val colorsColumnsCount = (MAX_FRAMES_PALETTE_COLORS / 2.0).roundToInt()
                val column = position % colorsColumnsCount
                val row = position / colorsColumnsCount
                val rowCount =
                    ((parent.adapter as? WallpaperDetailsAdapter)?.getItemCount(1)
                        ?: 0) / colorsColumnsCount
                val halfSpacing = (spacing / 2.0).roundToInt()
                val colorSmallSpacing = (halfSpacing / 4.0).roundToInt()

                outRect.left = if (column == 0) spacing else halfSpacing
                outRect.right = if (column == colorsColumnsCount) spacing else halfSpacing
                outRect.top = if (row == 0) colorsTopBottomSpacing else (colorSmallSpacing * -1)
                outRect.bottom =
                    if (row == rowCount - 1) colorsTopBottomSpacing else (colorSmallSpacing * -1)
            } else {
                outRect.left = spacing
                outRect.right = spacing
            }
        } else {
            outRect.left = spacing * -1
            outRect.right = spacing * -1
        }
    }
}