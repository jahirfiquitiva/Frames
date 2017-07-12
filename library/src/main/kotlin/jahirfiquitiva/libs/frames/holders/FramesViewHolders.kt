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

package jahirfiquitiva.libs.frames.holders

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import jahirfiquitiva.libs.frames.R
import jahirfiquitiva.libs.frames.extensions.createHeartSelector
import jahirfiquitiva.libs.frames.extensions.loadFromUrls
import jahirfiquitiva.libs.frames.models.Collection
import jahirfiquitiva.libs.frames.models.Wallpaper
import jahirfiquitiva.libs.frames.views.CheckableImageView

class CollectionHolder(itemView:View):RecyclerView.ViewHolder(itemView) {
    val img:ImageView = itemView.findViewById(R.id.collection_picture)
    val title:TextView = itemView.findViewById(R.id.collection_title)
    fun setItem(collection:Collection, listener:(Collection) -> Unit) =
            with(itemView) {
                img.loadFromUrls(collection.wallpapers.first().url,
                                 collection.wallpapers.first().thumbUrl)
                title.text = collection.name
                setOnClickListener { listener(collection) }
            }
}

class WallpaperHolder(itemView:View):RecyclerView.ViewHolder(itemView) {
    val img:ImageView = itemView.findViewById(R.id.wallpaper_image)
    val title:TextView = itemView.findViewById(R.id.wallpaper_name)
    val heartIcon:CheckableImageView = itemView.findViewById(R.id.heart_icon)
    fun setItem(wallpaper:Wallpaper, listener:(Wallpaper) -> Unit) =
            with(itemView) {
                img.loadFromUrls(wallpaper.url, wallpaper.thumbUrl)
                title.text = wallpaper.name
                heartIcon.setImageDrawable(context.createHeartSelector())
                heartIcon.setOnClickListener {
                    heartIcon.toggle()
                    listener(wallpaper)
                }
            }
}