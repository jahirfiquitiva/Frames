package dev.jahir.frames.extensions

import androidx.appcompat.widget.AppCompatImageView
import coil.Coil
import coil.api.load
import coil.request.LoadRequestBuilder
import dev.jahir.frames.ui.animations.SaturatingImageViewTarget

fun AppCompatImageView.loadFramesPic(
    url: String,
    thumbnail: String? = url,
    builder: LoadRequestBuilder.() -> Unit = {}
) {
    val saturatingTarget = SaturatingImageViewTarget(this)
    val shouldLoadThumbnail = thumbnail?.let { it.isNotEmpty() && it != url } ?: false
    if (shouldLoadThumbnail) {
        Coil.load(context, thumbnail) {
            builder()
            saturatingTarget.afterSuccess = { thumbnailDrawable ->
                load(url) {
                    builder()
                    placeholder(thumbnailDrawable)
                    error(thumbnailDrawable)
                }
            }
            target(saturatingTarget)
            listener(saturatingTarget)
        }
    } else {
        load(url) {
            builder()
            target(saturatingTarget)
            listener(saturatingTarget)
        }
    }
}