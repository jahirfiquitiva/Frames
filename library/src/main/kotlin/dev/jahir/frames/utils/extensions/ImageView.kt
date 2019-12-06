package dev.jahir.frames.utils.extensions

import androidx.appcompat.widget.AppCompatImageView
import coil.Coil
import coil.api.load
import coil.request.LoadRequestBuilder

fun AppCompatImageView.loadFramesPic(
    url: String,
    thumbnail: String? = url,
    builder: LoadRequestBuilder.() -> Unit = {}
) {
    val shouldLoadThumbnail = thumbnail?.let { it.isNotEmpty() && it != url } ?: false
    if (shouldLoadThumbnail) {
        Coil.load(context, thumbnail) {
            builder()
            target { thumbnailDrawable ->
                load(url) {
                    builder()
                    placeholder(thumbnailDrawable)
                    error(thumbnailDrawable)
                }
            }
        }
    } else {
        load(url) { builder() }
    }
}