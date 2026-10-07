package dev.jahir.frames.data.network

import dev.jahir.frames.data.models.Wallpaper
import org.json.JSONArray
import org.json.JSONObject

/**
 * Parses the wallpapers JSON: an array of wallpaper objects.
 * Missing keys stay null, as they did with Gson, so a missing size is not shown as "0 B".
 * @throws org.json.JSONException when the JSON is malformed or is not an array
 */
internal fun parseWallpapersJson(json: String): List<Wallpaper> {
    val array = JSONArray(json)
    return (0 until array.length()).mapNotNull { index ->
        array.optJSONObject(index)?.toWallpaper()
    }
}

private fun JSONObject.toWallpaper(): Wallpaper = Wallpaper(
    name = stringOrNull("name").orEmpty(),
    url = stringOrNull("url").orEmpty(),
    author = stringOrNull("author"),
    thumbnail = stringOrNull("thumbnail", "thumbUrl", "thumb", "url-thumb"),
    collections = stringOrNull("collections", "categories", "category"),
    dimensions = stringOrNull("dimensions", "dimension"),
    copyright = stringOrNull("copyright"),
    downloadable = if (isNull("downloadable")) null else optBoolean("downloadable"),
    size = if (isNull("size")) null else optLong("size"),
)

/** Value of the first of [keys] that is present and not null */
private fun JSONObject.stringOrNull(vararg keys: String): String? =
    keys.firstOrNull { !isNull(it) }?.let { optString(it) }
