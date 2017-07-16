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

import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.StateListDrawable
import android.support.annotation.ColorInt
import ca.allanwang.kau.utils.tint
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.request.FutureTarget
import jahirfiquitiva.libs.frames.utils.FramesKonfigs
import jahirfiquitiva.libs.frames.utils.PREFERENCES_NAME
import jahirfiquitiva.libs.kauextensions.extensions.getActiveIconsColorFor
import jahirfiquitiva.libs.kauextensions.extensions.getColorFromRes
import jahirfiquitiva.libs.kauextensions.extensions.getDrawable
import jahirfiquitiva.libs.kauextensions.extensions.getInactiveIconsColorFor
import java.io.File

fun Context.createHeartSelector():StateListDrawable {
    val res = StateListDrawable()
    res.addState(intArrayOf(android.R.attr.state_checked),
                 "ic_heart".getDrawable(this).tint(getColorFromRes(android.R.color.white)))
    res.addState(intArrayOf(-android.R.attr.state_checked),
                 "ic_heart_outline".getDrawable(this).tint(Color.parseColor("#80ffffff")))
    return res
}

fun Context.createHeartSelector(@ColorInt backgroundColor:Int):StateListDrawable {
    val res = StateListDrawable()
    res.addState(intArrayOf(android.R.attr.state_checked),
                 "ic_heart".getDrawable(this).tint(getActiveIconsColorFor(backgroundColor)))
    res.addState(intArrayOf(-android.R.attr.state_checked),
                 "ic_heart_outline".getDrawable(this).tint(
                         getInactiveIconsColorFor(backgroundColor)))
    return res
}

fun Context.downloadOnly(url:String, width:Int = 500, height:Int = 500):FutureTarget<File> =
        Glide.with(this).load(url).downloadOnly(width, height)

val Context.framesKonfigs:FramesKonfigs
    get() = FramesKonfigs.newInstance(PREFERENCES_NAME, this)

val Context.isInHorizontalMode:Boolean
    get() = resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE