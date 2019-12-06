package dev.jahir.frames.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "wallpapers")
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
    // @TypeConverters(RoomNullToString::class)
    val dimensions: String? = "",
    val copyright: String? = "",
    val downloadable: Boolean? = true,
    val size: Long? = 0
)