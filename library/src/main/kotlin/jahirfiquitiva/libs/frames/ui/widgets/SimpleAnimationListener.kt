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

package jahirfiquitiva.libs.frames.ui.widgets

import android.view.animation.Animation

abstract class SimpleAnimationListener:Animation.AnimationListener {
    open fun onStart(animation:Animation) = Unit
    open fun onEnd(animation:Animation) = Unit
    open fun onRepeat(animation:Animation) = Unit
    
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