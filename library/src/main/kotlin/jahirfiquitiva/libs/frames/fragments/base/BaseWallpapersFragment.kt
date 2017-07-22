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
package jahirfiquitiva.libs.frames.fragments.base

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.support.v4.app.ActivityOptionsCompat
import android.support.v4.util.Pair
import android.support.v4.view.ViewCompat
import android.support.v7.widget.GridLayoutManager
import android.view.View
import ca.allanwang.kau.utils.dimenPixelSize
import com.pluscubed.recyclerfastscroll.RecyclerFastScroller
import jahirfiquitiva.libs.frames.R
import jahirfiquitiva.libs.frames.activities.ViewerActivity
import jahirfiquitiva.libs.frames.adapters.WallpapersAdapter
import jahirfiquitiva.libs.frames.configs.maxPictureRes
import jahirfiquitiva.libs.frames.extensions.framesKonfigs
import jahirfiquitiva.libs.frames.holders.WallpaperHolder
import jahirfiquitiva.libs.frames.models.Wallpaper
import jahirfiquitiva.libs.kauextensions.ui.decorations.GridSpacingItemDecoration
import jahirfiquitiva.libs.kauextensions.ui.views.EmptyViewRecyclerView

abstract class BaseWallpapersFragment:BaseFramesFragment<Wallpaper, WallpaperHolder>() {

    internal lateinit var rv:EmptyViewRecyclerView
    internal lateinit var adapter:WallpapersAdapter
    private lateinit var fastScroll:RecyclerFastScroller

    override fun initUI(content:View) {
        rv = content.findViewById(R.id.list_rv)
        rv.emptyView = content.findViewById(R.id.no_favorites_view)
        rv.textView = content.findViewById(R.id.empty_text)
        rv.emptyTextRes = R.string.no_favorites
        rv.loadingView = content.findViewById(R.id.loading_view)
        rv.loadingTextRes = R.string.loading_section
        val spanCount = context.framesKonfigs.columns
        rv.layoutManager = GridLayoutManager(context, spanCount,
                                             GridLayoutManager.VERTICAL, false)
        rv.addItemDecoration(
                GridSpacingItemDecoration(spanCount,
                                          context.dimenPixelSize(R.dimen.wallpapers_grid_spacing)))
        adapter = WallpapersAdapter(
                { wall, holder -> onItemClicked(wall, holder) },
                { heart, wall -> onHeartClicked(heart, wall) },
                fromFavorites())
        rv.adapter = adapter
        rv.state = EmptyViewRecyclerView.State.LOADING
        fastScroll = content.findViewById(R.id.fast_scroller)
        fastScroll.attachRecyclerView(rv)
    }

    override fun getContentLayout():Int = R.layout.section_lists

    override fun onItemClicked(item:Wallpaper, holder:WallpaperHolder) {
        onWallpaperClicked(item, holder)
    }

    private fun onWallpaperClicked(wallpaper:Wallpaper, holder:WallpaperHolder) {
        val intent = Intent(activity, ViewerActivity::class.java)
        intent.putExtra("wallpaper", wallpaper)
        intent.putExtra("inFavorites", favoritesModel.isInFavorites(wallpaper))
        val imgTransition = ViewCompat.getTransitionName(holder.img)
        val nameTransition = ViewCompat.getTransitionName(holder.name)
        val authorTransition = ViewCompat.getTransitionName(holder.author)
        intent.putExtra("imgTransition", imgTransition)
        intent.putExtra("nameTransition", nameTransition)
        intent.putExtra("authorTransition", authorTransition)

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

            val options = ActivityOptionsCompat.makeSceneTransitionAnimation(activity, imgPair,
                                                                             namePair, authorPair)
            startActivityForResult(intent, 10, options.toBundle())
        } catch (ignored:Exception) {
            startActivityForResult(intent, 10)
        }
    }

    override fun onActivityResult(requestCode:Int, resultCode:Int, data:Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == 10) {
            data?.let {
                if (it.getBooleanExtra("modified", false)) {
                    val item = it.getParcelableExtra<Wallpaper>("item")
                    if (it.getBooleanExtra("inFavorites", false)) {
                        item?.let { addToFavorites(it, {}) }
                    } else {
                        item?.let { removeFromFavorites(it, {}) }
                    }
                }
            }
        }
    }

    abstract fun fromFavorites():Boolean
}