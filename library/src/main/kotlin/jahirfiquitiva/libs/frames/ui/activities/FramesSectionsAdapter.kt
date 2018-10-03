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
package jahirfiquitiva.libs.frames.ui.activities

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import jahirfiquitiva.libs.frames.ui.fragments.CollectionsFragment
import jahirfiquitiva.libs.frames.ui.fragments.FavoritesFragment
import jahirfiquitiva.libs.frames.ui.fragments.WallpapersFragment
import jahirfiquitiva.libs.kext.ui.fragments.adapters.DynamicFragmentsPagerAdapter

internal class FramesSectionsAdapter(
    manager: FragmentManager,
    private val withChecker: Boolean,
    private val withCollections: Boolean
                                    ) : DynamicFragmentsPagerAdapter(manager) {
    override fun createItem(position: Int): Fragment = when (position) {
        0 -> {
            if (withCollections) CollectionsFragment.create(withChecker)
            else WallpapersFragment.create(withChecker)
        }
        1 -> {
            if (withCollections) WallpapersFragment.create(withChecker)
            else FavoritesFragment.create(withChecker)
        }
        2 -> FavoritesFragment.create(withChecker)
        else -> Fragment()
    }
    
    override fun getCount(): Int = if (withCollections) 3 else 2
}