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

import android.graphics.ColorMatrixColorFilter
import android.graphics.drawable.Drawable
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
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
                            hasFaded:Boolean, listener:GlideRequestListener<Drawable>?) {
    val manager = requester ?: Glide.with(context)
    val loadFullRes = context.framesKonfigs.fullResGridPictures
    
    val thumbnailRequest = manager.load(thumbUrl)
            .apply(context.thumbnailOptions.override(512, 683))
            .transition(withCrossFade())
            .listener(object:GlideRequestListener<Drawable>() {
                override fun onLoadSucceed(resource:Drawable):Boolean =
                        listener?.onLoadSucceed(resource) ?: false
            })
    
    if (loadFullRes) {
        loadDrawable(manager, url, !hasFaded, false, thumbnailRequest, listener)
    } else {
        thumbnailRequest.into(this)
    }
}

fun ImageView.loadAvatar(requester:RequestManager?, url:String, shouldAnimate:Boolean) {
    loadDrawable(requester, url, shouldAnimate, true, null, null)
}

private fun ImageView.loadDrawable(requester:RequestManager?,
                                   url:String, shouldAnimate:Boolean, isAvatar:Boolean,
                                   thumbnail:RequestBuilder<Drawable>?,
                                   listener:GlideRequestListener<Drawable>?) {
    
    val manager = requester ?: Glide.with(context)
    val options = context.wallpaperOptions
    
    if (isAvatar) options.transform(CircleCrop())
    
    if (!shouldAnimate) options.dontAnimate()
    
    val builder = manager.load(url)
            .apply(options)
            .transition(withCrossFade())
            .listener(listener)
    
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