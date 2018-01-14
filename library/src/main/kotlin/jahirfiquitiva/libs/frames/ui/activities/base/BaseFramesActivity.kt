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
package jahirfiquitiva.libs.frames.ui.activities.base

import android.annotation.SuppressLint
import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.OnLifecycleEvent
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.design.widget.BaseTransientBottomBar
import android.support.design.widget.Snackbar
import android.widget.TextView
import ca.allanwang.kau.utils.startLink
import com.afollestad.materialdialogs.MaterialDialog
import com.anjlab.android.iab.v3.BillingProcessor
import com.anjlab.android.iab.v3.TransactionDetails
import com.github.javiersantos.piracychecker.PiracyChecker
import com.github.javiersantos.piracychecker.enums.InstallerID
import com.github.javiersantos.piracychecker.enums.PiracyCheckerCallback
import com.github.javiersantos.piracychecker.enums.PiracyCheckerError
import com.github.javiersantos.piracychecker.enums.PirateApp
import jahirfiquitiva.libs.frames.R
import jahirfiquitiva.libs.frames.data.models.Wallpaper
import jahirfiquitiva.libs.frames.helpers.extensions.buildMaterialDialog
import jahirfiquitiva.libs.frames.helpers.extensions.framesKonfigs
import jahirfiquitiva.libs.frames.helpers.utils.ADW_ACTION
import jahirfiquitiva.libs.frames.helpers.utils.APPLY_ACTION
import jahirfiquitiva.libs.frames.helpers.utils.FL
import jahirfiquitiva.libs.frames.helpers.utils.ICONS_APPLIER
import jahirfiquitiva.libs.frames.helpers.utils.IMAGE_PICKER
import jahirfiquitiva.libs.frames.helpers.utils.NOVA_ACTION
import jahirfiquitiva.libs.frames.helpers.utils.PLAY_STORE_LINK_PREFIX
import jahirfiquitiva.libs.frames.helpers.utils.TURBO_ACTION
import jahirfiquitiva.libs.frames.helpers.utils.WALLS_PICKER
import jahirfiquitiva.libs.frames.providers.viewmodels.IAPItem
import jahirfiquitiva.libs.frames.providers.viewmodels.IAPViewModel
import jahirfiquitiva.libs.kauextensions.extensions.buildSnackbar
import jahirfiquitiva.libs.kauextensions.extensions.getAppName
import jahirfiquitiva.libs.kauextensions.extensions.getStringArray
import jahirfiquitiva.libs.kauextensions.extensions.hasContent
import jahirfiquitiva.libs.kauextensions.extensions.isFirstRun
import jahirfiquitiva.libs.kauextensions.extensions.isFirstRunEver
import jahirfiquitiva.libs.kauextensions.extensions.isUpdate
import jahirfiquitiva.libs.kauextensions.extensions.justUpdated
import jahirfiquitiva.libs.kauextensions.extensions.showToast
import org.jetbrains.anko.contentView

