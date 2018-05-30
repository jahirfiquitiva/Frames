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
package jahirfiquitiva.libs.frames.helpers.utils

import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target

abstract class GlideRequestCallback<Type> : RequestListener<Type> {
    abstract fun onLoadSucceed(resource: Type): Boolean
    open fun onLoadFailed(): Boolean = false
    
    override fun onResourceReady(
        resource: Type, model: Any?, target: Target<Type>?,
        dataSource: DataSource?, isFirstResource: Boolean
                                ): Boolean =
        onLoadSucceed(resource)
    
    override fun onLoadFailed(
        e: GlideException?, model: Any?, target: Target<Type>?,
        isFirstResource: Boolean
                             ): Boolean = onLoadFailed()
}