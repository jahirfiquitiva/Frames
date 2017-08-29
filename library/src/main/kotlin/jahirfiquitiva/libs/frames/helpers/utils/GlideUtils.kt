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
package jahirfiquitiva.libs.frames.helpers.utils

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.widget.ImageView
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.animation.GlideAnimation
import com.bumptech.glide.request.target.BitmapImageViewTarget
import com.bumptech.glide.request.target.Target
import java.lang.Exception

abstract class GlideRequestListener<Type>:RequestListener<String, Type> {
    abstract fun onLoadSucceed(resource:Type):Boolean
    open fun onLoadFailed():Boolean = false
    
    override fun onResourceReady(resource:Type?, model:String?, target:Target<Type>?,
                                 isFromMemoryCache:Boolean, isFirstResource:Boolean):Boolean {
        if (resource != null) {
            return onLoadSucceed(resource)
        }
        return false
    }
    
    
    override fun onException(e:Exception?, model:String?, target:Target<Type>?,
                             isFirstResource:Boolean):Boolean = onLoadFailed()
}

abstract class GlideResourceRequestListener<Type>:RequestListener<Int, Type> {
    abstract fun onLoadSucceed(resource:Type):Boolean
    open fun onLoadFailed():Boolean = false
    
    override fun onResourceReady(resource:Type, model:Int?, target:Target<Type>?,
                                 isFromMemoryCache:Boolean, isFirstResource:Boolean):Boolean =
            onLoadSucceed(resource)
    
    override fun onException(e:Exception?, model:Int?, target:Target<Type>?,
                             isFirstResource:Boolean):Boolean = onLoadFailed()
}

abstract class GlideTarget(view:ImageView):BitmapImageViewTarget(view) {
    abstract fun onLoadSucceed(resource:Bitmap)
    open fun onLoadFailed(errorDrawable:Drawable) {}
    
    override fun onResourceReady(resource:Bitmap?, glideAnimation:GlideAnimation<in Bitmap>?) {
        resource?.let {
            onLoadSucceed(it)
        }
    }
    
    override fun onLoadFailed(e:Exception?, errorDrawable:Drawable?) {
        super.onLoadFailed(e, errorDrawable)
        errorDrawable?.let {
            onLoadFailed(it)
        }
    }
}