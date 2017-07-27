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
package jahirfiquitiva.libs.frames.activities.base

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.LifecycleRegistry
import android.arch.lifecycle.LifecycleRegistryOwner
import android.arch.lifecycle.Observer
import android.arch.lifecycle.OnLifecycleEvent
import android.content.Intent
import android.os.Bundle
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
import jahirfiquitiva.libs.frames.extensions.buildMaterialDialog
import jahirfiquitiva.libs.frames.extensions.framesKonfigs
import jahirfiquitiva.libs.frames.models.viewmodels.IAPItem
import jahirfiquitiva.libs.frames.models.viewmodels.IAPViewModel
import jahirfiquitiva.libs.frames.utils.*
import jahirfiquitiva.libs.kauextensions.activities.ThemedActivity
import jahirfiquitiva.libs.kauextensions.extensions.getAppName
import jahirfiquitiva.libs.kauextensions.extensions.getStringArray
import jahirfiquitiva.libs.kauextensions.extensions.hasContent
import jahirfiquitiva.libs.kauextensions.extensions.isFirstRunEver
import jahirfiquitiva.libs.kauextensions.extensions.justUpdated
import jahirfiquitiva.libs.kauextensions.extensions.printError
import jahirfiquitiva.libs.kauextensions.extensions.printInfo

@Suppress("LeakingThis")
abstract class BaseFramesActivity:ThemedActivity(), LifecycleRegistryOwner,
        LifecycleObserver, BillingProcessor.IBillingHandler {

    private var picker:Int = 0

    override fun lightTheme():Int = R.style.LightTheme
    override fun darkTheme():Int = R.style.DarkTheme
    override fun amoledTheme():Int = R.style.AmoledTheme
    override fun transparentTheme():Int = R.style.TransparentTheme
    override fun autoStatusBarTint():Boolean = true

    private var checker:PiracyChecker? = null
    private var dialog:MaterialDialog? = null
    internal var billingProcessor:BillingProcessor? = null

    val lcOwner = LifecycleRegistry(this)
    override fun getLifecycle():LifecycleRegistry = lcOwner

    override fun onCreate(savedInstanceState:Bundle?) {
        super.onCreate(savedInstanceState)
        picker = getPickerKey()
        if (donationsEnabled) {
            if (BillingProcessor.isIabServiceAvailable(this)) {
                destroyBillingProcessor()
                billingProcessor = BillingProcessor.newBillingProcessor(this, getLicKey(), this)
                billingProcessor?.let {
                    donationsEnabled = it.isOneTimePurchaseSupported
                }
            } else {
                donationsEnabled = false
            }
        }
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

    open var donationsEnabled = false
    open fun amazonInstallsEnabled():Boolean = false
    open fun checkLPF():Boolean = true
    open fun checkStores():Boolean = true
    abstract fun getLicKey():String?

    // Not really needed to override
    open fun getLicenseChecker():PiracyChecker? {
        destroyChecker() // Important
        val checker = PiracyChecker(this)
        getLicKey()?.let {
            if (it.hasContent() && it.length > 50) checker.enableGooglePlayLicensing(it)
        }
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

    internal fun initDonation() {
        destroyDialog()
        billingProcessor?.initialize()
        billingProcessor?.let {
            if (it.isInitialized) {
                val donationViewModel = IAPViewModel(it)
                donationViewModel.items.observe(this, Observer<ArrayList<IAPItem>> {
                    list ->
                    if (list != null) {
                        if (list.size > 0) {
                            showDonationDialog(list)
                        } else {
                            // TODO: Show some error
                        }
                    } else {
                        // TODO: Show some error
                    }
                })
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

    private fun showDonationDialog(items:ArrayList<IAPItem>) {
        destroyDialog()
        dialog = buildMaterialDialog {
            title(R.string.donate)
            items(items)
            itemsCallbackSingleChoice(0, { _, _, which, _ ->
                billingProcessor?.purchase(this@BaseFramesActivity, items[which].id)
                true
            })
            negativeText(android.R.string.cancel)
            positiveText(R.string.donate)
        }
        dialog?.show()
    }

    override fun onProductPurchased(productId:String?, details:TransactionDetails?) {
        printInfo("Product '$productId' has been purchased! Details: " + details.toString())
        productId?.let {
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
    }

    override fun onBillingError(errorCode:Int, error:Throwable?) {
        printError(
                "Unexpected error $errorCode occurred due to " + (error?.message ?: "unknown reasons"))
        // TODO: Show error dialog
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
    }

    override fun onActivityResult(requestCode:Int, resultCode:Int, data:Intent?) {
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

    override fun onBillingInitialized() {
        // Do nothing
    }

    override fun onPurchaseHistoryRestored() {
        // Do nothing
    }
}