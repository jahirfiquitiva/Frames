package dev.jahir.frames.data.models

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize

@Entity(tableName = "wallpapers")
@Parcelize
data class Wallpaper(
    val name: String,
    @PrimaryKey
    val url: String,
    val author: String = "",
    @SerializedName(value = "thumbnail", alternate = ["thumbUrl", "thumb", "url-thumb"])
    val thumbnail: String? = "",
    @SerializedName(value = "collections", alternate = ["categories", "category"])
    val collections: String? = "",
    @SerializedName(value = "dimensions", alternate = ["dimension"])
    val dimensions: String? = "",
    val copyright: String? = "",
    val downloadable: Boolean? = true,
    val size: Long? = 0
) : Parcelable {
    @IgnoredOnParcel
    @Ignore
    var isInFavorites: Boolean = false
}