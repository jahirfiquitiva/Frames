/*
 * Copyright (c) 2018. Jahir Fiquitiva
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
package jahirfiquitiva.libs.frames.helpers.glide

import android.content.Context
import android.graphics.drawable.Drawable
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.NoTransition
import jahirfiquitiva.libs.frames.helpers.extensions.doOnLayout
import jahirfiquitiva.libs.frames.helpers.utils.FramesKonfigs
import jahirfiquitiva.libs.kext.extensions.isLowRamDevice

fun RequestManager.loadPicture(
    url: String,
    thumbnail: String = url,
    placeholder: Drawable? = null,
    context: Context? = null,
    fitCenter: Boolean = false,
    circular: Boolean = false,
    withTransition: Boolean = true,
    forceNow: Boolean = false,
    listener: RequestListener<Drawable>? = null
                              ): RequestBuilder<*>? {
    val finalUrl =
        context?.let { if (FramesKonfigs(it).fullResGridPictures) url else thumbnail } ?: thumbnail
    var options = RequestOptions()
        .format(
            if (context == null || context.isLowRamDevice) DecodeFormat.PREFER_RGB_565
            else DecodeFormat.PREFER_ARGB_8888)
        .disallowHardwareConfig()
        .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
        .timeout(5000)
        .placeholder(placeholder)
        .fallback(placeholder)
        .error(placeholder)
    
    if (fitCenter) options = options.optionalFitCenter()
    if (circular) options = options.optionalCircleCrop()
    
    val transition = if (context == null || context.isLowRamDevice) {
        DrawableTransitionOptions.with { _, _ -> NoTransition<Drawable>() }
    } else if (withTransition) {
        DrawableTransitionOptions.with(SaturationTransitionFactory())
    } else {
        DrawableTransitionOptions.withCrossFade(750)
    }
    
    val thumbnailRequest: RequestBuilder<Drawable>? = if (finalUrl != thumbnail) {
        load(thumbnail)
            .apply(options.priority(Priority.IMMEDIATE))
            .transition(transition)
            .listener(listener)
    } else null
    
    return load(url)
        .apply(options.priority(if (forceNow) Priority.IMMEDIATE else Priority.HIGH))
        .transition(transition)
        .listener(listener)
        .thumbnail(thumbnailRequest)
}

fun ImageView.loadPicture(
    manager: RequestManager? = Glide.with(this),
    url: String,
    thumbnail: String = url,
    placeholder: Drawable? = null,
    fitCenter: Boolean = false,
    circular: Boolean = false,
    withTransition: Boolean = true,
    forceNow: Boolean = false,
    listener: RequestListener<Drawable>? = null
                         ) {
    
    doOnLayout {
        manager
            ?.loadPicture(
                url, thumbnail, placeholder ?: drawable, context, fitCenter, circular,
                withTransition, forceNow, listener)
            ?.into(this)
    }
}

fun ImageView.preloadPicture(
    manager: RequestManager? = Glide.with(this),
    url: String,
    thumbnail: String = url
                            ) {
    var gWidth = width
    if (gWidth <= 0) gWidth = measuredWidth
    if (gWidth <= 0) gWidth = minimumWidth
    if (gWidth <= 0) gWidth = Target.SIZE_ORIGINAL
    
    var gHeight = height
    if (gHeight <= 0) gHeight = measuredHeight
    if (gHeight <= 0) gHeight = minimumHeight
    if (gHeight <= 0) gHeight = Target.SIZE_ORIGINAL
    
    manager
        ?.loadPicture(url, thumbnail, drawable, withTransition = false, forceNow = true)
        ?.preload(gWidth, gHeight)
}

fun ImageView.clearFromGlide() {
    Glide.with(this).clear(this)
}