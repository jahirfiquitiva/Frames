package dev.jahir.frames.ui.fragments

import android.view.View
import androidx.palette.graphics.Palette
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dev.jahir.frames.R
import dev.jahir.frames.data.models.Wallpaper
import dev.jahir.frames.extensions.findView
import dev.jahir.frames.ui.adapters.WallpaperDetailsAdapter
import dev.jahir.frames.ui.fragments.base.BaseBottomSheet

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
        val lm = GridLayoutManager(context, 3)

        recyclerView?.layoutManager = lm
        wallpaperDetailsAdapter.setLayoutManager(lm)
        recyclerView?.adapter = wallpaperDetailsAdapter

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