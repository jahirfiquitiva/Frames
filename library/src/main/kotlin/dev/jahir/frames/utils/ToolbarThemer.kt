package dev.jahir.frames.utils

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.os.Build
import android.view.Menu
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.appcompat.view.menu.ActionMenuItemView
import androidx.appcompat.widget.ActionMenuView
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.core.graphics.drawable.DrawableCompat
import dev.jahir.frames.R
import dev.jahir.frames.extensions.drawable
import dev.jahir.frames.extensions.string
import dev.jahir.frames.extensions.withAlpha
import java.lang.reflect.Field

fun Toolbar.tintIcons(@ColorInt iconsColor: Int, forceShowIcons: Boolean = false) {
    (0..childCount).forEach { i ->
        val v = getChildAt(i)

        //Step 1 : Changing the color of back button (or open drawer button).
        (v as? ImageButton)?.drawable?.tint(iconsColor)

        if (v is ActionMenuView) {
            //Step 2: Changing the color of any ActionMenuViews - icons that are not back
            // button, nor text, nor overflow menu icon.
            (0..v.childCount)
                .map {
                    v.getChildAt(it)
                }
                .filterIsInstance<ActionMenuItemView>()
                .forEach { innerView ->
                    innerView.compoundDrawables.forEach {
                        if (it != null) {
                            innerView.post {
                                it.tint(iconsColor)
                            }
                        }
                    }
                }
        }
    }

    // Step 3: Tint collapse icon
    try {
        val field = Toolbar::class.java.getDeclaredField("mCollapseIcon")
        field.isAccessible = true
        val collapseIcon = field.get(this) as? Drawable
        field.set(this, collapseIcon?.tint(iconsColor))
    } catch (e: Exception) {
    }

    // Step 4: Change the color of overflow menu icon.
    setOverflowButtonColor(iconsColor)

    // Step 5: Tint toolbar menu.
    menu?.tint(iconsColor, forceShowIcons)
}

fun Toolbar.tint(
    @ColorInt titleColor: Int, @ColorInt subtitleColor: Int = titleColor,
    @ColorInt iconsColor: Int = titleColor, forceShowIcons: Boolean = false
) {
    tintIcons(iconsColor, forceShowIcons)

    // Step 6: Changing the color of title and subtitle.
    setTitleTextColor(titleColor)
    setSubtitleTextColor(subtitleColor)
}

fun Menu.tint(@ColorInt iconsColor: Int, forceShowIcons: Boolean = false) {
    // Theme menu action views
    (0 until size()).forEach { i ->
        val item = getItem(i)
        item.icon?.tint(iconsColor)
        (item.actionView as? SearchView)?.tint(iconsColor)
    }

    // Display icons for easy UI understanding
    if (forceShowIcons) {
        try {
            val setOptionalIconsVisible = javaClass.getDeclaredMethod(
                "setOptionalIconsVisible",
                Boolean::class.javaPrimitiveType
            )
            if (!setOptionalIconsVisible.isAccessible) setOptionalIconsVisible.isAccessible = true
            setOptionalIconsVisible.invoke(this, true)
        } catch (ignored: Exception) {
        }
    }
}

fun Toolbar.setOverflowButtonColor(@ColorInt color: Int) {
    overflowIcon?.tint(color)
    @SuppressLint("PrivateResource")
    val overflowDescription = context.string(R.string.abc_action_menu_overflow_description)
    val outViews = ArrayList<View>()
    findViewsWithText(outViews, overflowDescription, View.FIND_VIEWS_WITH_CONTENT_DESCRIPTION)
    if (outViews.isEmpty()) return
    val overflow = outViews[0] as? AppCompatImageView
    overflow?.setImageDrawable(overflow.drawable.tint(color))
}

fun SearchView.tint(@ColorInt tintColor: Int, @ColorInt hintColor: Int = tintColor) {
    val cls = javaClass
    try {
        val mSearchSrcTextViewField = cls.getDeclaredField("mSearchSrcTextView")
        mSearchSrcTextViewField.isAccessible = true
        val mSearchSrcTextView = mSearchSrcTextViewField.get(this) as? EditText
        mSearchSrcTextView?.setTextColor(tintColor)
        mSearchSrcTextView?.setHintTextColor(
            if (hintColor == tintColor) hintColor.withAlpha(0.5F) else hintColor
        )
        mSearchSrcTextView?.tint(tintColor)

        var field = cls.getDeclaredField("mSearchButton")
        tintImageView(this, field, tintColor)
        field = cls.getDeclaredField("mGoButton")
        tintImageView(this, field, tintColor)
        field = cls.getDeclaredField("mCloseButton")
        tintImageView(this, field, tintColor)
        field = cls.getDeclaredField("mVoiceButton")
        tintImageView(this, field, tintColor)

        field = cls.getDeclaredField("mSearchPlate")
        field.isAccessible = true
        (field.get(this) as? View)?.background?.tint(tintColor)

        field = cls.getDeclaredField("mSearchHintIcon")
        field.isAccessible = true
        field.set(this, (field.get(this) as? Drawable)?.tint(tintColor))
    } catch (e: Exception) {
    }
}

private fun tintImageView(target: Any, field: Field, tintColor: Int) {
    field.isAccessible = true
    val imageView = field.get(target) as? ImageView
    imageView?.tint(tintColor)
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

@SuppressLint("RestrictedApi")
fun EditText.tint(@ColorInt color: Int) {
    val editTextColorStateList = ColorStateList.valueOf(color)
    if (this is AppCompatEditText) {
        supportBackgroundTintList = editTextColorStateList
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        backgroundTintList = editTextColorStateList
    }
    tintCursor(color)
}

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
    } catch (e: Exception) {
    }
}