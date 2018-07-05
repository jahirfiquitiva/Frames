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

import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import jahirfiquitiva.libs.frames.helpers.utils.FramesKonfigs
import jahirfiquitiva.libs.kext.extensions.isLowRamDevice

fun RequestManager.loadPic(
    url: String,
    now: Boolean = false,
    lowRam: Boolean = true,
    listener: RequestListener<Drawable>? = null,
    placeholder: Drawable? = null,
    fitCenter: Boolean = false,
    circleCrop: Boolean = false
                          ): RequestBuilder<Drawable>? {
    var options = RequestOptions()
        .format(if (lowRam) DecodeFormat.PREFER_RGB_565 else DecodeFormat.PREFER_ARGB_8888)
        .disallowHardwareConfig()
        .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
        .priority(if (now) Priority.IMMEDIATE else Priority.HIGH)
        .timeout(10000)
        .placeholder(placeholder)
        .error(placeholder)
    
    if (fitCenter) options = options.fitCenter()
    if (circleCrop) options = options.circleCrop()
    
    return load(url)
        .apply(options)
        .transition(
            if (lowRam) withCrossFade(250)
            else DrawableTransitionOptions.with(SaturationTransitionFactory()))
        .listener(listener)
}

fun ImageView.releaseFromGlide() {
    Glide.with(context).clear(this)
}

@SuppressLint("CheckResult")
fun ImageView.loadWallpaper(
    requester: RequestManager?,
    url: String,
    thumbUrl: String,
    listener: RequestListener<Drawable>?
                           ) {
    
    val manager = requester ?: Glide.with(context)
    val loadFullRes = FramesKonfigs(context).fullResGridPictures
    val isLowRam = context.isLowRamDevice
    
    if (loadFullRes) {
        val placeholder: RequestBuilder<Drawable>? = if (!thumbUrl.equals(url, true)) {
            manager.loadPic(thumbUrl, true, isLowRam, listener)
        } else null
        
        manager.loadPic(url, false, isLowRam, listener)
            ?.thumbnail(placeholder)
            ?.into(this)
    } else {
        manager.loadPic(thumbUrl, true, isLowRam, listener)?.into(this)
    }
}

fun ImageView.loadAvatar(requester: RequestManager?, url: String) {
    val manager = requester ?: Glide.with(context)
    manager.loadPic(url, false, context.isLowRamDevice, null, null, false, true)?.into(this)
}