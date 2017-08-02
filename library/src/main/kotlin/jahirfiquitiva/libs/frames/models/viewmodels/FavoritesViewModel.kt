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
import jahirfiquitiva.libs.frames.utils.AsyncTaskManager

class FavoritesViewModel:ListViewModel<FavoritesDao, Wallpaper>() {
    override fun loadItems(param:FavoritesDao):ArrayList<Wallpaper> {
        val list = ArrayList<Wallpaper>()
        try {
            val favs = param.getFavorites().distinct()
            list.addAll(favs.distinct())
        } catch (e:Exception) {
            e.printStackTrace()
        }
        return list
    }
    
    fun forceUpdateFavorites(items:List<Wallpaper>) {
        param?.let {
            AsyncTaskManager(it, {}, {
                try {
                    val currentItems = it.getFavorites()
                    for (item in currentItems) {
                        it.removeFromFavorites(item)
                    }
                    it.insertAll(items)
                    for (item in items) {
                        it.addToFavorites(item)
                    }
                    val list = ArrayList<Wallpaper>()
                    list.addAll(it.getFavorites())
                    list.distinct()
                    postResult(list)
                    return@AsyncTaskManager true
                } catch (e:Exception) {
                    e.printStackTrace()
                    return@AsyncTaskManager false
                }
            }, {})
        }
    }
    
    fun isInFavorites(wallpaper:Wallpaper):Boolean {
        try {
            return items.value?.contains(wallpaper) ?: false
        } catch (e:Exception) {
            e.printStackTrace()
            return false
        }
    }
    
    fun addToFavorites(wallpaper:Wallpaper) {
        try {
            if (isInFavorites(wallpaper)) return
            AsyncTaskManager(
                    wallpaper, {},
                    { it ->
                        try {
                            param?.addToFavorites(it)
                            items.value?.add(it)
                            items.value?.let { postResult(it) }
                            return@AsyncTaskManager true
                        } catch (e:Exception) {
                            e.printStackTrace()
                            return@AsyncTaskManager false
                        }
                    }, {}).execute()
        } catch (e:Exception) {
            e.printStackTrace()
        }
    }
    
    fun removeFromFavorites(wallpaper:Wallpaper) {
        try {
            if (!isInFavorites(wallpaper)) return
            AsyncTaskManager(
                    wallpaper, {},
                    { it ->
                        try {
                            param?.removeFromFavorites(it)
                            items.value?.remove(it)
                            items.value?.let { postResult(it) }
                            return@AsyncTaskManager true
                        } catch (e:Exception) {
                            e.printStackTrace()
                            return@AsyncTaskManager false
                        }
                    }, {}).execute()
        } catch (e:Exception) {
            e.printStackTrace()
        }
    }
}