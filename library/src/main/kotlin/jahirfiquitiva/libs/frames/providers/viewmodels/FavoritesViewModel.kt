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
package jahirfiquitiva.libs.frames.providers.viewmodels

import jahirfiquitiva.libs.frames.data.models.Wallpaper
import jahirfiquitiva.libs.frames.data.models.db.FavoritesDao
import jahirfiquitiva.libs.frames.helpers.utils.AsyncTaskManager

class FavoritesViewModel:ListViewModel<FavoritesDao, Wallpaper>() {
    override fun loadItems(param:FavoritesDao):MutableList<Wallpaper> {
        val list = ArrayList<Wallpaper>()
        try {
            list.addAll(param.getFavorites().distinct())
        } catch (e:Exception) {
            e.printStackTrace()
        }
        return list
    }
    
    fun forceUpdateFavorites(items:List<Wallpaper>) {
        param?.let {
            AsyncTaskManager(it, {}, {
                try {
                    it.nukeFavorites()
                    for (item in items) {
                        it.addToFavorites(item)
                    }
                    loadData(it, true)
                    return@AsyncTaskManager true
                } catch (e:Exception) {
                    e.printStackTrace()
                    return@AsyncTaskManager false
                }
            }, {}).execute()
        }
    }
    
    fun isInFavorites(wallpaper:Wallpaper):Boolean {
        return try {
            val contains = items.value?.contains(wallpaper) ?: false
            contains
        } catch (e:Exception) {
            e.printStackTrace()
            false
        }
    }
    
    fun addToFavorites(wallpaper:Wallpaper, onError:() -> Unit) {
        try {
            if (isInFavorites(wallpaper)) return
            AsyncTaskManager(
                    wallpaper, {},
                    { it ->
                        try {
                            val old = ArrayList(param?.getFavorites())
                            param?.nukeFavorites()
                            items.value?.clear()
                            if (!(old.contains(it))) old.add(it)
                            old.forEach {
                                param?.addToFavorites(it)
                            }
                            param?.let { loadData(it, true) }
                            return@AsyncTaskManager true
                        } catch (e:Exception) {
                            e.printStackTrace()
                            return@AsyncTaskManager false
                        }
                    }, {}).execute()
        } catch (e:Exception) {
            e.printStackTrace()
            onError()
        }
    }
    
    fun removeFromFavorites(wallpaper:Wallpaper, onError:() -> Unit) {
        try {
            if (!(isInFavorites(wallpaper))) return
            AsyncTaskManager(
                    wallpaper, {},
                    { it ->
                        try {
                            val old = ArrayList(param?.getFavorites())
                            param?.nukeFavorites()
                            items.value?.clear()
                            if (old.contains(it)) old.remove(it)
                            old.forEach {
                                param?.addToFavorites(it)
                            }
                            param?.let { loadData(it, true) }
                            return@AsyncTaskManager true
                        } catch (e:Exception) {
                            e.printStackTrace()
                            return@AsyncTaskManager false
                        }
                    }, {}).execute()
        } catch (e:Exception) {
            e.printStackTrace()
            onError()
        }
    }
}