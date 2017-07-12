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

package jahirfiquitiva.libs.frames.extensions

import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.engine.DiskCacheStrategy

fun ImageView.loadFromUrls(url:String, thumbUrl:String) {
    if (thumbUrl.isNotEmpty())
        Glide.with(context).load(url).diskCacheStrategy(DiskCacheStrategy.SOURCE).thumbnail(
                Glide.with(context).load(thumbUrl).priority(Priority.IMMEDIATE).thumbnail(0.5F))
                .priority(Priority.HIGH).into(this)
    else
        Glide.with(context).load(url).diskCacheStrategy(DiskCacheStrategy.SOURCE).thumbnail(0.5F)
                .priority(Priority.HIGH).into(this)
}