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
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import jahirfiquitiva.libs.frames.configs.GlideConfiguration


fun Drawable.toBitmap(context:Context):Bitmap {
    val bitmap:Bitmap
    if (this is BitmapDrawable) {
        this.bitmap?.let { return it }
    }
    if (intrinsicWidth <= 0 || intrinsicHeight <= 0) {
        bitmap = Bitmap.createBitmap(1, 1, GlideConfiguration.getBitmapsConfig(context))
    } else {
        bitmap = Bitmap.createBitmap(intrinsicWidth, intrinsicHeight,
                                     GlideConfiguration.getBitmapsConfig(context))
    }
    val canvas = Canvas(bitmap)
    setBounds(0, 0, intrinsicWidth, intrinsicHeight)
    draw(canvas)
    return bitmap
}