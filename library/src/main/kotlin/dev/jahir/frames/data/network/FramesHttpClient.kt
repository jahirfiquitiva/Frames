package dev.jahir.frames.data.network

import okhttp3.OkHttpClient

/**
 * The only OkHttp client in Frames. The wallpapers JSON, the images (Coil) and the wallpaper
 * applier use it, so they share its connections and threads.
 */
internal val framesHttpClient: OkHttpClient by lazy { OkHttpClient() }
