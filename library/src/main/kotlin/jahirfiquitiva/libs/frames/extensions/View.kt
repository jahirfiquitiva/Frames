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

import android.app.Activity
import android.os.Build
import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.Snackbar
import android.view.View
import android.widget.ImageView
import ca.allanwang.kau.utils.dpToPx
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.BitmapImageViewTarget
import jahirfiquitiva.libs.frames.R
import jahirfiquitiva.libs.kauextensions.extensions.currentRotation
import jahirfiquitiva.libs.kauextensions.extensions.hasContent
import jahirfiquitiva.libs.kauextensions.extensions.isInPortraitMode

fun ImageView.loadFromUrls(url:String, thumbUrl:String, sizeMultiplier:Float = 0.5F) {
    if (thumbUrl.hasContent()) {
        Glide.with(context).load(url)
                .thumbnail(Glide.with(context).load(thumbUrl)
                                   .thumbnail(sizeMultiplier)
                                   .apply(RequestOptions().dontTransform()
                                                  .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                                                  .priority(Priority.IMMEDIATE)))
                .apply(RequestOptions().dontTransform()
                               .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                               .priority(Priority.IMMEDIATE))
                .into(this)
    } else if (url.hasContent()) {
        Glide.with(context).load(url).thumbnail(sizeMultiplier)
                .apply(RequestOptions().dontTransform()
                               .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                               .priority(Priority.HIGH)).into(this)
    }
}

fun ImageView.loadFromUrlsIntoTarget(url:String, thumbUrl:String, target:BitmapImageViewTarget,
                                     sizeMultiplier:Float = 0.5F) {
    if (thumbUrl.hasContent()) {
        Glide.with(context).asBitmap().load(url)
                .thumbnail(Glide.with(context).asBitmap().load(thumbUrl)
                                   .thumbnail(sizeMultiplier)
                                   .apply(RequestOptions()
                                                  .dontTransform()
                                                  .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                                                  .priority(Priority.IMMEDIATE)))
                .apply(RequestOptions().dontTransform()
                               .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                               .priority(Priority.HIGH))
                .into(target)
    } else if (url.hasContent()) {
        Glide.with(context).asBitmap().load(url).thumbnail(sizeMultiplier)
                .apply(RequestOptions().dontTransform().diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                               .priority(Priority.HIGH))
                .into(target)
    }
}

fun View.setNavBarMargins() {
    val params = (layoutParams as? CoordinatorLayout.LayoutParams) ?: return
    val left = if (this is FloatingActionButton) 16.dpToPx else 0
    var right = if (this is FloatingActionButton) 16.dpToPx else 0
    var bottom = if (this is FloatingActionButton) 16.dpToPx else 0
    val top = if (this is FloatingActionButton) 16.dpToPx else 0
    var bottomNavBar = 0
    var sideNavBar = 0
    
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        val tabletMode = context.resources.getBoolean(R.bool.md_is_tablet)
        if (tabletMode || context.isInPortraitMode) {
            bottomNavBar = (context as? Activity)?.navigationBarHeight ?: 0
        } else {
            sideNavBar = (context as? Activity)?.navigationBarHeight ?: 0
        }
    }
    
    val navBar = (context as? Activity)?.navigationBarHeight ?: 0
    if (bottom > bottomNavBar && bottom - navBar > 0) bottom -= navBar
    if (right > sideNavBar && right - navBar > 0) right -= navBar
    
    var extraLeft = 0
    var extraRight = 0
    if (context.currentRotation == 90) extraRight = sideNavBar
    else if (context.currentRotation == 270) extraLeft = sideNavBar
    
    params.setMargins(left + extraLeft, top, right + extraRight, bottom + bottomNavBar)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
        params.marginEnd = right + extraRight
    }
    layoutParams = params
    requestLayout()
}

fun View.buildSnackbar(text:String, duration:Int = Snackbar.LENGTH_LONG,
                       builder:Snackbar.() -> Unit = {}):Snackbar {
    val snackbar = Snackbar.make(this, text, duration)
    snackbar.builder()
    return snackbar
}