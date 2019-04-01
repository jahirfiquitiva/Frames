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

import android.app.SharedElementCallback
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import jahirfiquitiva.libs.frames.R

fun AppCompatActivity.framesPostponeEnterTransition(onTransitionEnd: () -> Unit = {}) {
    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
        supportPostponeEnterTransition()
        val decor = window?.decorView
        
        val views = arrayListOf(
            decor?.findViewById<View?>(android.R.id.statusBarBackground),
            decor?.findViewById<View?>(android.R.id.navigationBarBackground),
            decor?.findViewById<View?>(R.id.action_bar_container),
            decor?.findViewById<View?>(R.id.appbar),
            decor?.findViewById<View?>(R.id.toolbar),
            decor?.findViewById<View?>(R.id.tabs))
        
        views.jfilter { it != null }
            .forEach { window?.sharedElementEnterTransition?.excludeTarget(it, true) }
        
        setEnterSharedElementCallback(object : SharedElementCallback() {
            override fun onSharedElementEnd(
                sharedElementNames: MutableList<String>?,
                sharedElements: MutableList<View>?,
                sharedElementSnapshots: MutableList<View>?
                                           ) {
                super.onSharedElementEnd(sharedElementNames, sharedElements, sharedElementSnapshots)
                onTransitionEnd()
            }
        })
    } else onTransitionEnd()
}

fun AppCompatActivity.safeStartPostponedEnterTransition() {
    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP)
        supportStartPostponedEnterTransition()
}
