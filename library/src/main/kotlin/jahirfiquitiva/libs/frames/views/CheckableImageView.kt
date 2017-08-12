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

import android.content.Context
import android.support.v7.widget.AppCompatImageView
import android.util.AttributeSet
import android.view.animation.Animation
import android.widget.Checkable

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
            refreshDrawableState()
        }
    }
    
    constructor(context:Context):super(context)
    constructor(context:Context, attributeSet:AttributeSet):super(context, attributeSet)
    constructor(context:Context, attributeSet:AttributeSet, defStyleAttr:Int)
            :super(context, attributeSet, defStyleAttr)
    
    override fun onCreateDrawableState(extraSpace:Int):IntArray {
        val state = super.onCreateDrawableState(extraSpace + 1)
        if (isChecked) mergeDrawableStates(state, CHECKED_STATE_SET)
        return state
    }
}

abstract class SimpleAnimationListener:Animation.AnimationListener {
    open fun onStart(animation:Animation) {}
    open fun onEnd(animation:Animation) {}
    open fun onRepeat(animation:Animation) {}
    
    override fun onAnimationRepeat(animation:Animation?) {
        animation?.let { onRepeat(it) }
    }
    
    override fun onAnimationEnd(animation:Animation?) {
        animation?.let { onEnd(it) }
    }
    
    override fun onAnimationStart(animation:Animation?) {
        animation?.let { onStart(it) }
    }
}