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
package jahirfiquitiva.libs.frames.helpers.utils.diff

import android.support.v7.util.DiffUtil
import jahirfiquitiva.libs.frames.data.models.Collection
import jahirfiquitiva.libs.frames.data.models.Wallpaper

abstract class BaseDiffCallback<Type>(private val oldList:ArrayList<Type>,
                                      private val newList:ArrayList<Type>):DiffUtil.Callback() {
    override fun getOldListSize():Int = oldList.size
    
    override fun getNewListSize():Int = newList.size
    
    override fun areItemsTheSame(oldItemPosition:Int, newItemPosition:Int):Boolean =
            newList[newItemPosition] == oldList[oldItemPosition]
    
    override fun areContentsTheSame(oldItemPosition:Int, newItemPosition:Int):Boolean =
            newList[newItemPosition] == oldList[oldItemPosition]
}

class WallpapersDiffCallback(oldList:ArrayList<Wallpaper>, newList:ArrayList<Wallpaper>):
        BaseDiffCallback<Wallpaper>(oldList, newList)

class CollectionsDiffCallback(oldList:ArrayList<Collection>, newList:ArrayList<Collection>):
        BaseDiffCallback<Collection>(oldList, newList)