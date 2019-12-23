package dev.jahir.frames.extensions

import android.graphics.drawable.Drawable
import androidx.appcompat.widget.AppCompatImageView
import coil.Coil
import coil.api.load
import dev.jahir.frames.ui.animations.SaturatingImageViewTarget

private const val CROSSFADE_DURATION = 250

private fun AppCompatImageView.internalLoadFramesPic(
    url: String,
    isForPalette: Boolean = false,
    thumbnail: Drawable? = null,
    customTarget: SaturatingImageViewTarget? = null
) {
    Coil.load(context, url) {
        if (isForPalette) allowHardware(false)
        if (thumbnail == null) crossfade(CROSSFADE_DURATION)
        placeholder(thumbnail)
        error(thumbnail)
        customTarget?.let {
            target(customTarget)
            listener(customTarget)
        }
    }
}

private fun AppCompatImageView.buildTarget(
    block: SaturatingImageViewTarget.() -> Unit
): SaturatingImageViewTarget = SaturatingImageViewTarget(
    this
).apply(block)

fun AppCompatImageView.loadFramesPic(
    url: String,
    thumbnail: String? = url,
    doWithPalette: ((drawable: Drawable?) -> Unit)? = null
) {
    val isForPalette = doWithPalette?.let { true } ?: false
    val saturatingTarget = buildTarget {
        if (isForPalette) addListener { doWithPalette?.invoke(it) }
        clearListenersOnSuccess = true
    }
    val shouldLoadThumbnail = thumbnail?.let { it.isNotEmpty() && it != url } ?: false
    if (shouldLoadThumbnail) {
        val thumbnailTarget = saturatingTarget.addListener {
            internalLoadFramesPic(url, isForPalette, it,
                saturatingTarget.apply {
                    shouldActuallySaturate = false
                    clearListenersOnSuccess = true
                })
        }
        internalLoadFramesPic(thumbnail.orEmpty(), isForPalette, null, thumbnailTarget.apply {
            clearListenersOnSuccess = false
        })
    } else {
        internalLoadFramesPic(url, isForPalette, null, saturatingTarget)
    }
}