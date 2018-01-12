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
package jahirfiquitiva.libs.frames.ui.fragments.base

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.support.v4.app.ActivityOptionsCompat
import android.support.v4.util.Pair
import android.support.v4.view.ViewCompat
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import ca.allanwang.kau.utils.dimenPixelSize
import ca.allanwang.kau.utils.toBitmap
import com.bumptech.glide.Glide
import com.bumptech.glide.integration.recyclerview.RecyclerViewPreloader
import com.bumptech.glide.util.ViewPreloadSizeProvider
import com.pluscubed.recyclerfastscroll.RecyclerFastScroller
import jahirfiquitiva.libs.frames.R
import jahirfiquitiva.libs.frames.data.models.Collection
import jahirfiquitiva.libs.frames.data.models.Wallpaper
import jahirfiquitiva.libs.frames.helpers.extensions.framesKonfigs
import jahirfiquitiva.libs.frames.helpers.extensions.maxPictureRes
import jahirfiquitiva.libs.frames.helpers.extensions.maxPreload
import jahirfiquitiva.libs.frames.helpers.utils.MAX_WALLPAPERS_LOAD
import jahirfiquitiva.libs.frames.ui.activities.ViewerActivity
import jahirfiquitiva.libs.frames.ui.activities.base.BaseFramesActivity
import jahirfiquitiva.libs.frames.ui.adapters.WallpapersAdapter
import jahirfiquitiva.libs.frames.ui.adapters.viewholders.FramesViewClickListener
import jahirfiquitiva.libs.frames.ui.adapters.viewholders.WallpaperHolder
import jahirfiquitiva.libs.frames.ui.widgets.EmptyViewRecyclerView
import jahirfiquitiva.libs.kauextensions.extensions.accentColor
import jahirfiquitiva.libs.kauextensions.extensions.actv
import jahirfiquitiva.libs.kauextensions.extensions.cardBackgroundColor
import jahirfiquitiva.libs.kauextensions.extensions.ctxt
import jahirfiquitiva.libs.kauextensions.extensions.formatCorrectly
import jahirfiquitiva.libs.kauextensions.extensions.hasContent
import jahirfiquitiva.libs.kauextensions.extensions.isInHorizontalMode
import jahirfiquitiva.libs.kauextensions.extensions.isLowRamDevice
import jahirfiquitiva.libs.kauextensions.extensions.safeActv
import jahirfiquitiva.libs.kauextensions.ui.decorations.GridSpacingItemDecoration
import java.io.FileOutputStream

@Suppress("DEPRECATION")
abstract class BaseWallpapersFragment : BaseFramesFragment<Wallpaper, WallpaperHolder>() {
    
    lateinit var swipeToRefresh: SwipeRefreshLayout
    lateinit var rv: EmptyViewRecyclerView
    lateinit var fastScroll: RecyclerFastScroller
    
    var wallsAdapter: WallpapersAdapter? = null
        private set
    private var spanCount = 0
    private var spacingDecoration: GridSpacingItemDecoration? = null
    
