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
package jahirfiquitiva.libs.frames.viewmodels

import jahirfiquitiva.libs.archhelpers.tasks.QAsync
import jahirfiquitiva.libs.archhelpers.viewmodels.ListViewModel
import jahirfiquitiva.libs.frames.data.models.Wallpaper
import jahirfiquitiva.libs.frames.data.models.db.FavoritesDao
import java.lang.ref.WeakReference

class FavoritesViewModel : ListViewModel<FavoritesDao, Wallpaper>() {
    
    private var daoTask: QAsync<*, *>? = null
    
    override fun internalLoad(param: FavoritesDao): ArrayList<Wallpaper> =
        ArrayList(param.getFavorites().reversed().distinct())
    
    private fun cancelDaoTask() {
        daoTask?.cancel(true)
        daoTask = null
    }
    
    fun forceUpdateFavorites(dao: FavoritesDao, items: List<Wallpaper>) {
        cancelDaoTask()
        daoTask = QAsync<FavoritesDao, Unit>(
            WeakReference(dao),
            object : QAsync.Callback<FavoritesDao, Unit>() {
                override fun doLoad(param: FavoritesDao): Unit? =
                    internalForceUpdateFavorites(param, items)
                
                override fun onSuccess(result: Unit) {}
            })
        daoTask?.execute()
    }
    
    private fun internalForceUpdateFavorites(dao: FavoritesDao, items: List<Wallpaper>) {
        dao.nukeFavorites()
        items.forEach { dao.addToFavorites(it) }
        loadData(dao, true)
    }
    
    fun isInFavorites(wallpaper: Wallpaper): Boolean = getData().orEmpty().contains(wallpaper)
    
    fun addToFavorites(
        dao: FavoritesDao,
        wallpaper: Wallpaper,
        onResult: (success: Boolean) -> Unit
                      ) {
        if (isInFavorites(wallpaper)) return
        cancelDaoTask()
        daoTask = QAsync<Wallpaper, Unit>(
            WeakReference(wallpaper),
            object : QAsync.Callback<Wallpaper, Unit>() {
                override fun doLoad(param: Wallpaper) {
                    val oldList = ArrayList(dao.getFavorites())
                    if (!oldList.contains(param)) {
                        oldList.add(param)
                        forceUpdateFavorites(dao, oldList)
                    }
                }
                
                override fun onSuccess(result: Unit) {
                    onResult(true)
                }
                
                override fun onError(e: Exception?): Unit? {
                    onResult(false)
                    return super.onError(e)
                }
            })
        daoTask?.execute()
    }
    
    fun removeFromFavorites(
        dao: FavoritesDao,
        wallpaper: Wallpaper,
        onResult: (success: Boolean) -> Unit
                           ) {
        if (!isInFavorites(wallpaper)) return
        cancelDaoTask()
        daoTask = QAsync<Wallpaper, Unit>(
            WeakReference(wallpaper),
            object : QAsync.Callback<Wallpaper, Unit>() {
                override fun doLoad(param: Wallpaper) {
                    val oldList = ArrayList(dao.getFavorites())
                    if (oldList.contains(param)) {
                        oldList.remove(param)
                        forceUpdateFavorites(dao, oldList)
                    }
                }
                
                override fun onSuccess(result: Unit) {
                    onResult(true)
                }
                
                override fun onError(e: Exception?): Unit? {
                    onResult(false)
                    return super.onError(e)
                }
            })
        daoTask?.execute()
    }
}
