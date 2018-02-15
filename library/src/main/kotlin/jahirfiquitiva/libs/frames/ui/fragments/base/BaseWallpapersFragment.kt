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
import jahirfiquitiva.libs.kauextensions.ui.decorations.GridSpacingItemDecoration
import java.io.FileOutputStream

@Suppress("DEPRECATION")
abstract class BaseWallpapersFragment : BaseFramesFragment<Wallpaper, WallpaperHolder>() {
    
    var hasChecker = false
    var recyclerView: EmptyViewRecyclerView? = null
        private set
    var fastScroller: RecyclerFastScroller? = null
        private set
    private var swipeToRefresh: SwipeRefreshLayout? = null
    
    private val provider: ViewPreloadSizeProvider<Wallpaper> by lazy {
        ViewPreloadSizeProvider<Wallpaper>()
    }
    
    val wallsAdapter: WallpapersAdapter by lazy {
        WallpapersAdapter(
                context?.let { Glide.with(it) },
                provider, fromFavorites(), showFavoritesIcon(),
                object : FramesViewClickListener<Wallpaper, WallpaperHolder>() {
                    override fun onSingleClick(item: Wallpaper, holder: WallpaperHolder) {
                        onItemClicked(item, holder)
                    }
                    
                    override fun onLongClick(item: Wallpaper) {
                        super.onLongClick(item)
                        (activity as? BaseFramesActivity)?.showWallpaperOptionsDialog(item)
                    }
                    
                    override fun onHeartClick(
                            view: ImageView,
                            item: Wallpaper,
                            color: Int
                                             ) {
                        super.onHeartClick(view, item, color)
                        onHeartClicked(view, item, color)
                    }
                })
    }
    
    private var spanCount = 0
    private var spacingDecoration: GridSpacingItemDecoration? = null
    
    override fun initUI(content: View) {
        swipeToRefresh = content.findViewById(R.id.swipe_to_refresh)
        recyclerView = content.findViewById(R.id.list_rv)
        fastScroller = content.findViewById(R.id.fast_scroller)
        
        swipeToRefresh?.let {
            it.setProgressBackgroundColorSchemeColor(it.context.cardBackgroundColor)
            it.setColorSchemeColors(it.context.accentColor)
            it.setOnRefreshListener { reloadData(if (fromFavorites()) 2 else 1) }
        }
        
        recyclerView?.let { recyclerView ->
            with(recyclerView) {
                textView = content.findViewById(R.id.empty_text)
                emptyView = content.findViewById(R.id.empty_view)
                setEmptyImage(
                        if (fromFavorites()) R.drawable.no_favorites else R.drawable.empty_section)
                setEmptyText(if (fromFavorites()) R.string.no_favorites else R.string.empty_section)
                loadingView = content.findViewById(R.id.loading_view)
                setLoadingText(R.string.loading_section)
                configureRVColumns()
                itemAnimator = if (context.isLowRamDevice) null else DefaultItemAnimator()
                setHasFixedSize(true)
                
                actv {
                    val preloader: RecyclerViewPreloader<Wallpaper> =
                            RecyclerViewPreloader(it, wallsAdapter, provider, context.maxPreload)
                    addOnScrollListener(preloader)
                }
                
                addOnScrollListener(
                        object : RecyclerView.OnScrollListener() {
                            override fun onScrolled(rv: RecyclerView?, dx: Int, dy: Int) {
                                super.onScrolled(rv, dx, dy)
                                if (!recyclerView.canScrollVertically(1)) {
                                    recyclerView.post { wallsAdapter.allowMoreItemsLoad() }
                                }
                            }
                        })
                
                setItemViewCacheSize(MAX_WALLPAPERS_LOAD)
                isDrawingCacheEnabled = true
                drawingCacheQuality = View.DRAWING_CACHE_QUALITY_AUTO
                
                adapter = wallsAdapter
            }
        }
        
        fastScroller?.attachSwipeRefreshLayout(swipeToRefresh)
        recyclerView?.let { fastScroller?.attachRecyclerView(it) }
        
        recyclerView?.state = EmptyViewRecyclerView.State.LOADING
    }
    
    override fun scrollToTop() {
        recyclerView?.post { recyclerView?.scrollToPosition(0) }
    }
    
    override fun onResume() {
        super.onResume()
        configureRVColumns()
        canClick = true
    }
    
