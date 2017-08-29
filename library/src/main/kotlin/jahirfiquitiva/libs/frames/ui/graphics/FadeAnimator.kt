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
package jahirfiquitiva.libs.frames.ui.graphics

import android.animation.ObjectAnimator
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import com.bumptech.glide.request.animation.ViewPropertyAnimation

/**
 * Credits to Mysplash
 * @author WangDaYeeeeee
 * https://goo.gl/rDDqTZ
 */
class FadeAnimator:ViewPropertyAnimation.Animator {
    override fun animate(view:View?) {
        view?.let {
            if (!it.isEnabled) {
                it.isEnabled = true
                val animator = ObjectAnimator.ofFloat(view, "alpha", 0f, 1f)
                animator.duration = 250
                animator.interpolator = AccelerateDecelerateInterpolator()
                animator.start()
            }
        }
    }
}