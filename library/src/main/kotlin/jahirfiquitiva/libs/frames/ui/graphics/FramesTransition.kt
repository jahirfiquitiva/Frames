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

import android.annotation.TargetApi
import android.os.Build
import android.transition.ChangeBounds
import android.transition.ChangeImageTransform
import android.transition.ChangeTransform
import android.transition.TransitionSet
import android.view.View

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
class FramesTransition(vararg toExclude:View):TransitionSet() {
    init {
        duration = 300
        ordering = ORDERING_TOGETHER
        toExclude.forEach {
            excludeTarget(it, true)
        }
        addTransition(ChangeBounds()).addTransition(ChangeTransform())
                .addTransition(ChangeImageTransform())
    }
}