    fun configureRVColumns() {
        ctxt {
            if (it.framesKonfigs.columns != spanCount) {
                recyclerView?.removeItemDecoration(spacingDecoration)
                val columns = ctxt.framesKonfigs.columns
                spanCount = if (ctxt.isInHorizontalMode) ((columns * 1.5).toInt()) else columns
                recyclerView?.layoutManager = GridLayoutManager(
                        context, spanCount,
                        GridLayoutManager.VERTICAL, false)
                spacingDecoration = GridSpacingItemDecoration(
                        spanCount, ctxt.dimenPixelSize(R.dimen.wallpapers_grid_spacing))
                recyclerView?.addItemDecoration(spacingDecoration)
            }
        }
    }
    
    override fun getContentLayout(): Int = R.layout.section_with_swipe_refresh
    
    override fun onItemClicked(item: Wallpaper, holder: WallpaperHolder) =
            onWallpaperClicked(item, holder)
    
    override fun loadDataFromViewModel() {
        recyclerView?.state = EmptyViewRecyclerView.State.LOADING
        super.loadDataFromViewModel()
    }
    
    override fun enableRefresh(enable: Boolean) {
        swipeToRefresh?.isEnabled = enable
    }
    
    override fun reloadData(section: Int) {
        val isRefreshing = swipeToRefresh?.isRefreshing ?: false
        if (isRefreshing) swipeToRefresh?.isRefreshing = false
        recyclerView?.state = EmptyViewRecyclerView.State.LOADING
        super.reloadData(section)
        swipeToRefresh?.isRefreshing = true
    }
    
    override fun doOnCollectionsChange(data: ArrayList<Collection>) {
        super.doOnCollectionsChange(data)
        swipeToRefresh?.isRefreshing = false
    }
    
    override fun doOnFavoritesChange(data: ArrayList<Wallpaper>) {
        super.doOnFavoritesChange(data)
        swipeToRefresh?.isRefreshing = false
    }
    
    override fun doOnWallpapersChange(data: ArrayList<Wallpaper>, fromCollectionActivity: Boolean) {
        super.doOnWallpapersChange(data, fromCollectionActivity)
        swipeToRefresh?.isRefreshing = false
    }
    
    override fun applyFilter(filter: String) {
        val list = ArrayList(
                if (fromFavorites()) favoritesModel?.getData().orEmpty()
                else wallpapersModel?.getData().orEmpty())
        
        if (filter.hasContent()) {
            recyclerView?.setEmptyImage(R.drawable.no_results)
            recyclerView?.setEmptyText(R.string.search_no_results)
            wallsAdapter.setItems(ArrayList(list.filter { filteredWallpaper(it, filter) }))
        } else {
            recyclerView?.setEmptyImage(
                    if (fromFavorites()) R.drawable.no_favorites else R.drawable.empty_section)
            recyclerView?.setEmptyText(
                    if (fromFavorites()) R.string.no_favorites else R.string.empty_section)
            wallsAdapter.setItems(list)
        }
        scrollToTop()
    }
    
    private fun filteredWallpaper(wallpaper: Wallpaper, filter: String): Boolean {
        var toReturn = false
        ctxt {
            toReturn = if (it.framesKonfigs.deepSearchEnabled) {
                wallpaper.name.contains(filter, true) ||
                        wallpaper.author.contains(filter, true) ||
                        (!fromCollectionActivity() &&
                                wallpaper.collections.formatCorrectly().replace("_", " ")
                                        .contains(filter, true))
            } else {
                wallpaper.name.contains(filter, true)
            }
        }
        return toReturn
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
                putExtra("checker", hasChecker)
            }
            
            var fos: FileOutputStream? = null
            try {
                val filename = "thumb.png"
                fos = activity?.openFileOutput(filename, Context.MODE_PRIVATE)
                holder.img?.drawable?.toBitmap()?.compress(
                        Bitmap.CompressFormat.JPEG, ctxt.maxPictureRes, fos)
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
                    if (hasModifiedFavs) {
                        if (inFavs) addToFavorites(it)
                        else removeFromFavorites(it)
                    }
                }
            }
        }
    }
    
    abstract fun showFavoritesIcon(): Boolean
    
    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if (isVisibleToUser && !allowReloadAfterVisibleToUser()) recyclerView?.updateEmptyState()
    }
}