package dev.jahir.frames.ui.activities.base

import androidx.appcompat.app.AlertDialog
import com.github.javiersantos.piracychecker.PiracyChecker
import com.github.javiersantos.piracychecker.allow
import com.github.javiersantos.piracychecker.callback
import com.github.javiersantos.piracychecker.doNotAllow
import com.github.javiersantos.piracychecker.enums.InstallerID
import com.github.javiersantos.piracychecker.enums.PirateApp
import com.github.javiersantos.piracychecker.onError
import com.github.javiersantos.piracychecker.piracyChecker
import dev.jahir.frames.R
import dev.jahir.frames.extensions.getAppName
import dev.jahir.frames.extensions.hasContent
import dev.jahir.frames.extensions.isUpdate
import dev.jahir.frames.extensions.mdDialog
import dev.jahir.frames.extensions.message
import dev.jahir.frames.extensions.negativeButton
import dev.jahir.frames.extensions.openLink
import dev.jahir.frames.extensions.positiveButton
import dev.jahir.frames.extensions.showSnackbar
import dev.jahir.frames.extensions.title
import dev.jahir.frames.utils.Prefs
import dev.jahir.frames.utils.postDelayed

abstract class BaseLicenseCheckerActivity<out P : Prefs> : BaseChangelogDialogActivity<P>() {

    var licenseCheckEnabled: Boolean = false
        private set

    private var checker: PiracyChecker? = null

    private var licenseCheckDialog: AlertDialog? = null

    open fun amazonInstallsEnabled(): Boolean = false
    open fun checkLPF(): Boolean = true
    open fun checkStores(): Boolean = true
    abstract fun getLicKey(): String?

    override fun setContentView(layoutResID: Int) {
        super.setContentView(layoutResID)
        postDelayed(50) { startLicenseCheck() }
    }

    override fun onDestroy() {
        super.onDestroy()
        destroyChecker()
        licenseCheckDialog?.dismiss()
        licenseCheckDialog = null
    }

    fun destroyChecker() {
        checker?.destroy()
        checker = null
    }

    private fun startLicenseCheck(force: Boolean = false) {
        destroyChecker()
        checker = getLicenseChecker()
        checker?.let {
            licenseCheckEnabled = true
            if (isUpdate || !prefs.functionalDashboard || force) {
                with(it) {
                    callback {
                        allow { showLicensedSnack(isUpdate, force) }
                        doNotAllow { _, app -> showNotLicensedDialog(app) }
                        onError { showLicenseErrorDialog() }
                    }
                    start()
                }
            }
        } ?: {
            licenseCheckEnabled = false
            prefs.functionalDashboard = true
            if (isUpdate) showChangelog()
        }()
    }

    // Not really needed to override
    open fun getLicenseChecker(): PiracyChecker? {
        destroyChecker()
        val licKey = getLicKey().orEmpty()
        return piracyChecker {
            if (licKey.hasContent() && licKey.length > 50) enableGooglePlayLicensing(licKey)
            enableInstallerId(InstallerID.GOOGLE_PLAY)
            if (amazonInstallsEnabled()) enableInstallerId(InstallerID.AMAZON_APP_STORE)
            if (checkLPF()) enableUnauthorizedAppsCheck()
            if (checkStores()) enableStoresCheck()
            enableDebugCheck()
        }
    }

    private fun showLicensedSnack(update: Boolean, force: Boolean = false) {
        licenseCheckDialog?.dismiss()
        prefs.functionalDashboard = true
        if (!update || force) {
            showSnackbar(getString(R.string.license_valid_snack, getAppName()))
        } else {
            showChangelog()
        }
    }

    private fun showNotLicensedDialog(pirateApp: PirateApp?) {
        licenseCheckDialog?.dismiss()
        prefs.functionalDashboard = false
        val pirateAppName = pirateApp?.name ?: ""
        val content = if (pirateAppName.hasContent()) {
            getString(
                R.string.license_invalid_content, getAppName(),
                getString(R.string.license_invalid_content_extra, pirateAppName)
            )
        } else {
            getString(R.string.license_invalid_content, getAppName(), "~")
        }
        licenseCheckDialog = mdDialog {
            title(R.string.license_invalid_title)
            message(content)
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
        licenseCheckDialog?.setOnDismissListener {
            prefs.functionalDashboard = false
            finish()
        }
        licenseCheckDialog?.setOnCancelListener {
            prefs.functionalDashboard = false
            finish()
        }
        licenseCheckDialog?.show()
    }

    private fun showLicenseErrorDialog() {
        licenseCheckDialog?.dismiss()
        prefs.functionalDashboard = false
        licenseCheckDialog = mdDialog {
            title(R.string.error)
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
        licenseCheckDialog?.setOnDismissListener {
            prefs.functionalDashboard = false
            finish()
        }
        licenseCheckDialog?.setOnCancelListener {
            prefs.functionalDashboard = false
            finish()
        }
        licenseCheckDialog?.show()
    }

    companion object {
        const val PLAY_STORE_LINK_PREFIX = "https://play.google.com/store/apps/details?id="
    }
}