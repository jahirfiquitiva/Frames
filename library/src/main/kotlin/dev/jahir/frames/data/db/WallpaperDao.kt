package dev.jahir.frames.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
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

    @Query("""
        SELECT * FROM wallpapers 
        WHERE ROWID > (SELECT ROWID FROM wallpapers WHERE url = :currentUrl) 
        ORDER BY ROWID ASC LIMIT 1
    """)
    suspend fun getNextWallpaper(currentUrl: String): Wallpaper?

    @Query("""
        SELECT * FROM wallpapers 
        WHERE ROWID < (SELECT ROWID FROM wallpapers WHERE url = :currentUrl) 
        ORDER BY ROWID DESC LIMIT 1
    """)
    suspend fun getPreviousWallpaper(currentUrl: String): Wallpaper?

    @Query("SELECT * FROM wallpapers ORDER BY ROWID LIMIT 1")
    suspend fun getFirstWallpaper(): Wallpaper?

    @Query("SELECT * FROM wallpapers ORDER BY ROWID DESC LIMIT 1")
    suspend fun getLastWallpaper(): Wallpaper?

    @Query("""
        SELECT * FROM wallpapers 
        WHERE (
            collections = :collection 
            OR collections LIKE :collection || ',%'
            OR collections LIKE '%,' || :collection || ',%'  
            OR collections LIKE '%,' || :collection
        )
        AND ROWID > (SELECT ROWID FROM wallpapers WHERE url = :currentUrl) 
        ORDER BY ROWID LIMIT 1
    """)
    suspend fun getNextWallpaperInCollection(currentUrl: String, collection: String): Wallpaper?

    @Query("""
        SELECT * FROM wallpapers 
        WHERE (
            collections = :collection 
            OR collections LIKE :collection || ',%'
            OR collections LIKE '%,' || :collection || ',%'  
            OR collections LIKE '%,' || :collection
        )
        AND ROWID < (SELECT ROWID FROM wallpapers WHERE url = :currentUrl) 
        ORDER BY ROWID DESC LIMIT 1
    """)
    suspend fun getPreviousWallpaperInCollection(currentUrl: String, collection: String): Wallpaper?

    @Query("""
        SELECT * FROM wallpapers 
        WHERE (
            collections = :collection 
            OR collections LIKE :collection || ',%'
            OR collections LIKE '%,' || :collection || ',%'  
            OR collections LIKE '%,' || :collection
        )
        ORDER BY ROWID LIMIT 1
    """)
    suspend fun getFirstWallpaperInCollection(collection: String): Wallpaper?

    @Query("""
        SELECT * FROM wallpapers 
        WHERE (
            collections = :collection 
            OR collections LIKE :collection || ',%'
            OR collections LIKE '%,' || :collection || ',%'  
            OR collections LIKE '%,' || :collection
        )
        ORDER BY ROWID DESC LIMIT 1
    """)
    suspend fun getLastWallpaperInCollection(collection: String): Wallpaper?
}
