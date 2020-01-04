package dev.jahir.frames.ui.fragments

import android.view.View
import androidx.palette.graphics.Palette
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dev.jahir.frames.R
import dev.jahir.frames.data.models.Wallpaper
import dev.jahir.frames.extensions.MAX_FRAMES_PALETTE_COLORS
import dev.jahir.frames.extensions.dpToPx
import dev.jahir.frames.extensions.findView
import dev.jahir.frames.ui.adapters.WallpaperDetailsAdapter
import dev.jahir.frames.ui.decorations.DetailsGridSpacingItemDecoration
import dev.jahir.frames.ui.fragments.base.BaseBottomSheet
import kotlin.math.roundToInt

class WallpaperDetailsFragment : BaseBottomSheet() {

    var wallpaper: Wallpaper? = null
        set(value) {
            field = value
            wallpaperDetailsAdapter.wallpaper = value
            wallpaperDetailsAdapter.notifyDataSetChanged()
        }

    var palette: Palette? = null
        set(value) {
            field = value
            wallpaperDetailsAdapter.palette = value
            wallpaperDetailsAdapter.notifyDataSetChanged()
        }

    private val wallpaperDetailsAdapter: WallpaperDetailsAdapter by lazy {
        WallpaperDetailsAdapter(wallpaper, palette)
    }

    override fun getContentView(): View? {
        val view = View.inflate(context, R.layout.fragment_wallpaper_details, null)

        val recyclerView: RecyclerView? by view.findView(R.id.recycler_view)
        val columns = (MAX_FRAMES_PALETTE_COLORS / 2.0).roundToInt()
        val decoration = DetailsGridSpacingItemDecoration(8.dpToPx)
        val lm = GridLayoutManager(context, columns)

        recyclerView?.layoutManager = lm
        wallpaperDetailsAdapter.setLayoutManager(lm)
        recyclerView?.adapter = wallpaperDetailsAdapter
        recyclerView?.addItemDecoration(decoration)

        return view
    }

    override fun shouldExpandOnShow(): Boolean = true

    companion object {
        @JvmStatic
        fun create(wallpaper: Wallpaper? = null, palette: Palette? = null) =
            WallpaperDetailsFragment().apply {
                this.wallpaper = wallpaper
                this.palette = palette
            }
    }
}