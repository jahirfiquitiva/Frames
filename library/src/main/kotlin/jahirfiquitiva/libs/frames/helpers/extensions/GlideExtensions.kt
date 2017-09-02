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
import android.widget.ImageView
import com.bumptech.glide.BitmapRequestBuilder
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.GlideDrawable
import com.bumptech.glide.request.animation.ViewPropertyAnimation
import jahirfiquitiva.libs.frames.helpers.utils.GlideRequestListener
import jahirfiquitiva.libs.frames.helpers.utils.GlideResourceRequestListener
import jahirfiquitiva.libs.frames.helpers.utils.GlideTarget
import jahirfiquitiva.libs.frames.ui.graphics.FadeAnimator
import jahirfiquitiva.libs.frames.ui.graphics.ObservableColorMatrix

fun ImageView.releaseFromGlide() {
    Glide.clear(this)
}

fun ImageView.setSaturation(saturation:Float) {
    val matrix = ObservableColorMatrix()
    matrix.setSaturation(saturation)
    colorFilter = ColorMatrixColorFilter(matrix)
}

fun ImageView.loadWallpaper(requester:RequestManager?, url:String, thumbUrl:String,
                            transform:Boolean, hasFaded:Boolean,
                            listener:GlideRequestListener<Bitmap>?, target:GlideTarget?,
                            onTransitionFinished:() -> Unit) {
    val manager = requester ?: Glide.with(context)
    val thumbnailRequest = manager.load(thumbUrl).asBitmap()
            .diskCacheStrategy(DiskCacheStrategy.SOURCE)
            .priority(Priority.IMMEDIATE)
            .listener(object:GlideRequestListener<Bitmap>() {
                override fun onLoadSucceed(resource:Bitmap):Boolean {
                    setImageBitmap(null)
                    isEnabled = true
                    if (!hasFaded) animateColorTransition({ onTransitionFinished() })
                    return false
                }
            })
    if (!transform) thumbnailRequest.dontTransform()
    if ((listener != null || target != null) && !hasFaded) {
        setSaturation(0F)
        isEnabled = false
    }
    loadBitmap(requester, url, !transform, !hasFaded,
               if (listener != null || target != null) thumbnailRequest else null,
               if (listener != null || target != null) FadeAnimator() else null, listener, target)
}

fun ImageView.loadAvatar(requester:RequestManager?, url:String, shouldAnimate:Boolean) {
    loadBitmap(requester, url, false, shouldAnimate, null, FadeAnimator(), null,
               object:GlideTarget(this) {
                   override fun onLoadSucceed(resource:Bitmap) {
                       setImageDrawable(resource.createRoundedDrawable(context))
                   }
               })
}

private fun ImageView.loadBitmap(requester:RequestManager?,
                                 url:String, dontTransform:Boolean, shouldAnimate:Boolean,
                                 thumbnail:BitmapRequestBuilder<String, Bitmap>?,
                                 animator:ViewPropertyAnimation.Animator?,
                                 listener:GlideRequestListener<Bitmap>?,
                                 target:GlideTarget?) {
    val manager = requester ?: Glide.with(context)
    val builder = manager.load(url)
            .asBitmap()
            .priority(Priority.HIGH)
            .diskCacheStrategy(DiskCacheStrategy.SOURCE)
            .listener(listener)
    if (dontTransform) {
        builder.dontTransform()
    }
    if (!shouldAnimate) {
        builder.dontAnimate()
    }
    if (thumbnail != null) {
        builder.thumbnail(thumbnail)
    }
    if (animator != null) {
        builder.animate(animator)
    }
    if (target != null) {
        builder.into(target)
    } else {
        builder.into(this)
    }
}

fun ImageView.loadResource(requester:RequestManager?, resId:Int, dontTransform:Boolean,
                           shouldAnimate:Boolean, immediately:Boolean,
                           listener:GlideResourceRequestListener<GlideDrawable>?) {
    val manager = requester ?: Glide.with(context)
    val builder = manager.load(resId)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .listener(listener)
    if (dontTransform) {
        builder.dontTransform()
    }
    if (!shouldAnimate) {
        builder.dontAnimate()
    }
    if (immediately) {
        builder.priority(Priority.IMMEDIATE)
    } else {
        builder.priority(Priority.HIGH)
    }
    builder.into(this)
}