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
package jahirfiquitiva.libs.frames.ui.fragments

import jahirfiquitiva.libs.frames.data.models.Wallpaper
import jahirfiquitiva.libs.frames.ui.fragments.base.BaseWallpapersFragment

internal class FavoritesFragment : BaseWallpapersFragment() {
    
    private val actualFavorites = ArrayList<Wallpaper>()
    
    override fun doOnFavoritesChange(data: ArrayList<Wallpaper>) {
        super.doOnFavoritesChange(data)
        actualFavorites.clear()
        actualFavorites.addAll(data)
        wallsAdapter.setItems(data)
    }
    
    override fun getWallpapersForViewer(): ArrayList<Wallpaper> = ArrayList(actualFavorites)
    
    override fun autoStartLoad(): Boolean = true
    override fun fromCollectionActivity(): Boolean = false
    override fun fromFavorites(): Boolean = true
    override fun showFavoritesIcon(): Boolean = true
    
    companion object {
        fun create(hasChecker: Boolean): FavoritesFragment =
            FavoritesFragment().apply { this.hasChecker = hasChecker }
    }
}
