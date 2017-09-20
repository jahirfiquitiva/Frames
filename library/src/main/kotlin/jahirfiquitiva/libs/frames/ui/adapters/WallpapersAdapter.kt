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
package jahirfiquitiva.libs.frames.ui.adapters

import android.view.ViewGroup
import android.widget.ImageView
import ca.allanwang.kau.utils.inflate
import com.bumptech.glide.ListPreloader
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.RequestManager
import com.bumptech.glide.util.ViewPreloadSizeProvider
import jahirfiquitiva.libs.frames.R
import jahirfiquitiva.libs.frames.data.models.Wallpaper
import jahirfiquitiva.libs.frames.helpers.utils.diff.WallpapersDiffCallback
import jahirfiquitiva.libs.frames.ui.adapters.viewholders.WallpaperHolder
import java.util.*

class WallpapersAdapter(private val manager:RequestManager,
                        private val provider:ViewPreloadSizeProvider<Wallpaper>,
                        private val singleTap:(Wallpaper, WallpaperHolder) -> Unit,
                        private val heartListener:(ImageView, Wallpaper) -> Unit,
                        private val fromFavorites:Boolean,
                        private val showFavIcon:Boolean):
        BaseListAdapter<Wallpaper, WallpaperHolder>(),
        ListPreloader.PreloadModelProvider<Wallpaper> {
    
    var favorites = ArrayList<Wallpaper>()
        set(value) {
            field.clear()
            field.addAll(value)
        }
    
    override fun doBind(holder:WallpaperHolder, position:Int, shouldAnimate:Boolean) {
        val item = list[position]
        holder.setItem(manager, provider, item, singleTap, heartListener,
                       fromFavorites || favorites.contains(item))
    }
    
    override fun onCreateViewHolder(parent:ViewGroup?, viewType:Int):WallpaperHolder? =
            parent?.inflate(R.layout.item_wallpaper)?.let { WallpaperHolder(it, showFavIcon) }
    
    override fun updateItems(newItems:ArrayList<Wallpaper>, detectMoves:Boolean) {
        updateItems(newItems, WallpapersDiffCallback(list, newItems), detectMoves)
    }
    
    override fun getPreloadItems(position:Int):MutableList<Wallpaper> =
            Collections.singletonList(list[position])
    
    override fun getPreloadRequestBuilder(item:Wallpaper?):RequestBuilder<*> =
            manager.load(item?.thumbUrl)
}