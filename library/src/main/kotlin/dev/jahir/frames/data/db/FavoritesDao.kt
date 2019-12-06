package dev.jahir.frames.data.db

import androidx.room.*
import dev.jahir.frames.data.models.Favorite

@Dao // Data Access Object
interface FavoritesDao {
    @Query("select * from favorites")
    fun getAllFavorites(): List<Favorite>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    // insert into favorites values (name, author, url, ...)
    fun insert(favorite: Favorite)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(favorites: List<Favorite>)

    @Delete
    fun delete(favorite: Favorite)

    @Query("delete from favorites where url = :url")
    fun deleteByUrl(url: String)

    @Query("delete from favorites")
    fun nuke()
}