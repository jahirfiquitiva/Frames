package dev.jahir.frames.extensions.views

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.widget.EditText
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.appcompat.widget.AppCompatEditText
import dev.jahir.frames.extensions.context.drawable
import dev.jahir.frames.extensions.resources.tint

@SuppressLint("RestrictedApi")
fun EditText.tint(@ColorInt color: Int) {
    val editTextColorStateList = ColorStateList.valueOf(color)
    if (this is AppCompatEditText) {
        supportBackgroundTintList = editTextColorStateList
    } else backgroundTintList = editTextColorStateList
    tintCursor(color)
}

@SuppressLint("DiscouragedPrivateApi", "SoonBlockedPrivateApi")
fun EditText.tintCursor(@ColorInt color: Int) {
    try {
        val fCursorDrawableRes = TextView::class.java.getDeclaredField("mCursorDrawableRes")
        fCursorDrawableRes.isAccessible = true
        val mCursorDrawableRes = fCursorDrawableRes.getInt(this)
        val fEditor = TextView::class.java.getDeclaredField("mEditor")
        fEditor.isAccessible = true
        val editor = fEditor.get(this)
        val clazz = editor.javaClass
        val fCursorDrawable = clazz.getDeclaredField("mCursorDrawable")
        fCursorDrawable.isAccessible = true
        val drawables: Array<Drawable?> = Array(2) {
            val drawable = context.drawable(mCursorDrawableRes)
            drawable?.tint(color)
            drawable
        }
        fCursorDrawable.set(editor, drawables)
    } catch (_: Exception) {
    }
}