    override fun initUI(content: View) {
        swipeToRefresh = content.findViewById(R.id.swipe_to_refresh)
        rv = content.findViewById(R.id.list_rv)
        fastScroll = content.findViewById(R.id.fast_scroller)
        
        with(swipeToRefresh) {
            setProgressBackgroundColorSchemeColor(context.cardBackgroundColor)
            setColorSchemeColors(context.accentColor)
            setOnRefreshListener { reloadData(if (fromFavorites()) 2 else 1) }
        }
        
        with(rv) {
            itemAnimator = if (context.isLowRamDevice) null else DefaultItemAnimator()
            textView = content.findViewById(R.id.empty_text)
            emptyView = content.findViewById(R.id.empty_view)
            setEmptyImage(
                    if (fromFavorites()) R.drawable.no_favorites else R.drawable.empty_section)
            setEmptyText(if (fromFavorites()) R.string.no_favorites else R.string.empty_section)
            loadingView = content.findViewById(R.id.loading_view)
            setLoadingText(R.string.loading_section)
            configureRVColumns()
            
            val provider = ViewPreloadSizeProvider<Wallpaper>()
            wallsAdapter = WallpapersAdapter(
                    Glide.with(context), provider, fromFavorites(), showFavoritesIcon(),
                    object : FramesViewClickListener<Wallpaper, WallpaperHolder>() {
                        override fun onSingleClick(item: Wallpaper, holder: WallpaperHolder) {
                            onItemClicked(item, holder)
                        }
                        
                        override fun onLongClick(item: Wallpaper) {
                            super.onLongClick(item)
                            (activity as? BaseFramesActivity)?.showWallpaperOptionsDialog(item)
                        }
                        
                        override fun onHeartClick(view: ImageView, item: Wallpaper, color: Int) {
                            super.onHeartClick(view, item, color)
                            onHeartClicked(view, item, color)
                        }
                    })
            
            safeActv { activity ->
                wallsAdapter?.let {
                    val preloader: RecyclerViewPreloader<Wallpaper> =
                            RecyclerViewPreloader(activity, it, provider, context.maxPreload)
                    addOnScrollListener(preloader)
                }
            }
            
            addOnScrollListener(
                    object : RecyclerView.OnScrollListener() {
                        override fun onScrolled(rv: RecyclerView?, dx: Int, dy: Int) {
                            super.onScrolled(rv, dx, dy)
                            rv?.let {
                                if (!it.canScrollVertically(1)) {
                                    it.post({ wallsAdapter?.allowMoreItemsLoad() })
                                }
                            }
                        }
                    })
            
            setItemViewCacheSize(MAX_WALLPAPERS_LOAD)
            isDrawingCacheEnabled = true
            drawingCacheQuality = View.DRAWING_CACHE_QUALITY_AUTO
            
            adapter = wallsAdapter
        }
        
        with(fastScroll) {
            attachSwipeRefreshLayout(swipeToRefresh)
            attachRecyclerView(rv)
        }
        
        rv.state = EmptyViewRecyclerView.State.LOADING
    }
    
    override fun scrollToTop() {
        rv.post { rv.scrollToPosition(0) }
    }
    
    override fun onResume() {
        super.onResume()
        configureRVColumns()
        canClick = true
    }
    
    fun configureRVColumns() {
        if (ctxt.framesKonfigs.columns != spanCount) {
            rv.removeItemDecoration(spacingDecoration)
            val columns = ctxt.framesKonfigs.columns
            spanCount = if (ctxt.isInHorizontalMode) ((columns * 1.5).toInt()) else columns
            rv.layoutManager = GridLayoutManager(
                    context, spanCount,
                    GridLayoutManager.VERTICAL, false)
            spacingDecoration = GridSpacingItemDecoration(
                    spanCount, ctxt.dimenPixelSize(R.dimen.wallpapers_grid_spacing))
            rv.addItemDecoration(spacingDecoration)
        }
    }
    
    override fun getContentLayout(): Int = R.layout.section_lists
    
    override fun onItemClicked(item: Wallpaper, holder: WallpaperHolder) =
            onWallpaperClicked(item, holder)
    
    override fun loadDataFromViewModel() {
        rv.state = EmptyViewRecyclerView.State.LOADING
        super.loadDataFromViewModel()
    }
    
    override fun enableRefresh(enable: Boolean) {
        swipeToRefresh.isEnabled = enable
    }
    
    override fun reloadData(section: Int) {
        if (swipeToRefresh.isRefreshing) swipeToRefresh.isRefreshing = false
        rv.state = EmptyViewRecyclerView.State.LOADING
        super.reloadData(section)
        swipeToRefresh.isRefreshing = true
    }
    
    override fun doOnCollectionsChange(data: ArrayList<Collection>) {
        super.doOnCollectionsChange(data)
        swipeToRefresh.isRefreshing = false
    }
    
    override fun doOnFavoritesChange(data: ArrayList<Wallpaper>) {
        super.doOnFavoritesChange(data)
        swipeToRefresh.isRefreshing = false
    }
    
