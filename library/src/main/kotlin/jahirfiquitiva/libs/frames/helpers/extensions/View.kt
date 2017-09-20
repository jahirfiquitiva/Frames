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
package jahirfiquitiva.libs.frames.helpers.extensions

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.app.Activity
import android.graphics.ColorMatrixColorFilter
import android.os.Build
import android.support.annotation.StringRes
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.Snackbar
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.FrameLayout
import android.widget.ImageView
import ca.allanwang.kau.utils.dpToPx
import jahirfiquitiva.libs.frames.R
import jahirfiquitiva.libs.frames.ui.graphics.ObservableColorMatrix
import jahirfiquitiva.libs.kauextensions.extensions.currentRotation
import jahirfiquitiva.libs.kauextensions.extensions.isInPortraitMode

fun View.setNavBarMargins() {
    val params = (layoutParams as? FrameLayout.LayoutParams) ?: return
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

fun View.buildSnackbar(@StringRes text:Int, duration:Int = Snackbar.LENGTH_LONG,
                       builder:Snackbar.() -> Unit = {}):Snackbar {
    val snackbar = Snackbar.make(this, text, duration)
    snackbar.builder()
    return snackbar
}

fun View.buildSnackbar(text:String, duration:Int = Snackbar.LENGTH_LONG,
                       builder:Snackbar.() -> Unit = {}):Snackbar {
    val snackbar = Snackbar.make(this, text, duration)
    snackbar.builder()
    return snackbar
}

/**
 * Credits to Mysplash
 * https://goo.gl/M2sqE2
 */
fun ImageView.animateColorTransition(onFaded:() -> Unit = {}) {
    setHasTransientState(true)
    val matrix = ObservableColorMatrix()
    val saturation = ObjectAnimator.ofFloat(matrix, ObservableColorMatrix.SATURATION, 0F, 1F)
    saturation.addUpdateListener {
        colorFilter = ColorMatrixColorFilter(matrix)
    }
    saturation.duration = 1500L
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        saturation.interpolator = AnimationUtils.loadInterpolator(context,
                                                                  android.R.interpolator.fast_out_slow_in)
    }
    saturation.addListener(object:AnimatorListenerAdapter() {
        override fun onAnimationEnd(animation:Animator?) {
            super.onAnimationEnd(animation)
            clearColorFilter()
            setHasTransientState(false)
        }
    })
    saturation.start()
    onFaded()
}

fun View.clearChildrenAnimations() {
    clearAnimation()
    getAllChildren(this).forEach { it.clearAnimation() }
}

private fun getAllChildren(v:View):ArrayList<View> {
    if (v !is ViewGroup) {
        val viewArrayList = ArrayList<View>()
        viewArrayList.add(v)
        return viewArrayList
    }
    val result = ArrayList<View>()
    for (i in 0 until v.childCount) {
        val child = v.getChildAt(i)
        val viewArrayList = ArrayList<View>()
        viewArrayList.add(v)
        viewArrayList.addAll(getAllChildren(child))
        result.addAll(viewArrayList)
    }
    return result
}