package dev.jahir.frames.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import dev.jahir.frames.R
import dev.jahir.frames.data.models.Wallpaper
import dev.jahir.frames.ui.adapters.WallpapersAdapter
import dev.jahir.frames.ui.decorations.GridSpacingItemDecoration
import dev.jahir.frames.ui.fragments.base.BaseFramesFragment

class WallpapersFragment : BaseFramesFragment<Wallpaper>() {

    private val wallsAdapter: WallpapersAdapter by lazy { WallpapersAdapter() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val columnsCount =
            context?.resources?.getInteger(R.integer.min_wallpapers_columns_count) ?: 2

        recyclerView?.adapter = wallsAdapter
        recyclerView?.layoutManager =
            GridLayoutManager(context, columnsCount, GridLayoutManager.VERTICAL, false)
        recyclerView?.addItemDecoration(
            GridSpacingItemDecoration(
                columnsCount, resources.getDimensionPixelSize(R.dimen.grids_spacing)
            )
        )

        swipeRefreshLayout?.setOnRefreshListener {
            Log.d("Frames", "Actualizando")
            swipeRefreshLayout?.isRefreshing = false
        }
    }

    override fun updateItems(newItems: ArrayList<Wallpaper>) {
        super.updateItems(newItems)
        wallsAdapter.wallpapers = items
        wallsAdapter.notifyDataSetChanged()
    }

    companion object {
        internal const val TAG = "Wallpapers"

        @JvmStatic
        fun create(list: ArrayList<Wallpaper> = ArrayList()) = WallpapersFragment().apply {
            updateItems(list)
        }
    }
}