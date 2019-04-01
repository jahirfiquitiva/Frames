/*
 * Copyright (c) 2019. Jahir Fiquitiva
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

import android.app.Activity
import android.os.Build
import android.view.View
import android.widget.FrameLayout
import androidx.core.view.ViewCompat
import ca.allanwang.kau.utils.dpToPx
import com.google.android.material.floatingactionbutton.FloatingActionButton
import jahirfiquitiva.libs.frames.R
import jahirfiquitiva.libs.kext.extensions.currentRotation
import jahirfiquitiva.libs.kext.extensions.isInPortraitMode
import jahirfiquitiva.libs.kext.extensions.navigationBarHeight

fun View.setNavBarMargins(): View {
    val params = layoutParams as? FrameLayout.LayoutParams ?: return this
    val left = if (this is FloatingActionButton) 16.dpToPx else 0
    var right = if (this is FloatingActionButton) 16.dpToPx else 0
    var bottom = if (this is FloatingActionButton) 16.dpToPx else 0
    val top = if (this is FloatingActionButton) 16.dpToPx else 0
    var bottomNavBar = 0
    var sideNavBar = 0
    
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        val tabletMode = context.resources.getBoolean(R.bool.isTablet)
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
    return this
}

/**
 * Performs the given action when this view is next laid out.
 *
 * @see doOnLayout
 */
inline fun View.doOnNextLayout(crossinline action: (view: View) -> Unit) {
    addOnLayoutChangeListener(object : View.OnLayoutChangeListener {
        override fun onLayoutChange(
            view: View,
            left: Int,
            top: Int,
            right: Int,
            bottom: Int,
            oldLeft: Int,
            oldTop: Int,
            oldRight: Int,
            oldBottom: Int
                                   ) {
            view.removeOnLayoutChangeListener(this)
            action(view)
        }
    })
}

/**
 * Performs the given action when this view is laid out. If the view has been laid out and it
 * has not requested a layout, the action will be performed straight away, otherwise the
 * action will be performed after the view is next laid out.
 *
 * @see doOnNextLayout
 */
inline fun View.doOnLayout(crossinline action: (view: View) -> Unit) {
    if (ViewCompat.isLaidOut(this) && !isLayoutRequested) {
        action(this)
    } else {
        doOnNextLayout {
            action(it)
        }
    }
}
