package dev.jahir.frames.extensions.views

import android.graphics.drawable.Animatable
import android.graphics.drawable.Drawable
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.view.postDelayed
import coil.Coil
import coil.api.load
import coil.transform.CircleCropTransformation
import dev.jahir.frames.extensions.context.drawable
import dev.jahir.frames.extensions.context.preferences
import dev.jahir.frames.extensions.resources.hasContent
import dev.jahir.frames.ui.animations.SaturatingImageViewTarget

private const val CROSSFADE_DURATION = 300

private fun AppCompatImageView.internalLoadFramesPic(
    url: String?,
    isForPalette: Boolean = false,
    cropAsCircle: Boolean = false,
    thumbnail: Drawable? = null,
    customTarget: SaturatingImageViewTarget? = null
) {
    Coil.load(context, url.orEmpty()) {
        if (isForPalette) allowHardware(false)
        if (thumbnail == null && context.preferences.animationsEnabled)
            crossfade(CROSSFADE_DURATION)
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
): SaturatingImageViewTarget = SaturatingImageViewTarget(this).apply(block)

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
    val saturatingTarget = buildTarget { addListener { doWithPalette?.invoke(it) } }
    val placeholder = context.drawable(placeholderName)
    val shouldLoadThumbnail = thumbnail?.let { it.hasContent() && it != url } ?: false
    if (shouldLoadThumbnail) {
        if (context.preferences.shouldLoadFullResPictures || forceLoadFullRes) {
            val thumbnailTarget = saturatingTarget.apply {
                addListener {
                    internalLoadFramesPic(url, isForPalette, cropAsCircle, it,
                        buildTarget {
                            shouldActuallySaturate = false
                            addListener { drwbl -> doWithPalette?.invoke(drwbl) }
                        })
                }
            }
            internalLoadFramesPic(
                thumbnail, isForPalette, cropAsCircle, placeholder, thumbnailTarget
            )
        } else {
            internalLoadFramesPic(
                thumbnail, isForPalette, cropAsCircle, placeholder, saturatingTarget
            )
        }
    } else {
        internalLoadFramesPic(url, isForPalette, cropAsCircle, placeholder, saturatingTarget)
    }
}

fun ImageView.startAnimatable() {
    postDelayed(IMAGEVIEW_ANIMATABLE_DELAY) { (drawable as? Animatable)?.start() }
}

private const val IMAGEVIEW_ANIMATABLE_DELAY = 100L