/*
 * Copyright (c) 2018. Jahir Fiquitiva
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
import android.graphics.Bitmap
import android.util.DisplayMetrics

fun Bitmap.adjustToDeviceScreen(activity: Activity): Bitmap {
    var flag = true
    
    val displayMetrics = DisplayMetrics()
    activity.windowManager.defaultDisplay.getMetrics(displayMetrics)
    
    val deviceWidth = displayMetrics.widthPixels
    val deviceHeight = displayMetrics.heightPixels
    
    if (width > deviceWidth) {
        flag = false
        var scaledHeight = deviceHeight
        val scaledWidth = scaledHeight * width / height
        try {
            if (scaledHeight > deviceHeight) {
                scaledHeight = deviceHeight
            }
            return Bitmap.createScaledBitmap(this, scaledWidth, scaledHeight, true)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    if (flag) {
        if (height > deviceHeight) {
            var scaledWidth = deviceHeight * width / height
            try {
                if (scaledWidth > deviceWidth)
                    scaledWidth = deviceWidth
                return Bitmap.createScaledBitmap(this, scaledWidth, deviceHeight, true)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    return this
}