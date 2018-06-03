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
package jahirfiquitiva.libs.frames.helpers.extensions

import android.app.SharedElementCallback
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.view.View
import jahirfiquitiva.libs.frames.R
import jahirfiquitiva.libs.kext.extensions.bind
import jahirfiquitiva.libs.kext.extensions.boolean

fun AppCompatActivity.framesPostponeEnterTransition(onTransitionEnd: () -> Unit = {}) {
    supportPostponeEnterTransition()
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        val decor = window.decorView
        val statusBar: View? by decor.bind(android.R.id.statusBarBackground)
        val navBar: View? by decor.bind(android.R.id.navigationBarBackground)
        val actionBar: View? by decor.bind(R.id.action_bar_container)
        
        val viewsToExclude = arrayListOf(statusBar, navBar, actionBar).jfilter { it != null }
        val extraViewsToExclude = arrayListOf(R.id.appbar, R.id.toolbar)
        if (boolean(R.bool.isFrames)) extraViewsToExclude += R.id.tabs
        
        viewsToExclude.forEach { window.sharedElementEnterTransition?.excludeTarget(it, true) }
        extraViewsToExclude.forEach {
            try {
                findViewById<View?>(it)?.let {
                    window.sharedElementEnterTransition?.excludeTarget(it, true)
                }
            } catch (ignored: Exception) {
            }
        }
        
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
    }
}