    override fun doOnWallpapersChange(data: ArrayList<Wallpaper>, fromCollectionActivity: Boolean) {
        super.doOnWallpapersChange(data, fromCollectionActivity)
        swipeToRefresh.isRefreshing = false
    }
    
    override fun applyFilter(filter: String) {
        wallsAdapter?.let {
            val list = ArrayList(
                    (if (fromFavorites()) favoritesModel?.getData() else wallpapersModel?.getData()))
            
            if (filter.hasContent()) {
                rv.setEmptyImage(R.drawable.no_results)
                rv.setEmptyText(R.string.search_no_results)
                it.setItems(ArrayList(list.filter { filteredWallpaper(it, filter) }))
            } else {
                rv.setEmptyImage(
                        if (fromFavorites()) R.drawable.no_favorites else R.drawable.empty_section)
                rv.setEmptyText(
                        if (fromFavorites()) R.string.no_favorites else R.string.empty_section)
                it.setItems(list)
            }
            scrollToTop()
        }
    }
    
    private fun filteredWallpaper(it: Wallpaper, filter: String): Boolean {
        return if (ctxt.framesKonfigs.deepSearchEnabled) {
            it.name.contains(filter, true) || it.author.contains(filter, true) ||
                    (!fromCollectionActivity() &&
                            it.collections.formatCorrectly().replace("_", " ").contains(
                                    filter,
                                    true))
        } else {
            it.name.contains(filter, true)
        }
    }
    
    private var canClick = true
    
    private fun onWallpaperClicked(wallpaper: Wallpaper, holder: WallpaperHolder) {
        if (!canClick) return
        try {
            val intent = Intent(activity, ViewerActivity::class.java)
            val imgTransition = ViewCompat.getTransitionName(holder.img)
            val nameTransition = ViewCompat.getTransitionName(holder.name)
            val authorTransition = ViewCompat.getTransitionName(holder.author)
            val heartTransition = ViewCompat.getTransitionName(holder.heartIcon)
            
            with(intent) {
                putExtra("wallpaper", wallpaper)
                putExtra("inFavorites", favoritesModel?.isInFavorites(wallpaper) == true)
                putExtra("showFavoritesButton", showFavoritesIcon())
                putExtra("imgTransition", imgTransition)
                putExtra("nameTransition", nameTransition)
                putExtra("authorTransition", authorTransition)
                putExtra("favTransition", heartTransition)
            }
            
            var fos: FileOutputStream? = null
            try {
                val filename = "thumb.png"
                fos = actv.openFileOutput(filename, Context.MODE_PRIVATE)
                holder.img.drawable.toBitmap().compress(
                        Bitmap.CompressFormat.JPEG,
                        ctxt.maxPictureRes, fos)
                intent.putExtra("image", filename)
            } catch (ignored: Exception) {
            } finally {
                fos?.flush()
                fos?.close()
            }
            
            val imgPair = Pair<View, String>(holder.img, imgTransition)
            val namePair = Pair<View, String>(holder.name, nameTransition)
            val authorPair = Pair<View, String>(holder.author, authorTransition)
            val heartPair = Pair<View, String>(holder.heartIcon, heartTransition)
            val options =
                    ActivityOptionsCompat.makeSceneTransitionAnimation(
                            actv, imgPair, namePair,
                            authorPair, heartPair)
            
            try {
                startActivityForResult(intent, 10, options.toBundle())
            } catch (ignored: Exception) {
                startActivityForResult(intent, 10)
            }
        } catch (ignored: Exception) {
            canClick = true
        }
    }
    
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == 10) {
            data?.let {
                val item = it.getParcelableExtra<Wallpaper>("item")
                val hasModifiedFavs = it.getBooleanExtra("modified", false)
                val inFavs = it.getBooleanExtra("inFavorites", false)
                item?.let {
                    wallpapersModel?.updateWallpaper(it)
                    if (hasModifiedFavs) {
                        if (inFavs) addToFavorites(it)
                        else removeFromFavorites(it)
                    }
                }
            }
        }
    }
    
    abstract fun showFavoritesIcon(): Boolean
}