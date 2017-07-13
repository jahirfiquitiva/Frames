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

package jahirfiquitiva.libs.frames.models.viewmodels

import jahirfiquitiva.libs.frames.models.Wallpaper
import jahirfiquitiva.libs.frames.models.db.FavoritesDao

class FavoritesViewModel:ListViewModel<Wallpaper, FavoritesDao>() {
    override fun loadItems(p:FavoritesDao):ArrayList<Wallpaper> {
        val list = ArrayList<Wallpaper>()
        if (items.value != null && items.value?.size ?: 0 > 0) {
            items.value?.let { list.addAll(it) }
            return list
        }
        list.addAll(p.getFavorites())
        return list
    }

    fun isInFavorites(wallpaper:Wallpaper):Boolean {
        try {
            return items.value?.contains(wallpaper) ?: false
        } catch (ignored:Exception) {
            return false
        }
    }

    fun addToFavorites(wallpaper:Wallpaper):Boolean {
        try {
            if (isInFavorites(wallpaper)) return true
            param?.addToFavorites(wallpaper)
            items.value?.add(wallpaper)
            items.value?.let { postResult(it) }
            return true
        } catch (ignored:Exception) {
            return false
        }
    }

    fun removeFromFavorites(wallpaper:Wallpaper):Boolean {
        try {
            if (!isInFavorites(wallpaper)) return true
            param?.removeFromFavorites(wallpaper)
            items.value?.remove(wallpaper)
            items.value?.let { postResult(it) }
            return true
        } catch (ignored:Exception) {
            return false
        }
    }
}