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
package jahirfiquitiva.libs.frames.ui.fragments.adapters

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import android.support.v4.view.PagerAdapter

class FragmentsAdapter(manager: FragmentManager, vararg fragments: Fragment) :
        FragmentStatePagerAdapter(manager) {
    
    val fragments = ArrayList<Fragment?>()
    
    init {
        this.fragments.clear()
        this.fragments.addAll(fragments)
    }
    
    override fun getItemPosition(obj: Any?): Int {
        val index = fragments.indexOf(obj)
        return if (index < 0) PagerAdapter.POSITION_NONE
        else index
    }
    
    override fun getItem(position: Int): Fragment? {
        return try {
            fragments[position]
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    fun addFragmentAt(fragment: Fragment, position: Int) {
        if (fragments.contains(fragment)) return
        fragments.add(position, fragment)
        notifyDataSetChanged()
    }
    
    fun removeFragment(fragment: Fragment) {
        if (fragments.contains(fragment)) {
            fragment.onDestroy()
            fragments.remove(fragment)
            notifyDataSetChanged()
        }
    }
    
    fun removeItemAt(position: Int) {
        fragments[position]?.let { removeFragment(it) }
    }
    
    override fun getCount(): Int = fragments.size
}