package dev.jahir.frames.data.db

import androidx.room.TypeConverter

object RoomNullToString {
    @TypeConverter
    fun fromNullToString(value: String?): String = value ?: ""
}