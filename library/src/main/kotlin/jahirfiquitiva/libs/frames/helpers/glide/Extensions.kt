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
import com.bumptech.glide.integration.webp.decoder.WebpDrawable
import com.bumptech.glide.integration.webp.decoder.WebpDrawableTransformation
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.load.resource.bitmap.FitCenter
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.transition.NoTransition
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
    listener: RequestListener<Drawable>? = null
                              ): RequestBuilder<*>? {
    return try {
        val finalUrl =
            context?.let { if (FramesKonfigs(it).fullResGridPictures) url else thumbnail }
                ?: thumbnail
        var options = RequestOptions()
            .format(
                if (context == null || context.isLowRamDevice) DecodeFormat.PREFER_RGB_565
                else DecodeFormat.PREFER_ARGB_8888)
            .disallowHardwareConfig()
            .timeout(10000)
            .placeholder(placeholder)
            .fallback(placeholder)
            .error(placeholder)
        
        if (fitCenter) options = options.optionalFitCenter()
        if (circular) options = options.optionalCircleCrop()
        
        try {
            if (url.endsWith("webp", true) || thumbnail.endsWith("webp", true)) {
                @Suppress("CascadeIf")
                val transformation: BitmapTransformation? = if (fitCenter) FitCenter()
                else if (circular) CircleCrop()
                else null
                options = options.optionalTransform(
                    WebpDrawable::class.java, WebpDrawableTransformation(transformation))
            }
        } catch (e: Exception) {
        }
        
        val transition = if (context == null || context.isLowRamDevice) {
            DrawableTransitionOptions.with { _, _ -> NoTransition<Drawable>() }
        } else if (withTransition) {
            DrawableTransitionOptions.with(SaturationTransitionFactory())
        } else {
            DrawableTransitionOptions.withCrossFade(750)
        }
        
        val thumbnailRequest: RequestBuilder<Drawable>? = if (finalUrl != thumbnail) {
            load(thumbnail).apply(options.priority(Priority.HIGH))
        } else null
        
        val request: RequestBuilder<Drawable>? = load(url)
            .apply(options)
            .thumbnail(thumbnailRequest?.transition(transition)?.listener(listener))
        request?.transition(transition)?.listener(listener)
    } catch (e: Exception) {
        null
    }
}

fun ImageView.loadPicture(
    manager: RequestManager? = Glide.with(this),
    url: String,
    thumbnail: String = url,
    placeholder: Drawable? = null,
    fitCenter: Boolean = false,
    circular: Boolean = false,
    withTransition: Boolean = true,
    listener: RequestListener<Drawable>? = null
                         ) {
    
    manager
        ?.loadPicture(
            url, thumbnail, placeholder ?: drawable, context, fitCenter, circular,
            withTransition, listener)
        ?.into(this)
}