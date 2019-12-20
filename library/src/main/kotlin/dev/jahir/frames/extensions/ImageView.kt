package dev.jahir.frames.extensions

import android.graphics.drawable.Drawable
import androidx.appcompat.widget.AppCompatImageView
import coil.Coil
import coil.api.load
import dev.jahir.frames.ui.animations.SaturatingImageViewTarget

fun AppCompatImageView.loadFramesPic(
    url: String,
    thumbnail: String? = url,
    onLoaded: (Drawable?) -> Unit = {}
) {
    val saturatingTarget = SaturatingImageViewTarget(this)
    saturatingTarget.afterSuccess = onLoaded
    val shouldLoadThumbnail = thumbnail?.let { it.isNotEmpty() && it != url } ?: false
    if (shouldLoadThumbnail) {
        Coil.load(context, thumbnail) {
            crossfade(250)
            saturatingTarget.afterSuccess = { thumbnailDrawable ->
                onLoaded(thumbnailDrawable)
                load(url) {
                    crossfade(250)
                    placeholder(thumbnailDrawable)
                    error(thumbnailDrawable)
                    target { onLoaded(it) }
                }
            }
            target(saturatingTarget)
            listener(saturatingTarget)
        }
    } else {
        load(url) {
            crossfade(250)
            target(saturatingTarget)
            listener(saturatingTarget)
        }
    }
}