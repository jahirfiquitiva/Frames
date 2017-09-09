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
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import jahirfiquitiva.libs.frames.helpers.utils.GlideRequestListener
import jahirfiquitiva.libs.frames.helpers.utils.GlideTarget
import jahirfiquitiva.libs.frames.ui.graphics.ObservableColorMatrix

fun ImageView.setSaturation(saturation:Float) {
    val matrix = ObservableColorMatrix()
    matrix.setSaturation(saturation)
    colorFilter = ColorMatrixColorFilter(matrix)
}

fun ImageView.loadWallpaper(requester:RequestManager?, url:String, thumbUrl:String,
                            transform:Boolean, hasFaded:Boolean,
                            listener:GlideRequestListener<Bitmap>?, target:GlideTarget?) {
    
    val manager = requester ?: Glide.with(context)
    val options = RequestOptions().diskCacheStrategy(DiskCacheStrategy.RESOURCE)
            .priority(Priority.IMMEDIATE)
    if (!transform) options.dontTransform()
    
    val thumbnailRequest = if (!thumbUrl.equals(url, true)) {
        manager.asBitmap().load(thumbUrl)
                .apply(options)
                .listener(object:GlideRequestListener<Bitmap>() {
                    override fun onLoadSucceed(resource:Bitmap):Boolean {
                        isEnabled = true
                        listener?.onLoadSucceed(resource)
                        return true
                    }
                })
    } else null
    
    if ((listener != null || target != null) && !hasFaded && context.framesKonfigs.animationsEnabled) {
        setSaturation(0F)
        isEnabled = false
    }
    
    loadBitmap(requester, url, !transform, !hasFaded,
               if (listener != null || target != null) thumbnailRequest else null,
               listener, target)
}

fun ImageView.loadAvatar(requester:RequestManager?, url:String, shouldAnimate:Boolean) {
    loadBitmap(requester, url, false, shouldAnimate, null, null,
               object:GlideTarget(this) {
                   override fun onLoadSucceed(resource:Bitmap) {
                       setImageDrawable(resource.createRoundedDrawable(context))
                   }
               })
}

private fun ImageView.loadBitmap(requester:RequestManager?,
                                 url:String, dontTransform:Boolean, shouldAnimate:Boolean,
                                 thumbnail:RequestBuilder<Bitmap>?,
                                 listener:GlideRequestListener<Bitmap>?,
                                 target:GlideTarget?) {
    val manager = requester ?: Glide.with(context)
    val options = RequestOptions().diskCacheStrategy(DiskCacheStrategy.RESOURCE)
            .priority(Priority.HIGH)
    if (dontTransform) options.dontTransform()
    if (!shouldAnimate) {
        options.dontAnimate()
    }
    
    val builder = manager.asBitmap().load(url)
            .apply(options)
            .listener(listener)
    
    if (thumbnail != null) {
        builder.thumbnail(thumbnail)
    } else {
        builder.thumbnail(0.5F)
    }
    if (target != null) {
        builder.into(target)
    } else {
        builder.into(this)
    }
}

fun ImageView.loadResource(requester:RequestManager?, resId:Int, dontTransform:Boolean,
                           shouldAnimate:Boolean, immediately:Boolean,
                           listener:GlideRequestListener<Drawable>?) {
    val manager = requester ?: Glide.with(context)
    
    val options = RequestOptions().diskCacheStrategy(DiskCacheStrategy.NONE)
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