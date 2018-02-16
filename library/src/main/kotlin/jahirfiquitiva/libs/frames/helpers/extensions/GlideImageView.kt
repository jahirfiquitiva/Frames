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
package jahirfiquitiva.libs.frames.helpers.extensions

import android.annotation.SuppressLint
import android.graphics.ColorMatrixColorFilter
import android.graphics.drawable.Drawable
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import jahirfiquitiva.libs.kauextensions.extensions.isLowRamDevice
import jahirfiquitiva.libs.kauextensions.ui.graphics.ObservableColorMatrix

fun ImageView.releaseFromGlide() {
    Glide.with(context).clear(this)
}

fun ImageView.setSaturation(saturation: Float) {
    val matrix = ObservableColorMatrix()
    matrix.setSaturation(saturation)
    colorFilter = ColorMatrixColorFilter(matrix)
}

@SuppressLint("CheckResult")
fun ImageView.loadWallpaper(
        requester: RequestManager?,
        url: String,
        thumbUrl: String,
        hasFaded: Boolean,
        listener: RequestListener<Drawable>?
                           ) {
    
    val manager = requester ?: Glide.with(context)
    val loadFullRes = context.framesKonfigs.fullResGridPictures
    val baseOptions = RequestOptions()
            .format(
                    if (context.isLowRamDevice) DecodeFormat.PREFER_RGB_565
                    else DecodeFormat.PREFER_ARGB_8888)
            .disallowHardwareConfig()
    
    if (loadFullRes) {
        val placeholder: RequestBuilder<Drawable>? = if (!thumbUrl.equals(url, true)) {
            manager.load(thumbUrl)
                    .apply(
                            baseOptions
                                    .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                                    .priority(Priority.IMMEDIATE))
                    .transition(withCrossFade(if (hasFaded) 100 else 300))
                    .listener(listener)
        } else null
        
        manager.load(url)
                .apply(
                        baseOptions
                                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                                .priority(Priority.HIGH))
                .transition(withCrossFade(if (hasFaded) 100 else 300))
                .thumbnail(placeholder)
                .listener(listener)
                .into(this)
    } else {
        manager.load(thumbUrl)
                .apply(
                        baseOptions
                                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                                .priority(Priority.IMMEDIATE))
                .transition(withCrossFade(if (hasFaded) 100 else 300))
                .listener(listener)
                .into(this)
    }
}

fun ImageView.loadAvatar(requester: RequestManager?, url: String) {
    val manager = requester ?: Glide.with(context)
    val baseOptions = RequestOptions()
            .format(
                    if (context.isLowRamDevice) DecodeFormat.PREFER_RGB_565
                    else DecodeFormat.PREFER_ARGB_8888)
            .disallowHardwareConfig()
    manager.load(url)
            .apply(
                    baseOptions
                            .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                            .priority(Priority.HIGH)
                            .circleCrop())
            .transition(withCrossFade())
            .into(this)
}