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
package jahirfiquitiva.libs.frames.activities

import android.content.Intent
import android.os.Bundle
import com.github.javiersantos.piracychecker.PiracyChecker
import jahirfiquitiva.libs.frames.R
import jahirfiquitiva.libs.frames.utils.*
import jahirfiquitiva.libs.kauextensions.activities.ThemedActivity

abstract class BaseFramesActivity:ThemedActivity() {
    var picker:Int = 0
    var checker:PiracyChecker? = null

    override fun lightTheme():Int = R.style.LightTheme
    override fun darkTheme():Int = R.style.DarkTheme
    override fun amoledTheme():Int = R.style.AmoledTheme

    override fun onCreate(savedInstanceState:Bundle?) {
        super.onCreate(savedInstanceState)
        picker = getPickerKey()
    }

    internal fun startLicenseCheck() {
        checker = getLicenseChecker()
        checker?.start()
    }

    internal fun getShortcut():String {
        if (intent != null && intent.dataString != null && intent.dataString.contains(
                "_shortcut")) {
            return intent.dataString
        }
        return ""
    }

    internal fun getPickerKey():Int {
        if (intent != null && intent.action != null) {
            when (intent.action) {
                APPLY_ACTION -> return ICONS_APPLIER
                ADW_ACTION, TURBO_ACTION, NOVA_ACTION, Intent.ACTION_PICK, Intent.ACTION_GET_CONTENT -> return IMAGE_PICKER
                Intent.ACTION_SET_WALLPAPER -> return WALLS_PICKER
                else -> return 0
            }
        }
        return 0
    }

    abstract fun donationsEnabled():Boolean
    abstract fun amazonInstallsEnabled():Boolean
    abstract fun getLicKey():String?

    // Not really needed to override
    abstract fun getLicenseChecker():PiracyChecker?

    override fun onDestroy() {
        super.onDestroy()
        checker?.destroy()
        checker = null
    }

}