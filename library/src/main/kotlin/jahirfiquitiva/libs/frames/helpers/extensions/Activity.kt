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
package jahirfiquitiva.libs.frames.helpers.extensions

import android.app.Activity
import android.content.Context
import android.graphics.Point
import android.os.Build
import android.view.Display
import android.view.WindowManager

val Activity.navigationBarHeight:Int
    get() {
        var height = 0
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
