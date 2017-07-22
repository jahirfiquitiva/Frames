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

package jahirfiquitiva.libs.frames.views

import android.animation.Animator
import android.content.Context
import android.support.v7.widget.AppCompatImageView
import android.util.AttributeSet
import android.view.View
import android.widget.Checkable
import ca.allanwang.kau.utils.gone
import ca.allanwang.kau.utils.visible
import jahirfiquitiva.libs.frames.extensions.run

class CheckableImageView:AppCompatImageView, Checkable {

    private val CHECKED_STATE_SET = intArrayOf(android.R.attr.state_checked)
    private var internalIsChecked = false

    override fun isChecked():Boolean = internalIsChecked

    override fun toggle() {
        isChecked = !isChecked
    }

    override fun setChecked(check:Boolean) {
        if (isChecked != check) {
            internalIsChecked = check
            animateCheck()
        }
    }

    fun animateCheck() {
        animate().scaleX(0F).scaleY(0F).setListener(object:Animator.AnimatorListener {
            override fun onAnimationRepeat(p0:Animator?) {
                // Do nothing
            }

            override fun onAnimationCancel(p0:Animator?) {
                // Do nothing
            }

            override fun onAnimationStart(p0:Animator?) {
                // Do nothing
            }

            override fun onAnimationEnd(p0:Animator?) {
                refreshDrawableState()
                animate().scaleX(1F).scaleY(1F).start()
            }
        })
    }

    constructor(context:Context):super(context)
    constructor(context:Context, attributeSet:AttributeSet):super(context, attributeSet)
    constructor(context:Context, attributeSet:AttributeSet, defStyleAttr:Int)
            :super(context, attributeSet, defStyleAttr)

    override fun onCreateDrawableState(extraSpace:Int):IntArray {
        val state = super.onCreateDrawableState(extraSpace + 1)
        if (isChecked()) View.mergeDrawableStates(state, CHECKED_STATE_SET)
        return state
    }
}