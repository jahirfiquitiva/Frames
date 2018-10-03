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
package jahirfiquitiva.libs.frames.ui.fragments

import jahirfiquitiva.libs.frames.data.models.Collection
import jahirfiquitiva.libs.frames.data.models.Wallpaper
import jahirfiquitiva.libs.frames.helpers.extensions.jfilter
import jahirfiquitiva.libs.frames.ui.fragments.base.BaseWallpapersFragment
import jahirfiquitiva.libs.kext.extensions.formatCorrectly

internal class WallpapersInCollectionFragment : BaseWallpapersFragment() {
    
    private var collection: Collection? = null
    private var wallpapers = ArrayList<Wallpaper>()
    private var firstFavsModification = true
    
    val newFavs = ArrayList<Wallpaper>()
    
    private fun getWallpapersInCollection(all: ArrayList<Wallpaper>): ArrayList<Wallpaper> {
        collection?.let {
            val collectionName = it.name
            return all.jfilter {
                it.collections.formatCorrectly().replace("_", " ").contains(collectionName, true)
            }
        }
        return ArrayList()
    }
    
    override fun doOnFavoritesChange(data: ArrayList<Wallpaper>) {
        super.doOnFavoritesChange(getWallpapersInCollection(data))
        if (!firstFavsModification) {
            newFavs.clear()
            newFavs.addAll(data)
        } else {
            firstFavsModification = false
        }
    }
    
    override fun doOnWallpapersChange(data: ArrayList<Wallpaper>, fromCollectionActivity: Boolean) {
        super.doOnWallpapersChange(data, fromCollectionActivity)
        wallsAdapter.setItems(getWallpapersInCollection(data))
    }
    
    override fun loadDataFromViewModel() {
        super.loadDataFromViewModel()
        wallpapersModel?.postResult(wallpapers)
    }
    
    companion object {
        fun create(
            collection: Collection,
            wallpapers: ArrayList<Wallpaper>,
            hasChecker: Boolean
                  ):
            WallpapersInCollectionFragment {
            return WallpapersInCollectionFragment().apply {
                this.collection = collection
                this.wallpapers.clear()
                this.wallpapers.addAll(wallpapers)
                this.hasChecker = hasChecker
            }
        }
    }
    
    override fun autoStartLoad(): Boolean = true
    override fun fromCollectionActivity(): Boolean = true
    override fun fromFavorites(): Boolean = false
    override fun showFavoritesIcon(): Boolean = true
}