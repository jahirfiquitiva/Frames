package dev.jahir.frames.extensions.resources

import android.content.res.Resources

/**
 * Utils originally created by Allan Wang
 * Available at https://github.com/AllanWang/KAU
 * I have added them here (copy/pasted) because this lib doesn't really use/need all its features
 * at a 100%.
 * Anyway, full credits go to Allan, for these awesome extensions
 */

inline val Float.dpToPx: Float
    get() = this * Resources.getSystem().displayMetrics.density

inline val Int.dpToPx: Int
    get() = toFloat().dpToPx.toInt()

inline val Float.pxToDp: Float
    get() = this / Resources.getSystem().displayMetrics.density

inline val Int.pxToDp: Int
    get() = toFloat().pxToDp.toInt()
