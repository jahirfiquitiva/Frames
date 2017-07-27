/*
 * Copyright (c) 2017. Jahir Fiquitiva
 *
 * Licensed under the CreativeCommons Attribution-ShareAlike
 * 4.0 International License. You may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *    http://creativecommons.org/licenses/by-sa/4.0/legalcode
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jahirfiquitiva.libs.frames.extensions

import android.app.Activity
import android.content.Context
import android.graphics.Point
import android.os.Build
import android.util.DisplayMetrics
import android.view.Display
import android.view.WindowManager
import jahirfiquitiva.libs.frames.R
import jahirfiquitiva.libs.kauextensions.extensions.isInPortraitMode

val Activity.hasVisibleNavigationBar:Boolean
    get() {
        val resId = resources.getIdentifier("config_showNavigationBar", "bool", "android")
        return (resId > 0 && resources.getBoolean(resId))
    }

val Activity.navigationBarHeight:Int
    get() {
        var height = 0
        if (!hasVisibleNavigationBar) return height
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val isTablet = resources.getBoolean(R.bool.md_is_tablet)
            val resourceId:Int
            if (isTablet) {
                resourceId = resources.getIdentifier(
                        if (isInPortraitMode) "navigation_bar_height" else "navigation_bar_height_landscape",
                        "dimen", "android")
            } else {
                resourceId = resources.getIdentifier(
                        if (isInPortraitMode) "navigation_bar_height" else "navigation_bar_width",
                        "dimen", "android")
            }
            if (resourceId > 0) {
                height = resources.getDimensionPixelSize(resourceId)
            }
        }
        if (height <= 0) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                val metrics = DisplayMetrics()
                windowManager.defaultDisplay.getMetrics(metrics)
                val usableHeight = metrics.heightPixels
                windowManager.defaultDisplay.getRealMetrics(metrics)
                val realHeight = metrics.heightPixels
                if (realHeight > usableHeight) height = realHeight - usableHeight
            }
        }
        if (height <= 0) {
            val appUsableSize = getAppUsableScreenSize()
            val realScreenSize = getRealScreenSize()
            if (appUsableSize.x < realScreenSize.x) {
                val point = Point(realScreenSize.x - appUsableSize.x, appUsableSize.y)
                height = point.x
            }
            if (appUsableSize.y < realScreenSize.y) {
                val point = Point(appUsableSize.x, realScreenSize.y - appUsableSize.y)
                height = point.y
            }
        }
        return height
    }

fun Context.getAppUsableScreenSize():Point {
    val windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
    val display = windowManager.defaultDisplay
    val size = Point()
    display.getSize(size)
    return size
}

@Suppress("DEPRECATION")
fun Context.getRealScreenSize():Point {
    val windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
    val display = windowManager.defaultDisplay
    val size = Point()
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
        display.getRealSize(size)
    } else {
        try {
            size.x = Display::class.java.getMethod("getRawWidth").invoke(display) as Int
            size.y = Display::class.java.getMethod("getRawHeight").invoke(display) as Int
        } catch (ignored:Exception) {
            size.x = display.width
            size.y = display.height
        }
    }
    return size
}
