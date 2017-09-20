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
package jahirfiquitiva.libs.frames.ui.fragments

import android.content.Intent
import android.support.v4.app.ActivityOptionsCompat
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.GridLayoutManager
import android.view.View
import com.bumptech.glide.Glide
import com.bumptech.glide.integration.recyclerview.RecyclerViewPreloader
import com.bumptech.glide.util.ViewPreloadSizeProvider
import com.pluscubed.recyclerfastscroll.RecyclerFastScroller
import jahirfiquitiva.libs.frames.R
import jahirfiquitiva.libs.frames.data.models.Collection
import jahirfiquitiva.libs.frames.data.models.Wallpaper
import jahirfiquitiva.libs.frames.helpers.configs.isLowRamDevice
import jahirfiquitiva.libs.frames.ui.activities.CollectionActivity
import jahirfiquitiva.libs.frames.ui.adapters.CollectionsAdapter
import jahirfiquitiva.libs.frames.ui.adapters.viewholders.CollectionHolder
import jahirfiquitiva.libs.frames.ui.fragments.base.BaseFramesFragment
import jahirfiquitiva.libs.frames.ui.widgets.EmptyViewRecyclerView
import jahirfiquitiva.libs.kauextensions.extensions.accentColor
import jahirfiquitiva.libs.kauextensions.extensions.cardBackgroundColor
import jahirfiquitiva.libs.kauextensions.extensions.hasContent
import jahirfiquitiva.libs.kauextensions.extensions.isInHorizontalMode
import jahirfiquitiva.libs.kauextensions.ui.decorations.GridSpacingItemDecoration

class CollectionsFragment:BaseFramesFragment<Collection, CollectionHolder>() {
    
    private lateinit var swipeToRefresh:SwipeRefreshLayout
    private lateinit var rv:EmptyViewRecyclerView
    private lateinit var fastScroll:RecyclerFastScroller
    
    private var collsAdapter:CollectionsAdapter? = null
    
    override fun initUI(content:View) {
        swipeToRefresh = content.findViewById(R.id.swipe_to_refresh)
        rv = content.findViewById(R.id.list_rv)
        fastScroll = content.findViewById(R.id.fast_scroller)
        
        with(swipeToRefresh) {
            setProgressBackgroundColorSchemeColor(context.cardBackgroundColor)
            setColorSchemeColors(context.accentColor)
            setOnRefreshListener { reloadData(0) }
        }
        
        with(rv) {
            itemAnimator = if (context.isLowRamDevice) null else DefaultItemAnimator()
            textView = content.findViewById(R.id.empty_text)
            emptyView = content.findViewById(R.id.empty_view)
            setEmptyImage(R.drawable.empty_section)
            setEmptyText(R.string.empty_section)
            loadingView = content.findViewById(R.id.loading_view)
            setLoadingText(R.string.loading_section)
            val spanCount = if (context.isInHorizontalMode) 2 else 1
            layoutManager = GridLayoutManager(context, spanCount, GridLayoutManager.VERTICAL, false)
            addItemDecoration(GridSpacingItemDecoration(spanCount, 0, true))
            
            val provider = ViewPreloadSizeProvider<Wallpaper>()
            collsAdapter = CollectionsAdapter(Glide.with(this), provider,
                                              { collection ->
                                                  onItemClicked(collection)
                                              })
            val preloader:RecyclerViewPreloader<Wallpaper> =
                    RecyclerViewPreloader(activity, collsAdapter, provider, 4)
            addOnScrollListener(preloader)
            adapter = collsAdapter
            
        }
        
        with(fastScroll) {
            attachSwipeRefreshLayout(swipeToRefresh)
            attachRecyclerView(rv)
        }
        
        rv.state = EmptyViewRecyclerView.State.LOADING
    }
    
    override fun getContentLayout():Int = R.layout.section_lists
    
    override fun scrollToTop() {
        rv.layoutManager.scrollToPosition(0)
    }
    
    override fun onItemClicked(item:Collection) {
        val intent = Intent(activity, CollectionActivity::class.java)
        intent.putExtra("item", item)
        val options = ActivityOptionsCompat.makeSceneTransitionAnimation(activity)
        try {
            activity.startActivityForResult(intent, 11, options.toBundle())
        } catch (ignored:Exception) {
            activity.startActivityForResult(intent, 11)
        }
    }
    
    override fun onItemClicked(item:Collection, holder:CollectionHolder) {}
    
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
    
    override fun applyFilter(filter:String) {
        if (collsAdapter != null) {
            collectionsModel?.items?.value?.let {
                if (filter.hasContent()) {
                    rv.setEmptyImage(R.drawable.no_results)
                    rv.setEmptyText(R.string.search_no_results)
                    collsAdapter?.updateItems(
                            ArrayList(it.filter { it.name.contains(filter, true) }),
                            true)
                } else {
                    rv.setEmptyImage(R.drawable.empty_section)
                    rv.setEmptyText(R.string.empty_section)
                    collsAdapter?.updateItems(it, true)
                    scrollToTop()
                }
            }
        }
    }
    
    override fun doOnFavoritesChange(data:ArrayList<Wallpaper>) {
        super.doOnFavoritesChange(data)
        swipeToRefresh.isRefreshing = false
    }
    
    override fun doOnWallpapersChange(data:ArrayList<Wallpaper>, fromCollectionActivity:Boolean) {
        super.doOnWallpapersChange(data, fromCollectionActivity)
        swipeToRefresh.isRefreshing = false
    }
    
    override fun doOnCollectionsChange(data:ArrayList<Collection>) {
        super.doOnCollectionsChange(data)
        swipeToRefresh.isRefreshing = false
        collsAdapter?.setItems(data)
    }
    
    override fun autoStartLoad():Boolean = true
    override fun fromCollectionActivity():Boolean = false
    override fun fromFavorites():Boolean = false
}