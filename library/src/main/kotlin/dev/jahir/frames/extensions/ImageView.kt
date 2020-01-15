package dev.jahir.frames.extensions

import android.graphics.drawable.Drawable
import androidx.appcompat.widget.AppCompatImageView
import coil.Coil
import coil.api.load
import coil.transform.CircleCropTransformation
import dev.jahir.frames.ui.animations.SaturatingImageViewTarget

private const val CROSSFADE_DURATION = 250

private fun AppCompatImageView.internalLoadFramesPic(
    url: String,
    isForPalette: Boolean = false,
    cropAsCircle: Boolean = false,
    thumbnail: Drawable? = null,
    customTarget: SaturatingImageViewTarget? = null
) {
    Coil.load(context, url) {
        if (isForPalette) allowHardware(false)
        if (thumbnail == null && context.prefs.animationsEnabled) crossfade(CROSSFADE_DURATION)
        placeholder(thumbnail)
        error(thumbnail)
        if (cropAsCircle) transformations(CircleCropTransformation())
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
    placeholderName: String? = "",
    forceLoadFullRes: Boolean = false,
    cropAsCircle: Boolean = false,
    doWithPalette: ((drawable: Drawable?) -> Unit)? = null
) {
    if (!url.hasContent()) return
    val isForPalette = doWithPalette?.let { true } ?: false
    val saturatingTarget = buildTarget {
        if (isForPalette) addListener { doWithPalette?.invoke(it) }
        clearListenersOnSuccess = true
    }
    val placeholder = context.getDrawable(placeholderName)
    val shouldLoadThumbnail = thumbnail?.let { it.isNotEmpty() && it != url } ?: false
    if (shouldLoadThumbnail) {
        if (context.prefs.shouldLoadFullResPictures || forceLoadFullRes) {
            val thumbnailTarget = saturatingTarget.addListener {
                internalLoadFramesPic(url, isForPalette, cropAsCircle, it,
                    saturatingTarget.apply {
                        shouldActuallySaturate = false
                        clearListenersOnSuccess = true
                    })
            }
            internalLoadFramesPic(thumbnail.orEmpty(), isForPalette, cropAsCircle, placeholder,
                thumbnailTarget.apply {
                    clearListenersOnSuccess = false
                })
        } else {
            internalLoadFramesPic(
                thumbnail.orEmpty(), isForPalette, cropAsCircle,
                placeholder, saturatingTarget
            )
        }
    } else {
        internalLoadFramesPic(url, isForPalette, cropAsCircle, placeholder, saturatingTarget)
    }
}