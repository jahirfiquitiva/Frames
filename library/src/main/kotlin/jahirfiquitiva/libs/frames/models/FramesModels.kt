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

package jahirfiquitiva.libs.frames.models

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import android.os.Parcel
import android.os.Parcelable
import java.util.*

@Entity(tableName = "FAVORITES")
data class Wallpaper(
        @ColumnInfo(name = "NAME")
        var name:String = "",
        @ColumnInfo(name = "AUTHOR")
        var author:String = "",
        @ColumnInfo(name = "COLLECTIONS")
        var collections:String = "",
        @ColumnInfo(name = "DOWNLOADABLE")
        var downloadable:Boolean = true,
        @ColumnInfo(name = "URL")
        var url:String = "",
        @ColumnInfo(name = "THUMB_URL")
        var thumbUrl:String = url,
        @PrimaryKey(autoGenerate = true)
        @ColumnInfo(name = "ID")
        var id:Long = 0):Parcelable {
    
    constructor(parcel:Parcel):this(
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readByte() != 0.toByte(),
            parcel.readString(),
            parcel.readString(),
            parcel.readLong())
    
    override fun equals(other:Any?):Boolean {
        if (other !is Wallpaper) return false
        return url.equals(other.url, true) || thumbUrl.equals(other.thumbUrl, true)
    }
    
    override fun hashCode():Int {
        var result = name.hashCode()
        result = 31 * result + author.hashCode()
        result = 31 * result + url.hashCode()
        result = 31 * result + thumbUrl.hashCode()
        return result
    }
    
    override fun writeToParcel(parcel:Parcel, flags:Int) {
        parcel.writeString(name)
        parcel.writeString(author)
        parcel.writeString(collections)
        parcel.writeByte(if (downloadable) 1 else 0)
        parcel.writeString(url)
        parcel.writeString(thumbUrl)
        parcel.writeLong(id)
    }
    
    override fun describeContents():Int {
        return 0
    }
    
    companion object CREATOR:Parcelable.Creator<Wallpaper> {
        override fun createFromParcel(parcel:Parcel):Wallpaper {
            return Wallpaper(parcel)
        }
        
        override fun newArray(size:Int):Array<Wallpaper?> {
            return arrayOfNulls(size)
        }
    }
}

data class Collection(val name:String,
                      var wallpapers:ArrayList<Wallpaper> = ArrayList<Wallpaper>()):Parcelable {
    
    constructor(parcel:Parcel):this(parcel.readString()) {
        parcel.readTypedList(wallpapers, Wallpaper.CREATOR)
    }
    
    override fun writeToParcel(parcel:Parcel, flags:Int) {
        parcel.writeString(name)
        parcel.writeTypedList(wallpapers)
    }
    
    override fun describeContents():Int {
        return 0
    }
    
    companion object CREATOR:Parcelable.Creator<Collection> {
        override fun createFromParcel(parcel:Parcel):Collection {
            return Collection(parcel)
        }
        
        override fun newArray(size:Int):Array<Collection?> {
            return arrayOfNulls(size)
        }
    }
}