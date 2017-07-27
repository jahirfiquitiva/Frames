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

package jahirfiquitiva.libs.frames.adapters

import android.view.ViewGroup
import ca.allanwang.kau.utils.inflate
import jahirfiquitiva.libs.frames.R
import jahirfiquitiva.libs.frames.holders.WallpaperHolder
import jahirfiquitiva.libs.frames.models.Wallpaper
import jahirfiquitiva.libs.frames.views.CheckableImageView

class WallpapersAdapter(val listener:(Wallpaper, WallpaperHolder) -> Unit,
                        val heartListener:(CheckableImageView, Wallpaper) -> Unit,
                        val fromFavorites:Boolean = false, val showFavIcon:Boolean = true):
        BaseListAdapter<Wallpaper, WallpaperHolder>() {

    var favorites = ArrayList<Wallpaper>()
        set(value) {
            field.clear()
            field.addAll(value)
            notifyDataSetChanged()
        }

    override fun doBind(holder:WallpaperHolder, position:Int) {
        val item = list[position]
        holder.setItem(item, listener, heartListener, fromFavorites || favorites.contains(item))
    }

    override fun onCreateViewHolder(parent:ViewGroup?, viewType:Int):WallpaperHolder? =
            parent?.inflate(R.layout.item_wallpaper)?.let { WallpaperHolder(it, showFavIcon) }
}