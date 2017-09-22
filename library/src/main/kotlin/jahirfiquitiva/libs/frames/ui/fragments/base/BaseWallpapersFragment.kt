/*
 * Copyright (c) 2017. Jahir Fiquitiva
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
import android.view.View
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
import jahirfiquitiva.libs.frames.helpers.extensions.isLowRamDevice
import jahirfiquitiva.libs.frames.helpers.extensions.maxPictureRes
import jahirfiquitiva.libs.frames.helpers.extensions.maxPreload
import jahirfiquitiva.libs.frames.ui.activities.ViewerActivity
import jahirfiquitiva.libs.frames.ui.activities.base.BaseFramesActivity
import jahirfiquitiva.libs.frames.ui.adapters.WallpapersAdapter
import jahirfiquitiva.libs.frames.ui.adapters.viewholders.WallpaperHolder
import jahirfiquitiva.libs.frames.ui.widgets.EmptyViewRecyclerView
import jahirfiquitiva.libs.kauextensions.extensions.accentColor
import jahirfiquitiva.libs.kauextensions.extensions.cardBackgroundColor
import jahirfiquitiva.libs.kauextensions.extensions.formatCorrectly
import jahirfiquitiva.libs.kauextensions.extensions.hasContent
import jahirfiquitiva.libs.kauextensions.extensions.isInHorizontalMode
import jahirfiquitiva.libs.kauextensions.ui.decorations.GridSpacingItemDecoration
import java.io.FileOutputStream

abstract class BaseWallpapersFragment:BaseFramesFragment<Wallpaper, WallpaperHolder>() {
    
    lateinit var swipeToRefresh:SwipeRefreshLayout
    lateinit var rv:EmptyViewRecyclerView
    lateinit var fastScroll:RecyclerFastScroller
    
    internal var wallsAdapter:WallpapersAdapter? = null
    private var spanCount = 0
    private var spacingDecoration:GridSpacingItemDecoration? = null
    
    override fun initUI(content:View) {
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
                    Glide.with(context), provider,
                    { wall, holder -> onItemClicked(wall, holder) },
                    { wall ->
                        if (activity is BaseFramesActivity)
                            (activity as BaseFramesActivity).showWallpaperOptionsDialog(wall)
                    },
                    { heart, wall, color ->
                        onHeartClicked(heart, wall, color)
                    },
                    fromFavorites(), showFavoritesIcon())
            
            val preloader:RecyclerViewPreloader<Wallpaper> =
                    RecyclerViewPreloader(activity, wallsAdapter, provider, context.maxPreload)
            addOnScrollListener(preloader)
            adapter = wallsAdapter
        }
        
        with(fastScroll) {
            attachSwipeRefreshLayout(swipeToRefresh)
            attachRecyclerView(rv)
        }
        
        rv.state = EmptyViewRecyclerView.State.LOADING
    }
    
    override fun scrollToTop() {
        rv.layoutManager.scrollToPosition(0)
    }
    
    override fun onResume() {
        super.onResume()
        configureRVColumns()
        canClick = true
    }
    
    fun configureRVColumns() {
        if (context.framesKonfigs.columns != spanCount) {
            rv.removeItemDecoration(spacingDecoration)
            spanCount = context.framesKonfigs.columns
            rv.layoutManager = GridLayoutManager(context,
                                                 if (context.isInHorizontalMode) ((spanCount * 1.5).toInt()) else spanCount,
                                                 GridLayoutManager.VERTICAL, false)
            spacingDecoration = GridSpacingItemDecoration(spanCount,
                                                          context.dimenPixelSize(
                                                                  R.dimen.wallpapers_grid_spacing))
            rv.addItemDecoration(spacingDecoration)
        }
    }
    
    override fun getContentLayout():Int = R.layout.section_lists
    
    override fun onItemClicked(item:Wallpaper, holder:WallpaperHolder) =
            onWallpaperClicked(item, holder)
    
    override fun loadDataFromViewModel() {
        rv.state = EmptyViewRecyclerView.State.LOADING
        super.loadDataFromViewModel()
    }
    
    override fun reloadData(section:Int) {
        if (swipeToRefresh.isRefreshing) swipeToRefresh.isRefreshing = false
        rv.state = EmptyViewRecyclerView.State.LOADING
        super.reloadData(section)
        swipeToRefresh.isRefreshing = true
    }
    
    override fun doOnCollectionsChange(data:ArrayList<Collection>) {
        super.doOnCollectionsChange(data)
        swipeToRefresh.isRefreshing = false
    }
    
    override fun doOnFavoritesChange(data:ArrayList<Wallpaper>) {
        super.doOnFavoritesChange(data)
        swipeToRefresh.isRefreshing = false
    }
    
    override fun doOnWallpapersChange(data:ArrayList<Wallpaper>, fromCollectionActivity:Boolean) {
        super.doOnWallpapersChange(data, fromCollectionActivity)
        swipeToRefresh.isRefreshing = false
    }
    
    override fun applyFilter(filter:String) {
        wallsAdapter?.let {
            val list = (if (fromFavorites()) favoritesModel?.getData() else wallpapersModel?.getData()) ?: return
            if (filter.hasContent()) {
                rv.setEmptyImage(R.drawable.no_results)
                rv.setEmptyText(R.string.search_no_results)
                it.updateItems(
                        ArrayList(list.filter { filteredWallpaper(it, filter) }), true)
            } else {
                rv.setEmptyImage(
                        if (fromFavorites()) R.drawable.no_favorites else R.drawable.empty_section)
                rv.setEmptyText(
                        if (fromFavorites()) R.string.no_favorites else R.string.empty_section)
                it.updateItems(ArrayList(list), true)
                scrollToTop()
            }
        }
    }
    
    private fun filteredWallpaper(it:Wallpaper, filter:String):Boolean {
        return if (context.framesKonfigs.deepSearchEnabled) {
            it.name.contains(filter, true) || it.author.contains(filter, true) ||
                    (!fromCollectionActivity() &&
                            it.collections.formatCorrectly().replace("_", " ").contains(filter,
                                                                                        true))
        } else {
            it.name.contains(filter, true)
        }
    }
    
    private var canClick = true
    
    private fun onWallpaperClicked(wallpaper:Wallpaper, holder:WallpaperHolder) {
        if (!canClick) return
        try {
            val intent = Intent(activity, ViewerActivity::class.java)
            val imgTransition = ViewCompat.getTransitionName(holder.img)
            val nameTransition = ViewCompat.getTransitionName(holder.name)
            val authorTransition = ViewCompat.getTransitionName(holder.author)
            val heartTransition = ViewCompat.getTransitionName(holder.heartIcon)
            
            with(intent) {
                putExtra("wallpaper", wallpaper)
                putExtra("inFavorites", favoritesModel?.isInFavorites(wallpaper) ?: false)
                putExtra("showFavoritesButton", showFavoritesIcon())
                putExtra("imgTransition", imgTransition)
                putExtra("nameTransition", nameTransition)
                putExtra("authorTransition", authorTransition)
                putExtra("favTransition", heartTransition)
            }
            
            var fos:FileOutputStream? = null
            try {
                val filename = "thumb.png"
                fos = activity.openFileOutput(filename, Context.MODE_PRIVATE)
                holder.img.drawable.toBitmap().compress(Bitmap.CompressFormat.JPEG,
                                                        context.maxPictureRes, fos)
                intent.putExtra("image", filename)
            } catch (ignored:Exception) {
            } finally {
                fos?.flush()
                fos?.close()
            }
            
            val imgPair = Pair<View, String>(holder.img, imgTransition)
            val namePair = Pair<View, String>(holder.name, nameTransition)
            val authorPair = Pair<View, String>(holder.author, authorTransition)
            val heartPair = Pair<View, String>(holder.heartIcon, heartTransition)
            val options =
                    ActivityOptionsCompat.makeSceneTransitionAnimation(activity, imgPair, namePair,
                                                                       authorPair, heartPair)
            
            try {
                startActivityForResult(intent, 10, options.toBundle())
            } catch (ignored:Exception) {
                startActivityForResult(intent, 10)
            }
        } catch (ignored:Exception) {
            canClick = true
        }
    }
    
    override fun onActivityResult(requestCode:Int, resultCode:Int, data:Intent?) {
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
    
    abstract fun showFavoritesIcon():Boolean
}