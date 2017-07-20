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
package jahirfiquitiva.libs.frames.extensions

import android.graphics.drawable.Drawable
import android.support.annotation.DrawableRes
import android.support.v4.content.ContextCompat
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.target.BitmapImageViewTarget

fun ImageView.loadFromUrl(url:String, @DrawableRes error:Int) {
    loadFromUrl(url, ContextCompat.getDrawable(context, error))
}

fun ImageView.loadFromUrl(url:String, error:Drawable? = null) {
    if (url.isEmpty() && error != null) {
        Glide.with(context).load(error).into(this)
    } else {
        Glide.with(context).load(url)
                .priority(Priority.HIGH).diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .into(this)
    }
}

fun ImageView.loadFromUrls(url:String, thumbUrl:String, sizeMultiplier:Float = 0.5F) {
    if (thumbUrl.isNotEmpty()) {
        Glide.with(context).load(url)
                .priority(Priority.HIGH).diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .thumbnail(Glide.with(context).load(thumbUrl)
                                   .priority(Priority.IMMEDIATE)
                                   .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                                   .thumbnail(sizeMultiplier))
                .into(this)
    } else if (url.isNotEmpty()) {
        Glide.with(context).load(url)
                .priority(Priority.HIGH).diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .thumbnail(sizeMultiplier).into(this)
    }
}

fun ImageView.loadFromUrlsIntoTarget(url:String, thumbUrl:String, target:BitmapImageViewTarget,
                                     sizeMultiplier:Float = 0.5F) {
    if (thumbUrl.isNotEmpty()) {
        Glide.with(context).load(url).asBitmap()
                .priority(Priority.HIGH).diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .thumbnail(Glide.with(context).load(thumbUrl).asBitmap()
                                   .priority(Priority.IMMEDIATE)
                                   .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                                   .thumbnail(sizeMultiplier))
                .into(target)
    } else if (url.isNotEmpty()) {
        Glide.with(context).load(url).asBitmap()
                .priority(Priority.HIGH).diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .thumbnail(sizeMultiplier).into(target)
    }
}