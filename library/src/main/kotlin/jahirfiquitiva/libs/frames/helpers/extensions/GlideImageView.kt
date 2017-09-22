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
package jahirfiquitiva.libs.frames.helpers.extensions

import android.graphics.Bitmap
import android.graphics.ColorMatrixColorFilter
import android.graphics.drawable.Drawable
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions.withCrossFade
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import jahirfiquitiva.libs.frames.helpers.utils.GlideRequestListener
import jahirfiquitiva.libs.frames.ui.graphics.ObservableColorMatrix

fun ImageView.releaseFromGlide() {
    Glide.with(context).clear(this)
}

fun ImageView.setSaturation(saturation:Float) {
    val matrix = ObservableColorMatrix()
    matrix.setSaturation(saturation)
    colorFilter = ColorMatrixColorFilter(matrix)
}

fun ImageView.loadWallpaper(requester:RequestManager?, url:String, thumbUrl:String,
                            hasFaded:Boolean, listener:GlideRequestListener<Bitmap>?) {
    val manager = requester ?: Glide.with(context)
    val loadFullRes = context.framesKonfigs.fullResGridPictures
    val validThumb = !thumbUrl.equals(url, true)
    
    val thumbnailRequest = manager.asBitmap()
            .load(thumbUrl)
            .apply(if (validThumb) context.thumbnailOptions else context.wallpaperOptions)
            .transition(withCrossFade())
            .listener(object:GlideRequestListener<Bitmap>() {
                override fun onLoadSucceed(resource:Bitmap):Boolean =
                        listener?.onLoadSucceed(resource) ?: false
            })
    
    if (loadFullRes) {
        loadBitmap(manager, url, !hasFaded, false, thumbnailRequest, listener)
    } else {
        thumbnailRequest.into(this)
    }
}

fun ImageView.loadAvatar(requester:RequestManager?, url:String, shouldAnimate:Boolean) {
    loadBitmap(requester, url, shouldAnimate, true, null, null)
}

private fun ImageView.loadBitmap(requester:RequestManager?,
                                 url:String, shouldAnimate:Boolean, isAvatar:Boolean,
                                 thumbnail:RequestBuilder<Bitmap>?,
                                 listener:GlideRequestListener<Bitmap>?) {
    
    val manager = requester ?: Glide.with(context)
    val options = context.wallpaperOptions
    
    if (isAvatar) options.transform(CircleCrop())
    
    val builder = manager.asBitmap()
            .load(url)
            .listener(listener)
    
    if (shouldAnimate) builder.transition(withCrossFade()) else options.dontAnimate()
    
    builder.apply(options)
    
    if (thumbnail != null) {
        builder.thumbnail(thumbnail)
    } else {
        builder.thumbnail(0.5F)
    }
    
    builder.into(this)
}

fun ImageView.loadResource(requester:RequestManager?, resId:Int, dontTransform:Boolean,
                           shouldAnimate:Boolean, immediately:Boolean,
                           listener:GlideRequestListener<Drawable>?) {
    val manager = requester ?: Glide.with(context)
    
    val options = context.resourceOptions
    
    if (dontTransform) {
        options.dontTransform()
    }
    
    if (!shouldAnimate) {
        options.dontAnimate()
    }
    
    if (immediately) {
        options.priority(Priority.IMMEDIATE)
    } else {
        options.priority(Priority.HIGH)
    }
    
    manager.load(resId).apply(options).listener(listener).into(this)
}