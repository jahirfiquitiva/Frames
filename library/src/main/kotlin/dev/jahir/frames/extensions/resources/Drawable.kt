package dev.jahir.frames.extensions.resources

import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.widget.ImageView
import androidx.annotation.ColorInt
import androidx.core.graphics.drawable.DrawableCompat

fun Drawable.asBitmap(
    scaling: Float = 1F,
    config: Bitmap.Config = Bitmap.Config.ARGB_8888
): Bitmap {
    if (this is BitmapDrawable && bitmap != null) {
        if (scaling == 1F) return bitmap
        val width = (bitmap.width * scaling).toInt()
        val height = (bitmap.height * scaling).toInt()
        return Bitmap.createScaledBitmap(bitmap, width, height, false)
    }
    val bitmap = if (intrinsicWidth <= 0 || intrinsicHeight <= 0)
        Bitmap.createBitmap(1, 1, config)
    else
        Bitmap.createBitmap(
            (intrinsicWidth * scaling).toInt(), (intrinsicHeight * scaling).toInt(), config
        )
    val canvas = Canvas(bitmap)
    setBounds(0, 0, canvas.width, canvas.height)
    draw(canvas)
    return bitmap
}

fun ImageView.tint(@ColorInt color: Int) {
    if (drawable != null) setImageDrawable(drawable.tint(color))
}

/**
 * Wrap the color into a state and tint the drawable
 */
fun Drawable.tint(@ColorInt color: Int): Drawable {
    val drawable = DrawableCompat.wrap(mutate())
    DrawableCompat.setTint(drawable, color)
    return drawable
}

/**
 * Tint the drawable with a given color state list
 */
fun Drawable.tint(state: ColorStateList): Drawable {
    val drawable = DrawableCompat.wrap(mutate())
    DrawableCompat.setTintList(drawable, state)
    return drawable
}