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

package jahirfiquitiva.libs.frames.utils

import android.content.Context
import com.afollestad.inquiry.Inquiry
import jahirfiquitiva.libs.frames.models.Wallpaper
import java.util.*

const val FAVS_DB_NAME = "FAVS_DB"

fun Context.initFavoritesDatabase() {
    Inquiry.newInstance(this, FAVS_DB_NAME).build()
}

fun Context.destroyFavoritesDatabase() {
    Inquiry.destroy(this)
}

fun Context.getFavoriteWallpapers():ArrayList<Wallpaper> {
    Inquiry.get(this).select(Wallpaper::class.java).all()?.let {
        val list = ArrayList<Wallpaper>()
        list.addAll(it.asList())
        return list
    }
    return ArrayList()
}

fun Context.isWallpaperInFavorites(wallpaper:Wallpaper):Boolean =
        Inquiry.get(this).select(Wallpaper::class.java)
                .where("_url = ?", wallpaper.url).first() != null

fun Context.toggleFavorite(wallpaper:Wallpaper):Boolean {
    if (!isWallpaperInFavorites(wallpaper)) return addToFavorites(wallpaper)
    return removeFromFavorites(wallpaper)
}

fun Context.addToFavorites(wallpaper:Wallpaper):Boolean {
    try {
        Inquiry.get(this).insert(Wallpaper::class.java).values(arrayOf(wallpaper)).run()
        return true
    } catch (e:Exception) {
        return false
    }
}

fun Context.removeFromFavorites(wallpaper:Wallpaper):Boolean {
    try {
        Inquiry.get(this).delete(Wallpaper::class.java).where("_url = ?", wallpaper.url).run()
        return true
    } catch (e:Exception) {
        return false
    }
}

fun Context.removeFavorites() {
    Inquiry.get(this).dropTable(Wallpaper::class.java)
    destroyFavoritesDatabase()
}

