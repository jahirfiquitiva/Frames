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

import android.graphics.Bitmap
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import ca.allanwang.kau.utils.visible
import com.bumptech.glide.request.target.BitmapImageViewTarget
import jahirfiquitiva.libs.frames.R
import jahirfiquitiva.libs.frames.extensions.createHeartSelector
import jahirfiquitiva.libs.frames.models.Collection
import jahirfiquitiva.libs.frames.models.Wallpaper
import jahirfiquitiva.libs.frames.views.CheckableImageView
import jahirfiquitiva.libs.kauextensions.extensions.bestSwatch
import jahirfiquitiva.libs.kauextensions.extensions.cardBackgroundColor
import jahirfiquitiva.libs.kauextensions.extensions.generatePalette
import jahirfiquitiva.libs.kauextensions.extensions.getPrimaryTextColorFor
import jahirfiquitiva.libs.kauextensions.extensions.loadFromUrlsIntoTarget
import jahirfiquitiva.libs.kauextensions.ui.views.LandscapeImageView
import jahirfiquitiva.libs.kauextensions.ui.views.VerticalImageView

class CollectionHolder(itemView:View):RecyclerView.ViewHolder(itemView) {
    val img:LandscapeImageView = itemView.findViewById(R.id.collection_picture)
    val title:TextView = itemView.findViewById(R.id.collection_title)
    fun setItem(collection:Collection, listener:(Collection) -> Unit) =
            with(itemView) {
                loadImage(collection.wallpapers.first().url, collection.wallpapers.first().thumbUrl)
                title.text = collection.name
                setOnClickListener { listener(collection) }
            }

    fun loadImage(url:String, thumbUrl:String) {
        val target = object:BitmapImageViewTarget(img) {
            override fun setResource(resource:Bitmap?) {
                resource?.let {
                    super.setResource(it)
                    val color = it.generatePalette().bestSwatch?.rgb ?: itemView.context.cardBackgroundColor
                    title.setBackgroundColor(color)
                    title.setTextColor(itemView.context.getPrimaryTextColorFor(color))
                }
            }
        }
        img.loadFromUrlsIntoTarget(url, thumbUrl, target)
    }
}

class WallpaperHolder(itemView:View):RecyclerView.ViewHolder(itemView) {
    val img:VerticalImageView = itemView.findViewById(R.id.wallpaper_image)
    val titleBg:LinearLayout = itemView.findViewById(R.id.wallpaper_details)
    val title:TextView = itemView.findViewById(R.id.wallpaper_name)
    val heartIcon:CheckableImageView = itemView.findViewById(R.id.heart_icon)
    fun setItem(wallpaper:Wallpaper, listener:(Wallpaper) -> Unit,
                heartListener:(CheckableImageView, Wallpaper) -> Unit,
                check:Boolean = false) =
            with(itemView) {
                itemView.setOnClickListener { listener(wallpaper) }
                loadImage(wallpaper.url, wallpaper.thumbUrl, check)
                title.text = wallpaper.name
                heartIcon.setOnClickListener {
                    heartListener(heartIcon, wallpaper)
                }
            }

    fun loadImage(url:String, thumbUrl:String, check:Boolean) {
        val target = object:BitmapImageViewTarget(img) {
            override fun setResource(resource:Bitmap?) {
                resource?.let {
                    super.setResource(it)
                    val color = it.generatePalette().bestSwatch?.rgb ?: itemView.context.cardBackgroundColor
                    titleBg.setBackgroundColor(color)
                    heartIcon.visible()
                    heartIcon.setImageDrawable(itemView.context.createHeartSelector(color))
                    heartIcon.isChecked = check
                    title.setTextColor(itemView.context.getPrimaryTextColorFor(color))
                }
            }
        }
        img.loadFromUrlsIntoTarget(url, thumbUrl, target)
    }
}