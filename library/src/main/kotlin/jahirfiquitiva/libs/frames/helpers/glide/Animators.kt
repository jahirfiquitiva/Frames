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
import android.animation.ObjectAnimator
import android.graphics.ColorMatrixColorFilter
import android.graphics.drawable.Drawable
import android.support.v4.view.animation.FastOutSlowInInterpolator
import android.view.View
import jahirfiquitiva.libs.kext.extensions.SimpleAnimatorListener

/**
 * Credits: https://github.com/chrisbanes/tivi/
 */
fun saturateDrawableAnimator(current: Drawable, view: View): Animator {
    view.setHasTransientState(true)
    val cm = TiviColorMatrix()
    val animator =
        ObjectAnimator.ofFloat(cm, TiviColorMatrix.PROP_SATURATION, 0F, 1F)
    animator.duration = 1000L
    animator.interpolator = FastOutSlowInInterpolator()
    animator.addUpdateListener { current.colorFilter = ColorMatrixColorFilter(cm) }
    animator.addListener(object : SimpleAnimatorListener() {
        override fun onEnd(animator: Animator) {
            current.clearColorFilter()
            view.setHasTransientState(false)
        }
        
        override fun onAnimationCancel(animation: Animator?) {
            animation?.let { onEnd(it) }
        }
    })
    return animator
}