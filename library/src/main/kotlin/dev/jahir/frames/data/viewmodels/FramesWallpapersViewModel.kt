package dev.jahir.frames.data.viewmodels

import dev.jahir.frames.data.models.Collection
import dev.jahir.frames.data.models.Wallpaper

class FramesWallpapersViewModel : WallpapersDataViewModel() {
    override fun internalTransformWallpapersToCollections(wallpapers: List<Wallpaper>): List<Collection> {
        val collections =
            wallpapers.joinToString(",") { it.collections ?: "" }
                .replace("|", ",")
                .split(",")
                .distinct()
        val importantCollectionsNames = listOf(
            "all", "featured", "new", "wallpaper of the day", "wallpaper of the week"
        )
        val sortedCollectionsNames =
            listOf(importantCollectionsNames, collections).flatten().distinct()

        var usedCovers = ArrayList<String>()
        val actualCollections: ArrayList<Collection> = ArrayList()
        sortedCollectionsNames.forEach { collectionName ->
            val collection = Collection(collectionName)
            wallpapers.filter { it.collections.orEmpty().contains(collectionName, true) }
                .distinctBy { it.url }
                .forEach { collection.push(it) }
            usedCovers = collection.setupCover(usedCovers)
            if (collection.count > 0) actualCollections.add(collection)
        }
        return actualCollections
    }
}