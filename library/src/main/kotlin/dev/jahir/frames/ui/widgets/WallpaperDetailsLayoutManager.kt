package dev.jahir.frames.ui.widgets

import android.content.Context
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class WallpaperDetailsLayoutManager
    (
    context: Context,
    spanCount: Int = 3,
    @RecyclerView.Orientation orientation: Int = RecyclerView.VERTICAL,
    reverseLayout: Boolean = false
) : GridLayoutManager(context, spanCount, orientation, reverseLayout){


    init {

        spanSizeLookup = object: SpanSizeLookup(){
            override fun getSpanSize(position: Int): Int {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }
        }

    }

}