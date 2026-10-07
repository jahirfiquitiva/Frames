package dev.jahir.frames.extensions.views

import android.graphics.drawable.Animatable
import android.graphics.drawable.Drawable
import android.widget.ImageView
import androidx.core.view.postDelayed
import coil3.load
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.request.error
import coil3.request.fallback
import coil3.request.placeholder
import coil3.request.transformations
import coil3.transform.CircleCropTransformation
import dev.jahir.frames.extensions.context.drawable
import dev.jahir.frames.extensions.context.preferences
import dev.jahir.frames.extensions.resources.hasContent
import dev.jahir.frames.ui.animations.SaturatingImageViewTarget

private const val CROSSFADE_DURATION = 200

private fun ImageView.buildSaturatingTarget(
    block: SaturatingImageViewTarget.() -> Unit
): SaturatingImageViewTarget = SaturatingImageViewTarget(this).apply(block)

private fun ImageView.buildRequestBuilder(
    thumbnail: Drawable?,
    cropAsCircle: Boolean,
    saturate: Boolean,
    onError: (() -> Unit)? = null,
    extra: ((drawable: Drawable?) -> Unit)? = null
): ImageRequest.Builder.() -> Unit = {
    fallback(thumbnail)
    placeholder(thumbnail)
    error(thumbnail)
    crossfade(if (thumbnail == null) CROSSFADE_DURATION else 0)

    if (cropAsCircle) transformations(CircleCropTransformation())

    val saturationTarget = buildSaturatingTarget {
        shouldActuallySaturate = saturate
        addListener { extra?.invoke(it) }
        onError?.let { addErrorListener(it) }
    }

    target(saturationTarget)
    listener(saturationTarget)
}

private fun ImageView.internalLoadFrames(
    url: String?,
    thumbnail: Drawable?,
    cropAsCircle: Boolean,
    saturate: Boolean,
    onError: (() -> Unit)? = null,
    extra: ((drawable: Drawable?) -> Unit)? = null
) {
    load(url, builder = buildRequestBuilder(thumbnail, cropAsCircle, saturate, onError, extra))
}

fun ImageView.loadFramesPic(
    url: String,
    thumbnailUrl: String? = url,
    placeholder: Drawable? = null,
    forceLoadFullRes: Boolean = false,
    cropAsCircle: Boolean = false,
    saturate: Boolean = true,
    onImageLoaded: ((drawable: Drawable?) -> Unit)? = null
) {
    val shouldLoadThumbnail = thumbnailUrl?.let { it.hasContent() && it != url } ?: false
    if (shouldLoadThumbnail) {
        // A thumbnail that cannot load (missing, or a format like SVG that Coil cannot decode)
        // falls back to the full image, so the card does not stay loading forever
        val loadFullResInstead = {
            internalLoadFrames(url, placeholder, cropAsCircle, saturate, extra = onImageLoaded)
        }
        if (context.preferences.shouldLoadFullResPictures || forceLoadFullRes) {
            internalLoadFrames(
                thumbnailUrl, placeholder, cropAsCircle, saturate, onError = loadFullResInstead
            ) {
                onImageLoaded?.invoke(it)
                internalLoadFrames(url, it, cropAsCircle, false, extra = onImageLoaded)
            }
        } else {
            internalLoadFrames(
                thumbnailUrl, placeholder, cropAsCircle, saturate,
                onError = loadFullResInstead, extra = onImageLoaded
            )
        }
    } else {
        internalLoadFrames(url, placeholder, cropAsCircle, saturate, extra = onImageLoaded)
    }
}

fun ImageView.loadFramesPicResPlaceholder(
    url: String,
    thumbnailUrl: String? = url,
    placeholderName: String? = "",
    forceLoadFullRes: Boolean = false,
    cropAsCircle: Boolean = false,
    saturate: Boolean = true,
    onImageLoaded: ((drawable: Drawable?) -> Unit)? = null
) = loadFramesPic(
    url, thumbnailUrl,
    context.drawable(placeholderName),
    forceLoadFullRes, cropAsCircle, saturate,
    onImageLoaded
)

fun ImageView.startAnimatable() {
    postDelayed(IMAGEVIEW_ANIMATABLE_DELAY) { (drawable as? Animatable)?.start() }
}

private const val IMAGEVIEW_ANIMATABLE_DELAY = 75L