abstract class BaseFramesActivity : BaseWallpaperActionsActivity(),
                                    BillingProcessor.IBillingHandler {
    
    override fun lightTheme(): Int = R.style.LightTheme
    override fun darkTheme(): Int = R.style.DarkTheme
    override fun amoledTheme(): Int = R.style.AmoledTheme
    override fun transparentTheme(): Int = R.style.TransparentTheme
    
    var picker: Int = 0
        private set
    var dialog: MaterialDialog? = null
    
    private var checker: PiracyChecker? = null
    private var donationsReady = false
    internal var billingProcessor: BillingProcessor? = null
    
    override var wallpaper: Wallpaper? = null
    override val allowBitmapApply: Boolean = false
    
    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        picker = getPickerKey()
        initDonations()
    }
    
    override fun setContentView(layoutResID: Int) {
        super.setContentView(layoutResID)
        startLicenseCheck()
    }
    
    override fun onSaveInstanceState(outState: Bundle?) {
        outState?.putInt("pickerKey", picker)
        super.onSaveInstanceState(outState)
    }
    
    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        super.onRestoreInstanceState(savedInstanceState)
        picker = savedInstanceState?.getInt("pickerKey") ?: 0
    }
    
    internal fun initDonations() {
        if (donationsReady) return
        if (donationsEnabled && getStringArray(R.array.donation_items).isNotEmpty()
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
        if (isFirstRun || isUpdate || !framesKonfigs.functionalDashboard || force) {
            checker = getLicenseChecker()
            checker?.let {
                with(it) {
                    callback(
                            object : PiracyCheckerCallback() {
                                override fun allow() = showLicensedSnack()
                                
                                override fun dontAllow(error: PiracyCheckerError, app: PirateApp?) =
                                        showNotLicensedDialog(app)
                                
                                override fun onError(error: PiracyCheckerError) {
                                    super.onError(error)
                                    showLicenseErrorDialog()
                                }
                            })
                    start()
                }
            } ?: { framesKonfigs.functionalDashboard = true }()
        }
    }
    
    fun getShortcut(): String {
        if (intent != null && intent.dataString != null &&
                intent.dataString.contains("_shortcut")) {
            return intent.dataString
        }
        return ""
    }
    
    internal fun getPickerKey(): Int {
        if (intent != null && intent.action != null) {
            return when (intent.action) {
                APPLY_ACTION -> ICONS_APPLIER
                ADW_ACTION, TURBO_ACTION, NOVA_ACTION, Intent.ACTION_PICK, Intent.ACTION_GET_CONTENT -> IMAGE_PICKER
                Intent.ACTION_SET_WALLPAPER -> WALLS_PICKER
                else -> 0
            }
        }
        return 0
    }
    
    open var donationsEnabled = false
    open fun amazonInstallsEnabled(): Boolean = false
    open fun checkLPF(): Boolean = true
    open fun checkStores(): Boolean = true
    abstract fun getLicKey(): String?
    
    // Not really needed to override
    open fun getLicenseChecker(): PiracyChecker? {
        destroyChecker() // Important
        val prvChecker = PiracyChecker(this)
        getLicKey()?.let {
            if (it.hasContent() && it.length > 50) prvChecker.enableGooglePlayLicensing(it)
        }
        prvChecker.apply {
            enableInstallerId(InstallerID.GOOGLE_PLAY)
            if (amazonInstallsEnabled()) enableInstallerId(InstallerID.AMAZON_APP_STORE)
            if (checkLPF()) enableUnauthorizedAppsCheck()
            if (checkStores()) enableStoresCheck()
            enableEmulatorCheck(false)
            enableDebugCheck()
            enableFoldersCheck(false)
        }
        return prvChecker
    }
    
    @Deprecated("Use showLicensedSnack() instead")
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
    
    internal fun showLicensedSnack() {
        destroyDialog()
        showSnackbar(
                getString(R.string.license_valid_snack, getAppName()),
                Snackbar.LENGTH_SHORT) {
            addCallback(
                    object : Snackbar.Callback() {
                        override fun onShown(sb: Snackbar?) {
                            super.onShown(sb)
                            framesKonfigs.functionalDashboard = true
                        }
                        
                        override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                            super.onDismissed(transientBottomBar, event)
                            framesKonfigs.functionalDashboard = true
                        }
                    })
        }
    }
    
    internal fun showNotLicensedDialog(pirateApp: PirateApp?) {
        destroyDialog()
        val pirateAppName = pirateApp?.name ?: ""
        val content = if (pirateAppName.hasContent()) {
            getString(
                    R.string.license_invalid_content, getAppName(),
                    getString(R.string.license_invalid_content_extra, pirateAppName))
        } else {
            getString(R.string.license_invalid_content, getAppName(), "~")
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
            title(R.string.error_title)
            content(R.string.license_error_content)
            positiveText(android.R.string.ok)
            neutralText(R.string.try_now)
            onPositive { _, _ ->
                framesKonfigs.functionalDashboard = false
                finish()
            }
            onNeutral { _, _ ->
                framesKonfigs.functionalDashboard = false
                startLicenseCheck(true)
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
                donationViewModel.observe(
                        this, {
                    if (it.size > 0) {
                        showDonationDialog(ArrayList(it))
                    } else {
                        showDonationErrorDialog(0, null)
                    }
                    donationViewModel.destroy(this)
                })
                destroyDialog()
                dialog = buildMaterialDialog {
                    content(R.string.loading)
                    progress(true, 0)
                    cancelable(false)
                }
                donationViewModel.loadData(getStringArray(R.array.donation_items), true)
                dialog?.show()
            }
        }
    }
    
    private fun showDonationDialog(items: ArrayList<IAPItem>) {
        destroyDialog()
        dialog = buildMaterialDialog {
            title(R.string.donate)
            items(items)
            itemsCallbackSingleChoice(
                    0, { _, _, which, _ ->
                billingProcessor?.purchase(this@BaseFramesActivity, items[which].id)
                true
            })
            negativeText(android.R.string.cancel)
            positiveText(R.string.donate)
        }
        dialog?.show()
    }
    
    private fun showDonationErrorDialog(error: Int, reason: String?) {
        destroyDialog()
        dialog = buildMaterialDialog {
            title(R.string.error_title)
            content(
                    getString(
                            R.string.donate_error, error.toString(),
                            reason ?: getString(R.string.donate_error_unknown)))
        }
        dialog?.show()
    }
    
    override fun onProductPurchased(productId: String, details: TransactionDetails?) {
        billingProcessor?.let {
            if (it.consumePurchase(productId)) {
                destroyDialog()
                dialog = buildMaterialDialog {
                    title(R.string.donate_success_title)
                    content(getString(R.string.donate_success_content, getAppName()))
                    positiveText(R.string.close)
                }
                dialog?.show()
            }
        }
    }
    
    override fun onBillingError(errorCode: Int, error: Throwable?) {
        showDonationErrorDialog(
                errorCode,
                error?.message ?: getString(R.string.donate_error_unknown))
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
    
    fun destroyBillingProcessor() {
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
        } ?: { if (defaultToToast) showToast(text) }()
    }
    
    internal fun showWallpaperOptionsDialog(wallpaper: Wallpaper) {
        this.wallpaper = wallpaper
        destroyDialog()
        dialog = buildMaterialDialog {
            content(R.string.actions_dialog_content)
            positiveText(R.string.apply)
            onPositive { dialog, _ ->
                dialog.dismiss()
                doItemClick(APPLY_ACTION_ID)
            }
            if (wallpaper.downloadable) {
                negativeText(R.string.download)
                onNegative { dialog, _ ->
                    dialog.dismiss()
                    doItemClick(DOWNLOAD_ACTION_ID)
                }
            }
        }
        dialog?.show()
    }
}