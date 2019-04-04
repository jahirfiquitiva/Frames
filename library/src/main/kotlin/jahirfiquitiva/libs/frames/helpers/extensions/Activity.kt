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
import android.view.View
import androidx.core.util.Pair
import androidx.core.view.ViewCompat
import androidx.fragment.app.FragmentActivity
import jahirfiquitiva.libs.frames.R

fun FragmentActivity.framesPostponeEnterTransition(
    onTransitionEnd: () -> Unit = {},
    onTransitionStart: () -> Unit = {}
                                                  ) {
    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
        supportPostponeEnterTransition()
        
        concatSharedElements().forEach {
            window?.sharedElementEnterTransition?.excludeTarget(it?.first, true)
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
            
            override fun onSharedElementStart(
                sharedElementNames: MutableList<String>?,
                sharedElements: MutableList<View>?,
                sharedElementSnapshots: MutableList<View>?
                                             ) {
                super.onSharedElementStart(
                    sharedElementNames, sharedElements, sharedElementSnapshots)
                onTransitionStart()
            }
        })
    } else onTransitionEnd()
}

fun FragmentActivity.safeStartPostponedEnterTransition() {
    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP)
        supportStartPostponedEnterTransition()
}

fun FragmentActivity.concatSharedElements(vararg activitySharedElements: Pair<View, String>):
    Array<Pair<View, String>?> {
    
    val sharedElements = ArrayList<Pair<View, String>>()
    sharedElements.addAll(activitySharedElements)
    
    val decor = window?.decorView ?: return arrayOfNulls(sharedElements.size)
    
    val views = arrayListOf(
        decor.findViewById<View?>(R.id.action_bar_container),
        decor.findViewById<View?>(R.id.appbar),
        decor.findViewById<View?>(R.id.toolbar),
        decor.findViewById<View?>(R.id.tabs))
    
    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
        views.add(decor.findViewById<View?>(android.R.id.statusBarBackground))
        views.add(decor.findViewById<View?>(android.R.id.navigationBarBackground))
    }
    
    views.jfilter { it != null }
        .forEach {
            it?.let { sharedElements.add(Pair(it, ViewCompat.getTransitionName(it) ?: "")) }
        }
    
    return sharedElements.toTypedArray()
}
