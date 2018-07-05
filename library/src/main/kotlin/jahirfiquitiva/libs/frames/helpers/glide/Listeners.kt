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

import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.support.v7.graphics.Palette
import android.util.LruCache
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import jahirfiquitiva.libs.frames.helpers.utils.FL

abstract class FramesGlideListener<Type> : RequestListener<Type> {
    abstract fun onLoadSucceed(resource: Type, model: Any?): Boolean
    open fun onLoadFailed(): Boolean = false
    
    override fun onResourceReady(
        resource: Type, model: Any?, target: Target<Type>?,
        dataSource: DataSource?, isFirstResource: Boolean
                                ): Boolean =
        onLoadSucceed(resource, model)
    
    override fun onLoadFailed(
        e: GlideException?, model: Any?, target: Target<Type>?,
        isFirstResource: Boolean
                             ): Boolean {
        FL.e("Glide Exception", e)
        return onLoadFailed()
    }
}

/**
 * Credits: https://github.com/chrisbanes/tivi/
 */
abstract class GlidePaletteListener : FramesGlideListener<Drawable>() {
    
    companion object {
        private val cache = LruCache<Any, Palette>(20)
        private val cacheLock = Any()
    }
    
    @Suppress("SENSELESS_COMPARISON")
    override fun onLoadSucceed(resource: Drawable, model: Any?): Boolean {
        // First check the cache
        synchronized(cacheLock) {
            val cached = model?.let { cache[it] }
            if (cached != null) {
                // If the cache has a result now, use it
                onPaletteReady(cached)
                // We don't want to handle updating the target
                return false
            }
        }
        
        if (resource is BitmapDrawable) {
            val bitmap = resource.bitmap
            Palette.Builder(bitmap)
                .clearTargets()
                .maximumColorCount(4)
                .setRegion(0, Math.round(bitmap.height * 0.9f), bitmap.width, bitmap.height)
                .generate { palette ->
                    synchronized(cacheLock) {
                        val cached = model?.let { cache[it] }
                        if (cached != null) {
                            // If the cache has a result now, just return it to maintain equality
                            onPaletteReady(cached)
                        } else if (palette != null) {
                            // Else we'll save the newly generated one
                            cache.put(model, palette)
                            // Now invoke the listener
                            onPaletteReady(palette)
                        }
                    }
                }
        }
        
        // We don't want to handle updating the target
        return false
    }
    
    abstract fun onPaletteReady(palette: Palette?)
}