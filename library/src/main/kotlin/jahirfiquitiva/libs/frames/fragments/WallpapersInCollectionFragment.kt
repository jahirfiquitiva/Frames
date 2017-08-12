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

import android.os.Bundle
import jahirfiquitiva.libs.frames.fragments.base.BaseWallpapersFragment
import jahirfiquitiva.libs.frames.models.Wallpaper
import jahirfiquitiva.libs.kauextensions.extensions.formatCorrectly
import jahirfiquitiva.libs.kauextensions.ui.views.EmptyViewRecyclerView

class WallpapersInCollectionFragment:BaseWallpapersFragment() {
    val currentFavorites = ArrayList<Wallpaper>()
    
    override fun doOnFavoritesChange(data:ArrayList<Wallpaper>) {
        super.doOnFavoritesChange(data)
        collection?.let {
            val coll = it
            adapter.favorites = ArrayList<Wallpaper>(data.filter {
                it.collections.formatCorrectly().replace("_", " ").contains(coll.name, true)
            })
            rv.state = EmptyViewRecyclerView.State.NORMAL
        }
    }
    
    override fun doOnWallpapersChange(data:ArrayList<Wallpaper>, fromCollectionActivity:Boolean) {
        super.doOnWallpapersChange(data, fromCollectionActivity)
        collection?.let {
            val coll = it
            adapter.setItems(
                    ArrayList<Wallpaper>(data.filter {
                        it.collections.formatCorrectly().replace("_", " ").contains(coll.name, true)
                    }))
            rv.state = EmptyViewRecyclerView.State.NORMAL
        }
    }
    
    companion object {
        @JvmStatic
        fun newInstance(args:Bundle):WallpapersInCollectionFragment {
            val newsFragment = WallpapersInCollectionFragment()
            newsFragment.arguments = args
            return newsFragment
        }
    }
    
    override fun fromFavorites():Boolean = false
    // TODO: Should I set this to false?
    override fun showFavoritesIcon():Boolean = true
}