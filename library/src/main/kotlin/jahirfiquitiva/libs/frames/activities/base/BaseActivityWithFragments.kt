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
package jahirfiquitiva.libs.frames.activities.base

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import jahirfiquitiva.libs.frames.R
import jahirfiquitiva.libs.frames.extensions.framesKonfigs
import jahirfiquitiva.libs.kauextensions.activities.ThemedActivity

abstract class BaseActivityWithFragments:ThemedActivity() {
    
    abstract fun hasBottomBar():Boolean
    abstract fun fragmentsContainer():Int
    override fun autoStatusBarTint():Boolean = true
    
    fun changeFragment(f:Fragment, tag:String? = null) {
        try {
            val manager = supportFragmentManager.beginTransaction()
            if (hasBottomBar()) clearBackStack()
            if (framesKonfigs.animationsEnabled)
                manager.setCustomAnimations(R.anim.abc_fade_in, R.anim.abc_fade_out,
                                            R.anim.abc_popup_enter, R.anim.abc_popup_exit)
            if (tag != null) manager.replace(fragmentsContainer(), f, tag)
            else manager.replace(fragmentsContainer(), f)
            if (!hasBottomBar()) manager.addToBackStack(null)
            manager.commit()
        } catch (ignored:Exception) {
        }
    }
    
    fun clearBackStack() {
        try {
            val manager = supportFragmentManager
            if (manager.backStackEntryCount > 0) {
                val first = manager.getBackStackEntryAt(0)
                manager.popBackStack(first.id, FragmentManager.POP_BACK_STACK_INCLUSIVE)
            }
        } catch (e:Exception) {
            e.printStackTrace()
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        clearBackStack()
    }
}