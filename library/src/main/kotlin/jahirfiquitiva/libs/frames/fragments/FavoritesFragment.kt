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

package jahirfiquitiva.libs.frames.fragments

import jahirfiquitiva.libs.frames.fragments.base.BaseWallpapersFragment
import jahirfiquitiva.libs.frames.models.Wallpaper
import jahirfiquitiva.libs.kauextensions.extensions.printInfo
import jahirfiquitiva.libs.kauextensions.ui.views.EmptyViewRecyclerView

class FavoritesFragment:BaseWallpapersFragment() {
    override fun doOnFavoritesChange(data:ArrayList<Wallpaper>) {
        super.doOnFavoritesChange(data)
        data.forEach { context.printInfo("Found favorite: " + it.toString()) }
        adapter.setItems(data)
        rv.state = EmptyViewRecyclerView.State.NORMAL
    }

    override fun fromFavorites():Boolean = true
    override fun showFavoritesIcon():Boolean = true
}