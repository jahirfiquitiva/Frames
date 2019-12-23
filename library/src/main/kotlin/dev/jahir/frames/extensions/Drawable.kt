package dev.jahir.frames.extensions

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable

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