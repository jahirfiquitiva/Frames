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

package jahirfiquitiva.libs.frames.fragments

import android.content.Intent
import android.support.v4.app.ActivityOptionsCompat
import android.support.v4.util.Pair
import android.support.v7.widget.GridLayoutManager
import android.view.View
import com.pluscubed.recyclerfastscroll.RecyclerFastScroller
import jahirfiquitiva.libs.frames.R
import jahirfiquitiva.libs.frames.activities.CollectionActivity
import jahirfiquitiva.libs.frames.adapters.CollectionsAdapter
import jahirfiquitiva.libs.frames.fragments.base.BaseFramesFragment
import jahirfiquitiva.libs.frames.holders.CollectionHolder
import jahirfiquitiva.libs.frames.models.Collection
import jahirfiquitiva.libs.frames.models.Wallpaper
import jahirfiquitiva.libs.kauextensions.extensions.hasContent
import jahirfiquitiva.libs.kauextensions.extensions.isInHorizontalMode
import jahirfiquitiva.libs.kauextensions.extensions.printInfo
import jahirfiquitiva.libs.kauextensions.ui.decorations.GridSpacingItemDecoration
import jahirfiquitiva.libs.kauextensions.ui.views.EmptyViewRecyclerView

class CollectionsFragment:BaseFramesFragment<Collection, CollectionHolder>() {

    private lateinit var rv:EmptyViewRecyclerView
    private lateinit var adapter:CollectionsAdapter
    private lateinit var fastScroll:RecyclerFastScroller

    override fun initUI(content:View) {
        rv = content.findViewById(R.id.list_rv)
        rv.textView = content.findViewById(R.id.empty_text)
        rv.emptyView = content.findViewById(R.id.empty_view)
        rv.emptyTextRes = R.string.empty_section
        rv.loadingView = content.findViewById(R.id.loading_view)
        rv.loadingTextRes = R.string.loading_section
        val spanCount = if (context.isInHorizontalMode) 2 else 1
        rv.layoutManager = GridLayoutManager(context, spanCount,
                                             GridLayoutManager.VERTICAL, false)
        rv.addItemDecoration(GridSpacingItemDecoration(spanCount, 0, true))
        adapter = CollectionsAdapter { collection, holder ->
            onItemClicked(collection, holder)
        }
        rv.adapter = adapter
        rv.state = EmptyViewRecyclerView.State.LOADING
        fastScroll = content.findViewById(R.id.fast_scroller)
        fastScroll.attachRecyclerView(rv)
    }

    override fun getContentLayout():Int = R.layout.section_lists

    override fun onItemClicked(item:Collection, holder:CollectionHolder) {
        val intent = Intent(activity, CollectionActivity::class.java)
        intent.putExtra("item", item)
        val titlePair = Pair<View, String>(holder.title, "title")
        val options = ActivityOptionsCompat.makeSceneTransitionAnimation(activity, titlePair)
        try {
            startActivityForResult(intent, 11, options.toBundle())
        } catch (ignored:Exception) {
            startActivityForResult(intent, 11)
        }
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
                rv.emptyView = content.findViewById(R.id.no_results_view)
                rv.emptyTextRes = R.string.kau_no_results_found
                adapter.setItems(ArrayList(it.filter { it.name.contains(filter, true) }))
            } else {
                rv.emptyView = content.findViewById(R.id.empty_view)
                rv.emptyTextRes = R.string.empty_section
                adapter.setItems(it)
            }
        }
        rv.state = EmptyViewRecyclerView.State.NORMAL
    }

    override fun onActivityResult(requestCode:Int, resultCode:Int, data:Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == 11) {
            try {
                data?.let {
                    val favs = data.getSerializableExtra("favs")
                    favs?.let {
                        try {
                            val rFavs = favs as ArrayList<Wallpaper>
                            favoritesModel.forceUpdateFavorites(rFavs)
                        } catch (e:Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            } catch (e:Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun doOnFavoritesChange(data:ArrayList<Wallpaper>) {
        super.doOnFavoritesChange(data)
    }

    override fun doOnCollectionsChange(data:ArrayList<Collection>) {
        super.doOnCollectionsChange(data)
        data.forEach { context.printInfo("Found collection: " + it.toString()) }
        adapter.setItems(data)
        rv.state = EmptyViewRecyclerView.State.NORMAL
    }
}