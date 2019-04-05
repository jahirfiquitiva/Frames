/*
 * Copyright (c) 2019. Jahir Fiquitiva
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

import android.annotation.TargetApi
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.transition.Transition
import android.view.View
import android.view.ViewTreeObserver
import android.widget.ImageView
import androidx.core.app.ActivityOptionsCompat
import androidx.core.app.SharedElementCallback
import androidx.core.util.Pair
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.integration.recyclerview.RecyclerViewPreloader
import com.bumptech.glide.util.ViewPreloadSizeProvider
import com.pluscubed.recyclerfastscroll.RecyclerFastScroller
import jahirfiquitiva.libs.frames.R
import jahirfiquitiva.libs.frames.data.models.Collection
import jahirfiquitiva.libs.frames.data.models.Wallpaper
import jahirfiquitiva.libs.frames.helpers.extensions.concatSharedElements
import jahirfiquitiva.libs.frames.helpers.extensions.configs
import jahirfiquitiva.libs.frames.helpers.extensions.framesPostponeEnterTransition
import jahirfiquitiva.libs.frames.helpers.extensions.jfilter
import jahirfiquitiva.libs.frames.helpers.extensions.maxPictureRes
import jahirfiquitiva.libs.frames.helpers.extensions.maxPreload
import jahirfiquitiva.libs.frames.helpers.extensions.safeStartPostponedEnterTransition
import jahirfiquitiva.libs.frames.helpers.utils.FL
import jahirfiquitiva.libs.frames.helpers.utils.MAX_WALLPAPERS_LOAD
import jahirfiquitiva.libs.frames.helpers.utils.TransitionCallback
import jahirfiquitiva.libs.frames.ui.activities.ViewerActivity
import jahirfiquitiva.libs.frames.ui.activities.base.BaseFramesActivity
import jahirfiquitiva.libs.frames.ui.activities.base.FavsDbManager
import jahirfiquitiva.libs.frames.ui.adapters.WallpapersAdapter
import jahirfiquitiva.libs.frames.ui.adapters.viewholders.FramesViewClickListener
import jahirfiquitiva.libs.frames.ui.adapters.viewholders.WallpaperHolder
import jahirfiquitiva.libs.frames.ui.widgets.EmptyViewRecyclerView
import jahirfiquitiva.libs.frames.ui.widgets.EndlessRecyclerViewScrollListener
import jahirfiquitiva.libs.frames.ui.widgets.FeaturedWallSpacingItemDecoration
import jahirfiquitiva.libs.frames.ui.widgets.WallpaperSharedElementCallback
import jahirfiquitiva.libs.kext.extensions.accentColor
import jahirfiquitiva.libs.kext.extensions.activity
import jahirfiquitiva.libs.kext.extensions.cardBackgroundColor
import jahirfiquitiva.libs.kext.extensions.context
import jahirfiquitiva.libs.kext.extensions.dimenPixelSize
import jahirfiquitiva.libs.kext.extensions.formatCorrectly
import jahirfiquitiva.libs.kext.extensions.hasContent
import jahirfiquitiva.libs.kext.extensions.isInHorizontalMode
import jahirfiquitiva.libs.kext.extensions.isLowRamDevice
import jahirfiquitiva.libs.kext.extensions.toBitmap
import jahirfiquitiva.libs.kext.ui.decorations.GridSpacingItemDecoration
import java.io.FileOutputStream

abstract class BaseWallpapersFragment : BaseFramesFragment<Wallpaper, WallpaperHolder>() {
    
    var hasChecker = false
    var recyclerView: EmptyViewRecyclerView? = null
        private set
    var fastScroller: RecyclerFastScroller? = null
        private set
    private var swipeToRefresh: SwipeRefreshLayout? = null
    
    private var rvLayoutManager: GridLayoutManager? = null
    private var currentWallPosition: Int = 0
    
    private val wallElementsCallback: WallpaperSharedElementCallback by lazy {
        WallpaperSharedElementCallback()
    }
    
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
                    (activity as? BaseFramesActivity<*>)?.showWallpaperOptionsDialog(item)
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
    private var spacingDecoration: GridSpacingItemDecoration = GridSpacingItemDecoration(0, 0)
    
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
                itemAnimator =
                    if (context.isLowRamDevice) null else DefaultItemAnimator()
                setHasFixedSize(true)
                
                activity {
                    addOnScrollListener(
                        RecyclerViewPreloader(it, wallsAdapter, provider, context.maxPreload))
                }
                
                setItemViewCacheSize((MAX_WALLPAPERS_LOAD * 1.5).toInt())
                adapter = wallsAdapter
            }
        }
        
        swipeToRefresh?.let { fastScroller?.attachSwipeRefreshLayout(it) }
        recyclerView?.let { fastScroller?.attachRecyclerView(it) }
    }
    
    override fun scrollToTop() {
        recyclerView?.post { recyclerView?.smoothScrollToPosition(0) }
    }
    
    override fun onResume() {
        super.onResume()
        configureRVColumns()
        canClick = true
    }
    
    fun configureRVColumns(force: Boolean = false) {
        context {
            if (configs.columns != spanCount || force) {
                recyclerView?.removeItemDecoration(spacingDecoration)
                val columns = configs.columns
                spanCount = if (it.isInHorizontalMode) (columns * 1.5).toInt() else columns
                rvLayoutManager =
                    GridLayoutManager(context, spanCount, RecyclerView.VERTICAL, false)
                val hasFeaturedWall = wallpapersModel?.getData().orEmpty().any { it.featured }
                if (!fromFavorites()) {
                    rvLayoutManager?.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                        override fun getSpanSize(position: Int): Int =
                            if (position == 0 && hasFeaturedWall) spanCount else 1
                    }
                }
                rvLayoutManager?.let {
                    recyclerView?.addOnScrollListener(
                        EndlessRecyclerViewScrollListener(it) { _, view ->
                            if (userVisibleHint) {
                                view.post { wallsAdapter.allowMoreItemsLoad() }
                            }
                        })
                }
                recyclerView?.layoutManager = rvLayoutManager
                spacingDecoration = if (hasFeaturedWall) {
                    FeaturedWallSpacingItemDecoration(
                        spanCount, it.dimenPixelSize(R.dimen.wallpapers_grid_spacing))
                } else {
                    GridSpacingItemDecoration(
                        spanCount, it.dimenPixelSize(R.dimen.wallpapers_grid_spacing))
                }
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
        wallsAdapter.updateFavorites(data)
        swipeToRefresh?.isRefreshing = false
    }
    
    override fun doOnWallpapersChange(data: ArrayList<Wallpaper>, fromCollectionActivity: Boolean) {
        super.doOnWallpapersChange(data, fromCollectionActivity)
        configureRVColumns(true)
        swipeToRefresh?.isRefreshing = false
    }
    
    override fun applyFilter(filter: String, closed: Boolean) {
        val list = ArrayList(
            if (fromFavorites())
                (activity as? FavsDbManager)?.getFavs() ?: wallpapersModel?.getData().orEmpty()
            else wallpapersModel?.getData().orEmpty())
        
        if (filter.hasContent()) {
            recyclerView?.setEmptyImage(R.drawable.no_results)
            recyclerView?.setEmptyText(R.string.search_no_results)
            wallsAdapter.setItems(list.jfilter { filteredWallpaper(it, filter) })
        } else {
            recyclerView?.setEmptyImage(
                if (fromFavorites()) R.drawable.no_favorites else R.drawable.empty_section)
            recyclerView?.setEmptyText(
                if (fromFavorites()) R.string.no_favorites else R.string.empty_section)
            wallsAdapter.setItems(list)
        }
        if (!closed)
            scrollToTop()
    }
    
    private fun filteredWallpaper(wallpaper: Wallpaper, filter: String): Boolean {
        return if (configs.deepSearchEnabled) {
            wallpaper.name.contains(filter, true) ||
                wallpaper.author.contains(filter, true) ||
                (!fromCollectionActivity() &&
                    wallpaper.collections.formatCorrectly().replace("_", " ")
                        .contains(filter, true))
        } else {
            wallpaper.name.contains(filter, true)
        }
    }
    
    private var canClick = true
    
    private fun onWallpaperClicked(wallpaper: Wallpaper, holder: WallpaperHolder) {
        if (!canClick) return
        try {
            val intent = Intent(activity, ViewerActivity::class.java)
            var options: ActivityOptionsCompat? = null
            currentWallPosition = holder.adapterPosition
            
            with(intent) {
                putParcelableArrayListExtra(
                    "wallpapers", ArrayList(wallpapersModel?.getData().orEmpty()))
                putExtra(ViewerActivity.CURRENT_WALL_POSITION, holder.adapterPosition)
                
                putExtra("wallpaper", wallpaper)
                putExtra(
                    "inFavorites", (activity as? FavsDbManager)?.isInFavs(wallpaper) ?: false)
                putExtra("showFavoritesButton", showFavoritesIcon())
                putExtra("checker", hasChecker)
                
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
                    val imgTransition = holder.img?.let { ViewCompat.getTransitionName(it) }
                        ?: "img_transition_${holder.adapterPosition}"
                    val nameTransition = holder.name?.let { ViewCompat.getTransitionName(it) }
                        ?: "name_transition_${holder.adapterPosition}"
                    val authorTransition =
                        holder.author?.let { ViewCompat.getTransitionName(it) }
                            ?: "author_transition_${holder.adapterPosition}"
                    val heartTransition =
                        holder.heartIcon?.let { ViewCompat.getTransitionName(it) }
                            ?: "fav_transition_${holder.adapterPosition}"
                    
                    val imgPair = Pair<View, String>(holder.img, imgTransition)
                    val namePair = Pair<View, String>(holder.name, nameTransition)
                    val authorPair = Pair<View, String>(holder.author, authorTransition)
                    val heartPair = Pair<View, String>(holder.heartIcon, heartTransition)
                    
                    val sharedElements =
                        activity?.concatSharedElements(imgPair, namePair, authorPair, heartPair)
                            ?: arrayOfNulls(0)
                    
                    options = activity?.let {
                        ActivityOptionsCompat.makeSceneTransitionAnimation(it, *sharedElements)
                    }
                }
            }
            
            var fos: FileOutputStream? = null
            try {
                val filename = "thumb.png"
                fos = activity?.openFileOutput(filename, Context.MODE_PRIVATE)
                holder.img?.drawable?.toBitmap()
                    ?.compress(Bitmap.CompressFormat.JPEG, context?.maxPictureRes ?: 25, fos)
                intent.putExtra("image", filename)
            } catch (ignored: Exception) {
            } finally {
                fos?.flush()
                fos?.close()
            }
            
            try {
                startActivityForResult(intent, 10, options?.toBundle())
            } catch (e: Exception) {
                FL.e("Error", e)
                startActivityForResult(intent, 10)
            }
        } catch (e: Exception) {
            FL.e("Error", e)
            canClick = true
        }
    }
    
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == 10) {
            onActivityReenter(resultCode, data)
            val hasModifiedFavs = data?.getBooleanExtra("modified", false) ?: false
            if (hasModifiedFavs) (activity as? FavsDbManager)?.reloadFavorites()
        }
    }
    
    fun onActivityReenter(resultCode: Int, data: Intent?) {
        if (resultCode != 10) return
        
        val position = data?.getIntExtra(ViewerActivity.CURRENT_WALL_POSITION, currentWallPosition)
            ?: currentWallPosition
        if (position != RecyclerView.NO_POSITION) currentWallPosition = position
        
        val scrollToPosition: () -> Unit = { recyclerView?.scrollToPosition(currentWallPosition) }
        scrollToPosition()
        
        activity?.setExitSharedElementCallback(wallElementsCallback)
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            activity?.window?.sharedElementExitTransition?.addListener(object : TransitionCallback() {
                override fun onTransitionEnd(transition: Transition?) = removeCallback()
                override fun onTransitionCancel(transition: Transition?) = removeCallback()
                @TargetApi(Build.VERSION_CODES.LOLLIPOP)
                private fun removeCallback() {
                    activity?.window?.sharedElementExitTransition?.removeListener(this)
                    activity?.setExitSharedElementCallback(null as SharedElementCallback?)
                }
            })
        }
        
        activity?.framesPostponeEnterTransition(scrollToPosition, scrollToPosition)
        recyclerView?.viewTreeObserver
            ?.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
                override fun onPreDraw(): Boolean {
                    recyclerView?.viewTreeObserver?.removeOnPreDrawListener(this)
                    val holder =
                        recyclerView?.findViewHolderForAdapterPosition(
                            position) as? WallpaperHolder?
                    holder?.let {
                        wallElementsCallback.setSharedElementViews(
                            it.img, it.name, it.author, it.heartIcon)
                    }
                    activity?.safeStartPostponedEnterTransition()
                    return true
                }
            })
    }
    
    abstract fun showFavoritesIcon(): Boolean
    
    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if (isVisibleToUser && !allowReloadAfterVisibleToUser()) recyclerView?.updateEmptyState()
    }
    
    override fun autoStartLoad(): Boolean = true
    
    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt("current_wall", currentWallPosition)
        super.onSaveInstanceState(outState)
    }
    
    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        super.onRestoreInstanceState(savedInstanceState)
        currentWallPosition =
            savedInstanceState?.getInt("current_wall", currentWallPosition) ?: currentWallPosition
    }
}
