package dev.jahir.frames.data.db

import androidx.room.*
import dev.jahir.frames.data.models.Wallpaper

@Dao // Data Access Object
interface WallpaperDao {
    @Query("select * from wallpapers")
    fun getAllWallpapers(): List<Wallpaper>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    // insert into wallpapers values (name, author, url, ...)
    fun insert(wallpaper: Wallpaper)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(wallpapers: List<Wallpaper>)

    @Delete
    fun delete(wallpaper: Wallpaper)

    @Query("delete from wallpapers where url = :url")
    fun deleteByUrl(url: String)

    @Query("delete from wallpapers")
    fun nuke()
}