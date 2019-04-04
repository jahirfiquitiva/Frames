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
package jahirfiquitiva.libs.frames.helpers.utils

import android.annotation.SuppressLint
import android.transition.Transition

@SuppressLint("NewApi")
abstract class TransitionCallback : Transition.TransitionListener {
    override fun onTransitionStart(transition: Transition?) {}
    override fun onTransitionPause(transition: Transition?) {}
    override fun onTransitionResume(transition: Transition?) {}
    override fun onTransitionEnd(transition: Transition?) {}
    override fun onTransitionCancel(transition: Transition?) {}
}
