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

import android.support.v7.widget.GridLayoutManager
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import ca.allanwang.kau.utils.dimenPixelSize
import com.pluscubed.recyclerfastscroll.RecyclerFastScroller
import jahirfiquitiva.libs.frames.R
import jahirfiquitiva.libs.frames.adapters.WallpapersAdapter
import jahirfiquitiva.libs.frames.extensions.isInHorizontalMode
import jahirfiquitiva.libs.frames.fragments.base.BaseFramesFragment
import jahirfiquitiva.libs.frames.models.Wallpaper
import jahirfiquitiva.libs.kauextensions.ui.decorations.GridSpacingItemDecoration
import jahirfiquitiva.libs.kauextensions.ui.views.EmptyViewRecyclerView

class FavoritesFragment:BaseFramesFragment<Wallpaper>() {

    private lateinit var rv:EmptyViewRecyclerView
    private lateinit var adapter:WallpapersAdapter
    private lateinit var fastScroll:RecyclerFastScroller

    override fun initUI(content:View) {
        rv = content.findViewById(R.id.list_rv)
        rv.emptyView = content.findViewById(R.id.no_favorites_view)
        rv.textView = content.findViewById(R.id.empty_text)
        rv.emptyTextRes = R.string.no_favorites
        rv.loadingView = content.findViewById(R.id.loading_view)
        rv.loadingTextRes = R.string.loading_section
        val spanCount = if (context.isInHorizontalMode) 3 else 2
        rv.layoutManager = GridLayoutManager(context, spanCount,
                                             GridLayoutManager.VERTICAL, false)
        rv.addItemDecoration(
                GridSpacingItemDecoration(spanCount, context.dimenPixelSize(R.dimen.cards_margin)))
        adapter = WallpapersAdapter(
                { wall, holder -> onWallpaperClicked(wall, holder) },
                { heart, wall -> onHeartClicked(heart, wall) },
                true)
        rv.adapter = adapter
        rv.state = EmptyViewRecyclerView.State.LOADING
        fastScroll = content.findViewById(R.id.fast_scroller)
        fastScroll.attachRecyclerView(rv)
    }

    override fun onItemClicked(item:Wallpaper) {
        // Do nothing
    }

    override fun getContentLayout():Int = R.layout.section_lists

    override fun doOnFavoritesChange(data:ArrayList<Wallpaper>) {
        super.doOnFavoritesChange(data)
        adapter.setItems(data)
        rv.state = EmptyViewRecyclerView.State.NORMAL
    }
}