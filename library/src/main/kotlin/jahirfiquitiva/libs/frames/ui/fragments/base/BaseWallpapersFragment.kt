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
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.GridLayoutManager
import android.view.View
import ca.allanwang.kau.utils.dimenPixelSize
import com.pluscubed.recyclerfastscroll.RecyclerFastScroller
import jahirfiquitiva.libs.frames.R
import jahirfiquitiva.libs.frames.data.models.Wallpaper
import jahirfiquitiva.libs.frames.helpers.configs.maxPictureRes
import jahirfiquitiva.libs.frames.helpers.extensions.framesKonfigs
import jahirfiquitiva.libs.frames.ui.activities.ViewerActivity
import jahirfiquitiva.libs.frames.ui.adapters.WallpapersAdapter
import jahirfiquitiva.libs.frames.ui.adapters.viewholders.WallpaperHolder
import jahirfiquitiva.libs.frames.ui.widgets.EmptyViewRecyclerView
import jahirfiquitiva.libs.kauextensions.extensions.hasContent
import jahirfiquitiva.libs.kauextensions.extensions.isInHorizontalMode
import jahirfiquitiva.libs.kauextensions.extensions.printInfo
import jahirfiquitiva.libs.kauextensions.ui.decorations.GridSpacingItemDecoration

abstract class BaseWallpapersFragment:BaseFramesFragment<Wallpaper, WallpaperHolder>() {
    
    lateinit var rv:EmptyViewRecyclerView
    lateinit var adapter:WallpapersAdapter
    lateinit var fastScroll:RecyclerFastScroller
    
    private var spanCount = 0
    private var spacingDecoration:GridSpacingItemDecoration? = null
    
    override fun initUI(content:View) {
        rv = content.findViewById(R.id.list_rv)
        rv.itemAnimator = DefaultItemAnimator()
        rv.textView = content.findViewById(R.id.empty_text)
        rv.emptyView = content.findViewById(R.id.empty_view)
        rv.setEmptyImage(if (fromFavorites()) R.drawable.no_favorites else R.drawable.empty_section)
        rv.setEmptyText(if (fromFavorites()) R.string.no_favorites else R.string.empty_section)
        rv.loadingView = content.findViewById(R.id.loading_view)
        rv.setLoadingText(R.string.loading_section)
        configureRVColumns()
        adapter = WallpapersAdapter(
                { wall, holder -> onItemClicked(wall, holder) },
                // TODO: Implement long-click listener
                { wall, _ -> context.printInfo("Long pressed $wall") },
                { heart, wall -> onHeartClicked(heart, wall) },
                fromFavorites(), showFavoritesIcon())
        rv.adapter = adapter
        fastScroll = content.findViewById(R.id.fast_scroller)
        fastScroll.attachRecyclerView(rv)
        rv.state = EmptyViewRecyclerView.State.LOADING
    }
    
    override fun onResume() {
        super.onResume()
        configureRVColumns()
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
        rv.state = EmptyViewRecyclerView.State.LOADING
        super.reloadData(section)
    }
    
    override fun applyFilter(filter:String) {
        val list = if (fromFavorites()) favoritesModel.items.value else wallpapersModel.items.value
        if (filter.hasContent()) {
            rv.setEmptyImage(R.drawable.no_results)
            rv.setEmptyText(R.string.search_no_results)
            list?.let {
                adapter.setItems(ArrayList<Wallpaper>(it.filter { it.name.contains(filter, true) }))
            }
        } else {
            rv.setEmptyImage(
                    if (fromFavorites()) R.drawable.no_favorites else R.drawable.empty_section)
            rv.setEmptyText(if (fromFavorites()) R.string.no_favorites else R.string.empty_section)
            list?.let { adapter.setItems(it) }
        }
    }
    
    private fun onWallpaperClicked(wallpaper:Wallpaper, holder:WallpaperHolder) {
        val intent = Intent(activity, ViewerActivity::class.java)
        intent.putExtra("wallpaper", wallpaper)
        intent.putExtra("inFavorites", favoritesModel.isInFavorites(wallpaper))
        intent.putExtra("showFavoritesButton", showFavoritesIcon())
        val imgTransition = ViewCompat.getTransitionName(holder.img)
        val nameTransition = ViewCompat.getTransitionName(holder.name)
        val authorTransition = ViewCompat.getTransitionName(holder.author)
        val heartTransition = ViewCompat.getTransitionName(holder.heartIcon)
        intent.putExtra("imgTransition", imgTransition)
        intent.putExtra("nameTransition", nameTransition)
        intent.putExtra("authorTransition", authorTransition)
        intent.putExtra("favTransition", heartTransition)
        
        try {
            holder.bitmap?.let {
                val filename = "thumb.png"
                val stream = activity.openFileOutput(filename, Context.MODE_PRIVATE)
                it.compress(Bitmap.CompressFormat.JPEG, context.maxPictureRes, stream)
                stream.flush()
                stream.close()
                intent.putExtra("image", filename)
            }
            val imgPair = Pair<View, String>(holder.img, imgTransition)
            val namePair = Pair<View, String>(holder.name, nameTransition)
            val authorPair = Pair<View, String>(holder.author, authorTransition)
            val heartPair = Pair<View, String>(holder.heartIcon, heartTransition)
            val options = ActivityOptionsCompat.makeSceneTransitionAnimation(activity, imgPair,
                                                                             namePair, authorPair,
                                                                             heartPair)
            startActivityForResult(intent, 10, options.toBundle())
        } catch (ignored:Exception) {
            startActivityForResult(intent, 10)
        }
        activity.overridePendingTransition(0, 0)
    }
    
    override fun onActivityResult(requestCode:Int, resultCode:Int, data:Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == 10) {
            data?.let {
                if (it.getBooleanExtra("modified", false)) {
                    val item = it.getParcelableExtra<Wallpaper>("item")
                    if (it.getBooleanExtra("inFavorites", false)) {
                        item?.let { addToFavorites(it) }
                    } else {
                        item?.let { removeFromFavorites(it) }
                    }
                }
            }
        }
    }
    
    abstract fun fromFavorites():Boolean
    abstract fun showFavoritesIcon():Boolean
}