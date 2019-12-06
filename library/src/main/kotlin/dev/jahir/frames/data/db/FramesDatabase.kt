package dev.jahir.frames.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import dev.jahir.frames.data.models.Favorite
import dev.jahir.frames.data.models.Wallpaper

@Database(
    entities = [Wallpaper::class, Favorite::class],
    version = 3,
    exportSchema = false
)
abstract class FramesDatabase : RoomDatabase() {
    abstract fun wallpapersDao(): WallpaperDao?
    abstract fun favoritesDao(): FavoritesDao?

    companion object {
        private var INSTANCE: FramesDatabase? = null

        fun getAppDatabase(context: Context): FramesDatabase? {
            if (INSTANCE == null) {
                INSTANCE = Room.databaseBuilder(
                    context.applicationContext,
                    FramesDatabase::class.java,
                    context.applicationInfo.name ?: "Frames"
                )
                    .fallbackToDestructiveMigration()
                    .build()
            }
            return INSTANCE
        }

        fun destroyInstance() {
            INSTANCE = null
        }
    }
}