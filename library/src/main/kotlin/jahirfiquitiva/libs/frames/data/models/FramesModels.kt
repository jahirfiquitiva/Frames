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
package jahirfiquitiva.libs.frames.data.models

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.Ignore
import android.arch.persistence.room.PrimaryKey
import android.os.Parcel
import android.os.Parcelable
import jahirfiquitiva.libs.frames.helpers.extensions.toReadableByteCount
import jahirfiquitiva.libs.kauextensions.extensions.readBoolean
import jahirfiquitiva.libs.kauextensions.extensions.writeBoolean
import java.util.ArrayList

@Entity(tableName = "FAVORITES")
data class Wallpaper(
        @ColumnInfo(name = "NAME")
        var name: String = "",
        @ColumnInfo(name = "AUTHOR")
        var author: String = "",
        @ColumnInfo(name = "COLLECTIONS")
        var collections: String = "",
        @ColumnInfo(name = "DOWNLOADABLE")
        var downloadable: Boolean = true,
        @ColumnInfo(name = "URL")
        var url: String = "",
        @ColumnInfo(name = "THUMB_URL")
        var thumbUrl: String = url,
        @ColumnInfo(name = "SIZE")
        var size: Long = 0,
        @ColumnInfo(name = "DIMENSIONS")
        var dimensions: String = "",
        @ColumnInfo(name = "COPYRIGHT")
        var copyright: String = "",
        @Ignore
        var hasFaded: Boolean = false,
        @PrimaryKey(autoGenerate = true)
        @ColumnInfo(name = "ID")
        var id: Long = 0
                    ) : Parcelable {
    
    constructor(parcel: Parcel) : this(
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readBoolean(),
            parcel.readString(),
            parcel.readString(),
            parcel.readLong(),
            parcel.readString(),
            parcel.readString(),
            parcel.readBoolean(),
            parcel.readLong())
    
    override fun equals(other: Any?): Boolean {
        if (other !is Wallpaper) return false
        return name.equals(other.name, true) && url.equals(other.url, true)
    }
    
    fun hasChangedFrom(other: Wallpaper?) =
            other != null && (size != other.size || (!(dimensions.equals(other.dimensions, true))))
    
    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + author.hashCode()
        result = 31 * result + url.hashCode()
        result = 31 * result + thumbUrl.hashCode()
        return result
    }
    
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeString(author)
        parcel.writeString(collections)
        parcel.writeBoolean(downloadable)
        parcel.writeString(url)
        parcel.writeString(thumbUrl)
        parcel.writeLong(size)
        parcel.writeString(dimensions)
        parcel.writeString(copyright)
        parcel.writeBoolean(hasFaded)
        parcel.writeLong(id)
    }
    
    override fun describeContents(): Int = 0
    
    companion object CREATOR : Parcelable.Creator<Wallpaper> {
        override fun createFromParcel(parcel: Parcel): Wallpaper = Wallpaper(parcel)
        override fun newArray(size: Int): Array<Wallpaper?> = arrayOfNulls(size)
    }
}

data class Collection(
        val name: String,
        var wallpapers: ArrayList<Wallpaper> = ArrayList()
                     ) : Parcelable {
    
    var bestCover: Wallpaper? = null
    
    override fun equals(other: Any?): Boolean {
        if (other !is Collection) return false
        return name.equals(other.name, true)
    }
    
    override fun toString(): String = name
    
    constructor(parcel: Parcel) : this(parcel.readString()) {
        parcel.readTypedList(wallpapers, Wallpaper.CREATOR)
    }
    
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeTypedList(wallpapers)
    }
    
    override fun describeContents(): Int = 0
    
    companion object CREATOR : Parcelable.Creator<Collection> {
        override fun createFromParcel(parcel: Parcel): Collection = Collection(parcel)
        override fun newArray(size: Int): Array<Collection?> = arrayOfNulls(size)
    }
}

data class WallpaperInfo(val size: Long, val dimension: Dimension) {
    val isValid: Boolean = size > 0L || dimension.isValid
    override fun toString(): String =
            "WallpaperInfo:[size = '${size.toReadableByteCount()}', dimension = '$dimension']"
}

data class Dimension(val width: Long, val height: Long) {
    val isValid: Boolean = width > 0L && height > 0L
    override fun toString(): String = "$width x $height px"
}