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

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.graphics.ColorMatrixColorFilter
import android.graphics.drawable.Drawable
import android.support.v4.view.animation.FastOutSlowInInterpolator
import android.view.View
import jahirfiquitiva.libs.kext.extensions.SimpleAnimatorListener
import kotlin.math.roundToLong

/**
 * Credits: https://github.com/chrisbanes/tivi/
 */
private val fastOutSlowInInterpolator = FastOutSlowInInterpolator()

fun saturateDrawableAnimator(current: Drawable, view: View): Animator {
    view.setHasTransientState(true)
    val cm = ImageLoadingColorMatrix()
    
    val duration = 1500L
    
    val satAnim = ObjectAnimator.ofFloat(cm, ImageLoadingColorMatrix.PROP_SATURATION, 0f, 1f)
    satAnim.duration = duration
    satAnim.interpolator = fastOutSlowInInterpolator
    satAnim.addUpdateListener { current.colorFilter = ColorMatrixColorFilter(cm) }
    
    val alphaAnim = ObjectAnimator.ofFloat(cm, ImageLoadingColorMatrix.PROP_ALPHA, 0f, 1f)
    alphaAnim.duration = duration / 2
    alphaAnim.interpolator = fastOutSlowInInterpolator
    
    val darkenAnim = ObjectAnimator.ofFloat(cm, ImageLoadingColorMatrix.PROP_DARKEN, 0f, 1f)
    darkenAnim.duration = (duration * 0.75f).roundToLong()
    darkenAnim.interpolator = fastOutSlowInInterpolator
    
    val set = AnimatorSet()
    set.playTogether(satAnim, alphaAnim, darkenAnim)
    set.addListener(object : SimpleAnimatorListener() {
        override fun onEnd(animator: Animator) {
            current.clearColorFilter()
            view.setHasTransientState(false)
        }
    })
    return set
}