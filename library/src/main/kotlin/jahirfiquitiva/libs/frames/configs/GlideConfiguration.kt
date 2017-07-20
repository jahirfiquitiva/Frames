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

package jahirfiquitiva.libs.frames.configs

import android.app.ActivityManager
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import com.bumptech.glide.Glide
import com.bumptech.glide.GlideBuilder
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.module.GlideModule

class GlideConfiguration:GlideModule {
    override fun applyOptions(context:Context?, builder:GlideBuilder?) {
        context?.let {
            builder?.setDecodeFormat(
                    if (it.runsMinSDK) if (it.isLowRamDevice) DecodeFormat.PREFER_RGB_565
                    else DecodeFormat.PREFER_ARGB_8888 else DecodeFormat.PREFER_RGB_565)
        }
    }

    override fun registerComponents(context:Context?, glide:Glide?) {}
}

val Context.maxPictureRes
    get() = if (runsMinSDK) if (isLowRamDevice) 30 else 55 else 50

val Context.bestBitmapConfig:Bitmap.Config
    get() {
        return if (runsMinSDK) if (isLowRamDevice) Bitmap.Config.RGB_565
        else Bitmap.Config.ARGB_8888 else Bitmap.Config.RGB_565
    }

val Context.runsMinSDK
    get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN

val Context.isLowRamDevice:Boolean
    get() {
        val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val lowRAMDevice:Boolean
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            lowRAMDevice = activityManager.isLowRamDevice
        } else {
            val memInfo = ActivityManager.MemoryInfo()
            activityManager.getMemoryInfo(memInfo)
            lowRAMDevice = memInfo.lowMemory
        }
        return lowRAMDevice
    }