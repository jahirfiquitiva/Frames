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

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import jahirfiquitiva.libs.frames.models.Collection
import jahirfiquitiva.libs.frames.models.Wallpaper

class CollectionsViewModel:ViewModel() {
    val items = MutableLiveData<ArrayList<Collection>>()

    fun loadData(wallpapers:ArrayList<Wallpaper>) {
        items.postValue(items.value ?: loadItems(wallpapers))
    }

    private fun loadItems(wallpapers:ArrayList<Wallpaper>):ArrayList<Collection> {
        val collections = ArrayList<Collection>()
        val collectionsMap = HashMap<String, ArrayList<Wallpaper>>()
        for ((index, wallpaper) in wallpapers.withIndex()) {
            val collectionsText = wallpaper.collections
            if (collectionsText.isNotEmpty()) {
                val collectionsList = collectionsText.split(",")
                if (collectionsList.isNotEmpty()) {
                    collectionsList.forEach {
                        val wallsList = ArrayList<Wallpaper>()
                        if (collectionsMap.containsKey(it)) {
                            collectionsMap[it]?.let { wallsInCollection ->
                                wallsList.addAll(wallsInCollection)
                            }
                        }
                        wallsList.add(wallpapers[index])
                        collectionsMap.put(it, wallsList)
                    }
                }
            }
        }
        for (key in collectionsMap.keys) {
            collectionsMap[key]?.let {
                collections.add(Collection(key, it))
            }
        }
        return collections
    }
}