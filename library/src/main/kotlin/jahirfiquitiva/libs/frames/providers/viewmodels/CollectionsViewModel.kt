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

import jahirfiquitiva.libs.frames.data.models.Collection
import jahirfiquitiva.libs.frames.data.models.Wallpaper
import jahirfiquitiva.libs.kauextensions.extensions.formatCorrectly
import jahirfiquitiva.libs.kauextensions.extensions.hasContent
import jahirfiquitiva.libs.kauextensions.extensions.toTitleCase

class CollectionsViewModel:ListViewModel<ArrayList<Wallpaper>, Collection>() {
    override fun loadItems(param:ArrayList<Wallpaper>):MutableList<Collection> {
        val collections = ArrayList<Collection>()
        val collectionsMap = HashMap<String, ArrayList<Wallpaper>>()
        for ((index, wallpaper) in param.withIndex()) {
            val collectionsText = wallpaper.collections
            if (collectionsText.hasContent()) {
                val collectionsList = collectionsText.split("[,|]".toRegex())
                if (collectionsList.isNotEmpty()) {
                    collectionsList.forEach {
                        val wallsList = ArrayList<Wallpaper>()
                        if (collectionsMap.containsKey(it)) {
                            collectionsMap[it]?.let { wallsInCollection ->
                                wallsList.addAll(wallsInCollection.distinct())
                            }
                        }
                        wallsList.add(param[index])
                        collectionsMap.put(it, wallsList)
                    }
                }
            }
        }
        collections.clear()
        val usedNames = ArrayList<String>()
        for (key in collectionsMap.keys) {
            collectionsMap[key]?.let {
                if (it.isNotEmpty()) {
                    val coll = Collection(key.formatCorrectly().replace("_", " ").toTitleCase(), it)
                    coll.bestCover = getBestCoverPicture(usedNames, it)
                    collections.add(coll)
                }
            }
        }
        usedNames.clear()
        
        collections.sortBy { it.name }
        collections.distinct()
        
        val importantCollectionsNames = arrayOf("all", "featured", "new", "wallpaper of the day",
                                                "wallpaper of the week")
        val importantCollections = ArrayList<Collection>()
        
        importantCollectionsNames.forEach {
            val position = getCollectionPosition(it, collections)
            if (position >= 0) {
                if (it != "all") importantCollections.add(collections[position])
                collections.removeAt(position)
            }
        }
        
        val newCollections = ArrayList<Collection>()
        importantCollections.forEach {
            newCollections.add(0, it)
        }
        newCollections.addAll(collections)
        
        return newCollections
    }
    
    private fun getCollectionPosition(otherName:String, inList:ArrayList<Collection>):Int {
        inList.forEachIndexed { index, (name) -> if (name.equals(otherName, true)) return index }
        return -1
    }
    
    private fun getBestCoverPicture(usedNames:ArrayList<String>,
                                    possibleWallpapers:ArrayList<Wallpaper>):Wallpaper? {
        possibleWallpapers.forEach {
            val wasInTheList = usedNames.contains(it.name)
            usedNames.add(it.name)
            if (!wasInTheList) return it
        }
        return null
    }
}