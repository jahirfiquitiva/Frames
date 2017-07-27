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
import ca.allanwang.kau.utils.startLink
import com.afollestad.materialdialogs.MaterialDialog
import com.github.javiersantos.piracychecker.PiracyChecker
import com.github.javiersantos.piracychecker.enums.InstallerID
import com.github.javiersantos.piracychecker.enums.PiracyCheckerCallback
import com.github.javiersantos.piracychecker.enums.PiracyCheckerError
import com.github.javiersantos.piracychecker.enums.PirateApp
import jahirfiquitiva.libs.frames.R
import jahirfiquitiva.libs.frames.extensions.buildMaterialDialog
import jahirfiquitiva.libs.frames.extensions.framesKonfigs
import jahirfiquitiva.libs.frames.utils.*
import jahirfiquitiva.libs.kauextensions.activities.ThemedActivity
import jahirfiquitiva.libs.kauextensions.extensions.getAppName
import jahirfiquitiva.libs.kauextensions.extensions.hasContent
import jahirfiquitiva.libs.kauextensions.extensions.isFirstRunEver
import jahirfiquitiva.libs.kauextensions.extensions.justUpdated

abstract class BaseFramesActivity:ThemedActivity() {
    var picker:Int = 0
    var checker:PiracyChecker? = null

    override fun lightTheme():Int = R.style.LightTheme
    override fun darkTheme():Int = R.style.DarkTheme
    override fun amoledTheme():Int = R.style.AmoledTheme
    override fun transparentTheme():Int = R.style.ClearTheme
    override fun autoStatusBarTint():Boolean = true

    private var dialog:MaterialDialog? = null

    override fun onCreate(savedInstanceState:Bundle?) {
        super.onCreate(savedInstanceState)
        picker = getPickerKey()
    }

    internal fun startLicenseCheck() {
        if (isFirstRunEver || justUpdated || (!framesKonfigs.functionalDashboard)) {
            checker = getLicenseChecker()
            checker?.callback(object:PiracyCheckerCallback() {
                override fun allow() {
                    showLicensedDialog()
                }

                override fun dontAllow(error:PiracyCheckerError, app:PirateApp?) {
                    showNotLicensedDialog(app)
                }

                override fun onError(error:PiracyCheckerError) {
                    super.onError(error)
                    showLicenseErrorDialog()
                }
            })
            checker?.start()
        }
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

    open fun donationsEnabled():Boolean = false
    open fun amazonInstallsEnabled():Boolean = false
    open fun checkLPF():Boolean = true
    open fun checkStores():Boolean = true
    abstract fun getLicKey():String?

    // Not really needed to override
    open fun getLicenseChecker():PiracyChecker? {
        destroyChecker() // Important
        val checker = PiracyChecker(this)
        checker.enableGooglePlayLicensing(getLicKey())
        checker.enableInstallerId(InstallerID.GOOGLE_PLAY)
        if (amazonInstallsEnabled()) checker.enableInstallerId(InstallerID.AMAZON_APP_STORE)
        if (checkLPF()) checker.enableUnauthorizedAppsCheck()
        if (checkStores()) checker.enableStoresCheck()
        checker.enableEmulatorCheck(true).enableDebugCheck()
        return checker
    }

    internal fun showLicensedDialog() {
        destroyDialog()
        dialog = buildMaterialDialog {
            title(R.string.license_valid_title)
            content(getString(R.string.license_valid_content, getAppName()))
            positiveText(android.R.string.ok)
            onPositive { _, _ -> framesKonfigs.functionalDashboard = true }
        }
        dialog?.setOnDismissListener { framesKonfigs.functionalDashboard = true }
        dialog?.setOnCancelListener { framesKonfigs.functionalDashboard = true }
        dialog?.show()
    }

    internal fun showNotLicensedDialog(pirateApp:PirateApp?) {
        destroyDialog()
        val pirateAppName = pirateApp?.name ?: ""
        val content:String
        if (pirateAppName.hasContent()) {
            content = getString(R.string.license_invalid_content, getAppName(),
                                getString(R.string.license_invalid_content_extra, pirateAppName))
        } else {
            content = getString(R.string.license_invalid_content, getAppName())
        }
        dialog = buildMaterialDialog {
            title(R.string.license_invalid_title)
            content(content)
            positiveText(android.R.string.ok)
            neutralText(R.string.download)
            onPositive { _, _ ->
                framesKonfigs.functionalDashboard = false
                finish()
            }
            onNeutral { _, _ ->
                framesKonfigs.functionalDashboard = false
                startLink(PLAY_STORE_LINK_PREFIX + packageName)
                finish()
            }
        }
        dialog?.setOnDismissListener {
            framesKonfigs.functionalDashboard = false
            finish()
        }
        dialog?.setOnCancelListener {
            framesKonfigs.functionalDashboard = false
            finish()
        }
        dialog?.show()
    }

    internal fun showLicenseErrorDialog() {
        destroyDialog()
        dialog = buildMaterialDialog {
            title(R.string.license_error_title)
            content(R.string.license_error_content)
            positiveText(android.R.string.ok)
            neutralText(R.string.try_now)
            onPositive { _, _ ->
                framesKonfigs.functionalDashboard = false
                finish()
            }
            onNeutral { _, _ ->
                framesKonfigs.functionalDashboard = false
                startLicenseCheck()
            }
        }
        dialog?.setOnDismissListener {
            framesKonfigs.functionalDashboard = false
            finish()
        }
        dialog?.setOnCancelListener {
            framesKonfigs.functionalDashboard = false
            finish()
        }
        dialog?.show()
    }

    override fun onDestroy() {
        super.onDestroy()
        destroyChecker()
    }

    fun destroyChecker() {
        checker?.destroy()
        checker = null
    }

    fun destroyDialog() {
        dialog?.dismiss()
        dialog = null
    }

}