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

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.support.v7.widget.GridLayoutManager
import com.pluscubed.recyclerfastscroll.RecyclerFastScroller
import jahirfiquitiva.libs.frames.R
import jahirfiquitiva.libs.frames.adapters.CollectionsAdapter
import jahirfiquitiva.libs.frames.extensions.isInHorizontalMode
import jahirfiquitiva.libs.frames.fragments.base.BaseViewModelFragment
import jahirfiquitiva.libs.frames.models.Collection
import jahirfiquitiva.libs.frames.models.Wallpaper
import jahirfiquitiva.libs.frames.models.viewmodels.CollectionsViewModel
import jahirfiquitiva.libs.frames.models.viewmodels.WallpapersViewModel
import jahirfiquitiva.libs.kauextensions.ui.views.EmptyViewRecyclerView

class CollectionsFragment:BaseViewModelFragment<Collection>() {

    private lateinit var wallpapersModel:WallpapersViewModel
    private lateinit var collectionsModel:CollectionsViewModel

    private val rv by lazy(LazyThreadSafetyMode.NONE) {
        view?.findViewById<EmptyViewRecyclerView>(R.id.list_rv).also {
            it?.emptyView = view?.findViewById(R.id.empty_view)
            it?.emptyView = view?.findViewById(R.id.empty_view)
            it?.emptyTextRes = R.string.empty_section
            it?.loadingView = view?.findViewById(R.id.loading_view)
            it?.loadingTextRes = R.string.loading_section
            it?.state = EmptyViewRecyclerView.State.LOADING
            it?.layoutManager = GridLayoutManager(context, if (context.isInHorizontalMode) 2 else 1,
                                                  GridLayoutManager.VERTICAL, false)
        }
    }

    private val adapter:CollectionsAdapter by lazy(LazyThreadSafetyMode.NONE) {
        CollectionsAdapter { onItemClicked(it) }.also { rv?.adapter = it }
    }

    private val fastScroll by lazy(LazyThreadSafetyMode.NONE) {
        view?.findViewById<RecyclerFastScroller>(R.id.fast_scroller).also {
            it?.attachRecyclerView(rv)
        }
    }

    override fun onItemClicked(item:Collection) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getContentLayout():Int = R.layout.section_lists

    override fun initViewModel() {
        wallpapersModel = ViewModelProviders.of(activity).get(WallpapersViewModel::class.java)
        collectionsModel = ViewModelProviders.of(activity).get(CollectionsViewModel::class.java)
    }

    override fun registerObserver() {
        wallpapersModel.items.observe(this, Observer<ArrayList<Wallpaper>> { data ->
            data?.let { collectionsModel.loadData(it) }
        })
        collectionsModel.items.observe(this, Observer<ArrayList<Collection>> { data ->
            data?.let { adapter.clearAndAddAll(data) }
        })
    }

    override fun loadDataFromViewModel() {
        wallpapersModel.loadData(activity)
    }

    override fun unregisterObserver() {
        wallpapersModel.items.removeObservers(this)
        collectionsModel.items.removeObservers(this)
    }
}