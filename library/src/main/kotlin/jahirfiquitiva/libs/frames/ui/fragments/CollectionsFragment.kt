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
import android.support.v4.util.Pair
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.GridLayoutManager
import android.view.View
import com.bumptech.glide.Glide
import com.pluscubed.recyclerfastscroll.RecyclerFastScroller
import jahirfiquitiva.libs.frames.R
import jahirfiquitiva.libs.frames.data.models.Collection
import jahirfiquitiva.libs.frames.data.models.Wallpaper
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
    private lateinit var adapter:CollectionsAdapter
    private lateinit var fastScroll:RecyclerFastScroller
    
    override fun initUI(content:View) {
        swipeToRefresh = content.findViewById(R.id.swipe_to_refresh)
        swipeToRefresh.setProgressBackgroundColorSchemeColor(context.cardBackgroundColor)
        swipeToRefresh.setColorSchemeColors(context.accentColor)
        swipeToRefresh.setOnRefreshListener {
            reloadData(0)
        }
        
        rv = content.findViewById(R.id.list_rv)
        rv.itemAnimator = DefaultItemAnimator()
        rv.textView = content.findViewById(R.id.empty_text)
        rv.emptyView = content.findViewById(R.id.empty_view)
        rv.setEmptyImage(R.drawable.empty_section)
        rv.setEmptyText(R.string.empty_section)
        rv.loadingView = content.findViewById(R.id.loading_view)
        rv.setLoadingText(R.string.loading_section)
        val spanCount = if (context.isInHorizontalMode) 2 else 1
        rv.layoutManager = GridLayoutManager(context, spanCount, GridLayoutManager.VERTICAL, false)
        rv.addItemDecoration(GridSpacingItemDecoration(spanCount, 0, true))
        adapter = CollectionsAdapter(Glide.with(this), { collection, holder ->
            onItemClicked(collection, holder)
        })
        rv.adapter = adapter
        fastScroll = content.findViewById(R.id.fast_scroller)
        fastScroll.attachRecyclerView(rv)
        rv.state = EmptyViewRecyclerView.State.LOADING
    }
    
    override fun getContentLayout():Int = R.layout.section_lists
    
    override fun scrollToTop() {
        rv.layoutManager.scrollToPosition(0)
    }
    
    override fun onItemClicked(item:Collection, holder:CollectionHolder) {
        val intent = Intent(activity, CollectionActivity::class.java)
        intent.putExtra("item", item)
        val titlePair = Pair<View, String>(holder.title, "title")
        val options = ActivityOptionsCompat.makeSceneTransitionAnimation(activity, titlePair)
        try {
            activity.startActivityForResult(intent, 11, options.toBundle())
        } catch (ignored:Exception) {
            activity.startActivityForResult(intent, 11)
        }
        activity.overridePendingTransition(0, 0)
    }
    
    override fun loadDataFromViewModel() {
        rv.state = EmptyViewRecyclerView.State.LOADING
        super.loadDataFromViewModel()
    }
    
    override fun reloadData(section:Int) {
        rv.state = EmptyViewRecyclerView.State.LOADING
        super.reloadData(section)
    }
    
    override fun applyFilter(filter:String) {
        collectionsModel.items.value?.let {
            if (filter.hasContent()) {
                rv.setEmptyImage(R.drawable.no_results)
                rv.setEmptyText(R.string.search_no_results)
                adapter.setItems(ArrayList(it.filter { it.name.contains(filter, true) }))
            } else {
                rv.setEmptyImage(R.drawable.empty_section)
                rv.setEmptyText(R.string.empty_section)
                adapter.setItems(it)
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
        adapter.setItems(data)
        swipeToRefresh.isRefreshing = false
    }
    
    override fun autoStartLoad():Boolean = true
}