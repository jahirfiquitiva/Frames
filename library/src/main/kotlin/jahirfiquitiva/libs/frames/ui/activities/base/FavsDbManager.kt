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
package jahirfiquitiva.libs.frames.ui.activities.base

import android.content.Context
import android.support.annotation.CallSuper
import jahirfiquitiva.libs.frames.R
import jahirfiquitiva.libs.frames.data.models.Wallpaper
import jahirfiquitiva.libs.frames.data.models.db.FavoritesDatabase
import jahirfiquitiva.libs.frames.providers.viewmodels.FavoritesViewModel
import jahirfiquitiva.libs.kext.extensions.string
import org.jetbrains.anko.doAsync

internal interface FavsDbManager {
    val favsViewModel: FavoritesViewModel
    val favsDB: FavoritesDatabase
    
    @CallSuper
    fun reloadFavorites() {
        doAsync { favsViewModel.loadData(favsDB.favoritesDao(), true) }
    }
    
    @CallSuper
    fun setNewFavorites(list: ArrayList<Wallpaper>) {
        favsViewModel.forceUpdateFavorites(favsDB.favoritesDao(), list)
    }
    
    @CallSuper
    fun updateToFavs(wallpaper: Wallpaper, add: Boolean, ctxt: Context, showSnack: Boolean = true) {
        if (add) addToFavs(wallpaper, ctxt, showSnack)
        else removeFromFavs(wallpaper, ctxt, showSnack)
    }
    
    private fun addToFavs(wallpaper: Wallpaper, ctxt: Context, showSnack: Boolean) {
        favsViewModel.addToFavorites(favsDB.favoritesDao(), wallpaper) {
            if (showSnack)
                showSnackbar(
                    if (it) ctxt.getString(R.string.added_to_favorites, wallpaper.name)
                    else ctxt.string(R.string.action_error_content))
        }
    }
    
    private fun removeFromFavs(wallpaper: Wallpaper, ctxt: Context, showSnack: Boolean) {
        favsViewModel.removeFromFavorites(favsDB.favoritesDao(), wallpaper) {
            if (showSnack)
                showSnackbar(
                    if (it) ctxt.getString(R.string.removed_from_favorites, wallpaper.name)
                    else ctxt.string(R.string.action_error_content))
        }
    }
    
    @CallSuper
    fun isInFavs(wallpaper: Wallpaper): Boolean = favsViewModel.isInFavorites(wallpaper)
    
    @CallSuper
    fun getFavs(): ArrayList<Wallpaper> = ArrayList(favsViewModel.getData().orEmpty())
    
    fun showSnackbar(text: String)
    
    fun notifyFavsToFrags(favs: ArrayList<Wallpaper>)
}