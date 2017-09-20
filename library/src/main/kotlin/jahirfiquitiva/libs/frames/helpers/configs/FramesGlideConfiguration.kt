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
package jahirfiquitiva.libs.frames.helpers.configs

import android.content.Context
import com.bumptech.glide.Glide
import com.bumptech.glide.GlideBuilder
import com.bumptech.glide.Registry
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.module.AppGlideModule
import com.bumptech.glide.request.RequestOptions
import jahirfiquitiva.libs.frames.helpers.extensions.isLowRamDevice

@GlideModule
open class FramesGlideConfiguration:AppGlideModule() {
    override fun applyOptions(context:Context?, builder:GlideBuilder?) {
        context?.let {
            val options = RequestOptions()
                    .format(if (it.isLowRamDevice) DecodeFormat.PREFER_RGB_565
                            else DecodeFormat.PREFER_ARGB_8888)
                    .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                    .disallowHardwareConfig()
            builder?.setDefaultRequestOptions(options)
        }
    }
    
    override fun isManifestParsingEnabled():Boolean = false
    override fun registerComponents(context:Context?, glide:Glide?, registry:Registry?) {
        super.registerComponents(context, glide, registry)
    }
}