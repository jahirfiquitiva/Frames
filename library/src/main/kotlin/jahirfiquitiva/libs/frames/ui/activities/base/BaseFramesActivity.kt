/*
 * Copyright (c) 2019. Jahir Fiquitiva
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
package jahirfiquitiva.libs.frames.ui.activities.base

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.TextView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ViewModelProviders
import ca.allanwang.kau.utils.contentView
import ca.allanwang.kau.utils.openLink
import ca.allanwang.kau.utils.toast
import com.afollestad.materialdialogs.MaterialDialog
import com.anjlab.android.iab.v3.BillingProcessor
import com.anjlab.android.iab.v3.TransactionDetails
import com.github.javiersantos.piracychecker.PiracyChecker
import com.github.javiersantos.piracychecker.allow
import com.github.javiersantos.piracychecker.callback
import com.github.javiersantos.piracychecker.doNotAllow
import com.github.javiersantos.piracychecker.enums.InstallerID
import com.github.javiersantos.piracychecker.enums.PirateApp
import com.github.javiersantos.piracychecker.onError
import com.github.javiersantos.piracychecker.piracyChecker
import com.google.android.material.snackbar.Snackbar
import jahirfiquitiva.libs.frames.R
import jahirfiquitiva.libs.frames.data.models.Wallpaper
import jahirfiquitiva.libs.frames.helpers.extensions.mdDialog
import jahirfiquitiva.libs.frames.helpers.extensions.showChanges
import jahirfiquitiva.libs.frames.helpers.utils.ADW_ACTION
import jahirfiquitiva.libs.frames.helpers.utils.APPLY_ACTION
import jahirfiquitiva.libs.frames.helpers.utils.FramesKonfigs
import jahirfiquitiva.libs.frames.helpers.utils.ICONS_APPLIER
import jahirfiquitiva.libs.frames.helpers.utils.ICONS_PICKER
import jahirfiquitiva.libs.frames.helpers.utils.IMAGE_PICKER
import jahirfiquitiva.libs.frames.helpers.utils.MIN_TIME
import jahirfiquitiva.libs.frames.helpers.utils.NOVA_ACTION
import jahirfiquitiva.libs.frames.helpers.utils.PLAY_STORE_LINK_PREFIX
import jahirfiquitiva.libs.frames.helpers.utils.TURBO_ACTION
import jahirfiquitiva.libs.frames.helpers.utils.WALLS_PICKER
import jahirfiquitiva.libs.frames.viewmodels.IAPItem
import jahirfiquitiva.libs.frames.viewmodels.IAPViewModel
import jahirfiquitiva.libs.kext.extensions.buildSnackbar
import jahirfiquitiva.libs.kext.extensions.compliesWithMinTime
import jahirfiquitiva.libs.kext.extensions.getAppName
import jahirfiquitiva.libs.kext.extensions.hasContent
import jahirfiquitiva.libs.kext.extensions.isUpdate
import jahirfiquitiva.libs.kext.extensions.itemsSingleChoice
import jahirfiquitiva.libs.kext.extensions.stringArray

abstract class BaseFramesActivity<T : FramesKonfigs> : BaseWallpaperActionsActivity<T>(),
                                                       BillingProcessor.IBillingHandler {
    
    override fun lightTheme(): Int = R.style.LightTheme
    override fun darkTheme(): Int = R.style.DarkTheme
    override fun amoledTheme(): Int = R.style.AmoledTheme
    override fun transparentTheme(): Int = R.style.TransparentTheme
    
    var pickerKey: Int = 0
        private set
        get() {
            return intent?.let {
                when (it.action) {
                    APPLY_ACTION -> ICONS_APPLIER
                    ADW_ACTION, TURBO_ACTION, NOVA_ACTION -> ICONS_PICKER
                    Intent.ACTION_PICK, Intent.ACTION_GET_CONTENT -> IMAGE_PICKER
                    Intent.ACTION_SET_WALLPAPER -> WALLS_PICKER
                    else -> field
                }
            } ?: field ?: 0
        }
    
    var dialog: MaterialDialog? = null
    
    private var checker: PiracyChecker? = null
    private var donationsReady = false
    private var billingProcessor: BillingProcessor? = null
    
    override var wallpaper: Wallpaper? = null
    override val allowBitmapApply: Boolean = false
    
    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initDonations()
    }
    
    override fun setContentView(layoutResID: Int) {
        super.setContentView(layoutResID)
        startLicenseCheck()
    }
    
    override fun onSaveInstanceState(outState: Bundle?) {
        outState?.putInt("pickerKey", pickerKey)
        super.onSaveInstanceState(outState)
    }
    
    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        super.onRestoreInstanceState(savedInstanceState)
        pickerKey = savedInstanceState?.getInt("pickerKey") ?: 0
    }
    
    private fun initDonations() {
        if (donationsReady) return
        if (donationsEnabled && stringArray(R.array.donation_items).orEmpty().isNotEmpty()
            && BillingProcessor.isIabServiceAvailable(this)) {
            destroyBillingProcessor()
            getLicKey()?.let {
                billingProcessor = BillingProcessor(this, it, this)
                billingProcessor?.let {
                    if (!it.isInitialized) it.initialize()
                    try {
                        donationsEnabled = it.isOneTimePurchaseSupported
                    } catch (ignored: Exception) {
                    }
                    donationsReady = true
                } ?: { donationsEnabled = false }()
            } ?: { donationsEnabled = false }()
        } else {
            donationsEnabled = false
        }
    }
    
    private fun startLicenseCheck(force: Boolean = false) {
        val update = isUpdate
        if (update || !prefs.functionalDashboard || force) {
            checker = getLicenseChecker()
            checker?.let {
                with(it) {
                    callback {
                        allow { showLicensedSnack(update, force) }
                        doNotAllow { _, app ->
                            showNotLicensedDialog(app)
                        }
                        onError { showLicenseErrorDialog() }
                    }
                    start()
                }
            } ?: {
                prefs.functionalDashboard = true
                if (update) showChanges()
            }()
        }
    }
    
    fun getShortcut(): String {
        if (intent != null && intent.dataString != null &&
            intent.dataString.orEmpty().contains("_shortcut")) {
            return intent.dataString.orEmpty()
        }
        return ""
    }
    
    open var donationsEnabled = false
    open fun amazonInstallsEnabled(): Boolean = false
    open fun checkLPF(): Boolean = true
    open fun checkStores(): Boolean = true
    abstract fun getLicKey(): String?
    
    // Not really needed to override
    open fun getLicenseChecker(): PiracyChecker? {
        destroyChecker() // Important
        val licKey = getLicKey().orEmpty()
        return piracyChecker {
            if (licKey.hasContent() && licKey.length > 50)
                enableGooglePlayLicensing(licKey)
            enableInstallerId(InstallerID.GOOGLE_PLAY)
            if (amazonInstallsEnabled())
                enableInstallerId(InstallerID.AMAZON_APP_STORE)
            enableUnauthorizedAppsCheck(checkLPF())
            enableStoresCheck(checkStores())
            enableDebugCheck(true)
            enableEmulatorCheck(false)
            enableFoldersCheck(false)
            enableAPKCheck(false)
        }
    }
    
    internal fun showLicensedSnack(update: Boolean, force: Boolean = false) {
        destroyDialog()
        prefs.functionalDashboard = true
        if (!update || force) {
            showSnackbar(
                getString(R.string.license_valid_snack, getAppName()),
                Snackbar.LENGTH_SHORT)
        } else {
            showChanges()
        }
    }
    
    internal fun showNotLicensedDialog(pirateApp: PirateApp?) {
        destroyDialog()
        prefs.functionalDashboard = false
        val pirateAppName = pirateApp?.name ?: ""
        val content = if (pirateAppName.hasContent()) {
            getString(
                R.string.license_invalid_content, getAppName(),
                getString(R.string.license_invalid_content_extra, pirateAppName))
        } else {
            getString(R.string.license_invalid_content, getAppName(), "~")
        }
        dialog = mdDialog {
            title(R.string.license_invalid_title)
            message(text = content)
            positiveButton(android.R.string.ok) {
                prefs.functionalDashboard = false
                finish()
            }
            negativeButton(R.string.download) {
                prefs.functionalDashboard = false
                openLink(PLAY_STORE_LINK_PREFIX + packageName)
                finish()
            }
        }
        dialog?.setOnDismissListener {
            prefs.functionalDashboard = false
            finish()
        }
        dialog?.setOnCancelListener {
            prefs.functionalDashboard = false
            finish()
        }
        dialog?.show()
    }
    
    internal fun showLicenseErrorDialog() {
        destroyDialog()
        prefs.functionalDashboard = false
        dialog = mdDialog {
            title(R.string.error_title)
            message(R.string.license_error_content)
            positiveButton(android.R.string.ok) {
                prefs.functionalDashboard = false
                finish()
            }
            negativeButton(R.string.try_now) {
                prefs.functionalDashboard = false
                startLicenseCheck(true)
            }
        }
        dialog?.setOnDismissListener {
            prefs.functionalDashboard = false
            finish()
        }
        dialog?.setOnCancelListener {
            prefs.functionalDashboard = false
            finish()
        }
        dialog?.show()
    }
    
    fun doDonation() {
        initDonations()
        destroyDialog()
        if (!donationsReady) {
            showDonationErrorDialog(0, null)
            return
        }
        billingProcessor?.let {
            if (!it.isInitialized) it.initialize()
            if (it.isInitialized) {
                val donationViewModel = ViewModelProviders.of(this).get(IAPViewModel::class.java)
                donationViewModel.iapBillingProcessor = it
                donationViewModel.observe(this) {
                    if (it.size > 0) {
                        showDonationDialog(ArrayList(it))
                    } else {
                        showDonationErrorDialog(0, null)
                    }
                    donationViewModel.destroy(this)
                }
                destroyDialog()
                dialog = mdDialog {
                    message(R.string.loading)
                    cancelable(false)
                    cancelOnTouchOutside(false)
                }
                donationViewModel.loadData(stringArray(R.array.donation_items) ?: arrayOf(""), true)
                dialog?.show()
            }
        }
    }
    
    private fun showDonationDialog(items: ArrayList<IAPItem>) {
        destroyDialog()
        dialog = mdDialog {
            title(R.string.donate)
            itemsSingleChoice(items) { _, which, _ ->
                billingProcessor?.purchase(this@BaseFramesActivity, items[which].id)
            }
            negativeButton(android.R.string.cancel)
            positiveButton(R.string.donate)
        }
        dialog?.show()
    }
    
    private fun showDonationErrorDialog(error: Int, reason: String?) {
        destroyDialog()
        dialog = mdDialog {
            title(R.string.error_title)
            message(
                text = getString(
                    R.string.donate_error, error.toString(),
                    reason ?: getString(R.string.donate_error_unknown)))
        }
        dialog?.show()
    }
    
    override fun onProductPurchased(productId: String, details: TransactionDetails?) {
        billingProcessor?.let {
            if (it.consumePurchase(productId)) {
                destroyDialog()
                dialog = mdDialog {
                    title(R.string.donate_success_title)
                    message(text = getString(R.string.donate_success_content, getAppName()))
                    positiveButton(R.string.close)
                }
                dialog?.show()
            }
        }
    }
    
    override fun onBillingError(errorCode: Int, error: Throwable?) {
        showDonationErrorDialog(
            errorCode, error?.message ?: getString(R.string.donate_error_unknown))
        destroyBillingProcessor()
    }
    
    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    override fun onDestroy() {
        super.onDestroy()
        destroyDialog()
        destroyBillingProcessor()
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
    
    private fun destroyBillingProcessor() {
        billingProcessor?.release()
        billingProcessor = null
        donationsReady = false
    }
    
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (billingProcessor != null) {
            billingProcessor?.let {
                if (!(it.handleActivityResult(requestCode, resultCode, data))) {
                    super.onActivityResult(requestCode, resultCode, data)
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }
    
    override fun onBillingInitialized() {}
    override fun onPurchaseHistoryRestored() {}
    
    override fun applyBitmapWallpaper(
        toHomeScreen: Boolean, toLockScreen: Boolean, toBoth: Boolean,
        toOtherApp: Boolean
                                     ) {
    }
    
    override fun showSnackbar(
        text: String,
        duration: Int,
        defaultToToast: Boolean,
        settings: Snackbar.() -> Unit
                             ) {
        contentView?.let {
            val snack = it.buildSnackbar(text, duration, settings)
            
            val snackText = snack.view.findViewById<TextView>(R.id.snackbar_text)
            snackText.setTextColor(Color.WHITE)
            snackText.maxLines = 3
            
            snack.show()
        } ?: { if (defaultToToast) toast(text) }()
    }
    
    internal fun showWallpaperOptionsDialog(wallpaper: Wallpaper) {
        this.wallpaper = wallpaper
        destroyDialog()
        dialog = mdDialog {
            message(R.string.actions_dialog_content)
            positiveButton(R.string.apply) {
                it.dismiss()
                doItemClick(APPLY_ACTION_ID)
            }
            
            val actuallyComplies =
                getLicenseChecker()?.let { compliesWithMinTime(MIN_TIME) } ?: true
            
            if (wallpaper.downloadable && actuallyComplies) {
                negativeButton(R.string.download) {
                    it.dismiss()
                    doItemClick(DOWNLOAD_ACTION_ID)
                }
            }
        }
        dialog?.show()
    }
}
