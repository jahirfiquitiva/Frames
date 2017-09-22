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
package jahirfiquitiva.libs.frames.ui.fragments

import jahirfiquitiva.libs.frames.data.models.Collection
import jahirfiquitiva.libs.frames.data.models.Wallpaper
import jahirfiquitiva.libs.frames.ui.fragments.base.BaseWallpapersFragment
import jahirfiquitiva.libs.frames.ui.widgets.EmptyViewRecyclerView
import jahirfiquitiva.libs.kauextensions.extensions.formatCorrectly

class WallpapersInCollectionFragment:BaseWallpapersFragment() {
    
    private var collection:Collection? = null
    private var wallpapers = ArrayList<Wallpaper>()
    private var firstFavsModification = true
    
    var newFavs = ArrayList<Wallpaper>()
    
    override fun doOnFavoritesChange(data:ArrayList<Wallpaper>) {
        super.doOnFavoritesChange(data)
        collection?.let {
            val collectionName = it.name
            wallsAdapter?.favorites = ArrayList(data.filter {
                it.collections.formatCorrectly().replace("_", " ").contains(collectionName, true)
            })
        }
        if (!firstFavsModification) {
            newFavs.clear()
            newFavs.addAll(data)
        } else {
            firstFavsModification = false
        }
    }
    
    override fun doOnWallpapersChange(data:ArrayList<Wallpaper>, fromCollectionActivity:Boolean) {
        super.doOnWallpapersChange(data, fromCollectionActivity)
        collection?.let {
            val collectionName = it.name
            wallsAdapter?.setItems(ArrayList(data.filter {
                it.collections.formatCorrectly().replace("_", " ").contains(collectionName, true)
            }))
        }
    }
    
    override fun loadDataFromViewModel() {
        super.loadDataFromViewModel()
        rv.state = EmptyViewRecyclerView.State.LOADING
        wallpapersModel?.postResult(wallpapers)
    }
    
    companion object {
        fun create(collection:Collection, wallpapers:ArrayList<Wallpaper>):
                WallpapersInCollectionFragment {
            return WallpapersInCollectionFragment().apply {
                this.collection = collection
                this.wallpapers.clear()
                this.wallpapers.addAll(wallpapers)
            }
        }
    }
    
    override fun autoStartLoad():Boolean = true
    override fun fromCollectionActivity():Boolean = true
    override fun fromFavorites():Boolean = false
    override fun showFavoritesIcon():Boolean = true
}