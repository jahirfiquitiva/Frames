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
package jahirfiquitiva.libs.frames.data.models.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import jahirfiquitiva.libs.frames.data.models.Wallpaper
import jahirfiquitiva.libs.frames.helpers.utils.DATABASE_NAME

@Dao
interface FavoritesDao {
    @Query("SELECT * FROM $DATABASE_NAME")
    fun getFavorites(): List<Wallpaper>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addToFavorites(wallpaper: Wallpaper)
    
    @Delete
    fun removeFromFavorites(wallpaper: Wallpaper)
    
    @Query("DELETE FROM $DATABASE_NAME")
    fun nukeFavorites()
}
