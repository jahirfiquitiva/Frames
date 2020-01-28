package dev.jahir.frames.ui.activities.base

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import com.anjlab.android.iab.v3.BillingProcessor
import com.anjlab.android.iab.v3.TransactionDetails
import dev.jahir.frames.R
import dev.jahir.frames.data.models.DonationItem
import dev.jahir.frames.data.viewmodels.DonationsViewModel
import dev.jahir.frames.extensions.getAppName
import dev.jahir.frames.extensions.mdDialog
import dev.jahir.frames.extensions.message
import dev.jahir.frames.extensions.negativeButton
import dev.jahir.frames.extensions.positiveButton
import dev.jahir.frames.extensions.singleChoiceItems
import dev.jahir.frames.extensions.title
import dev.jahir.frames.ui.fragments.viewer.DownloaderDialog
import dev.jahir.frames.utils.Prefs
import dev.jahir.frames.utils.postDelayed

abstract class BaseDonationsActivity<out P : Prefs> : BaseLicenseCheckerActivity<P>(),
    BillingProcessor.IBillingHandler {

    private val donationsViewModel: DonationsViewModel by lazy {
        ViewModelProvider(this).get(DonationsViewModel::class.java)
    }

    private val loadingDialog: DownloaderDialog by lazy { DownloaderDialog.create() }
    private var donationsDialog: AlertDialog? = null

    private var billingProcessor: BillingProcessor? = null
    private var donationsReady = false
    open val donationsEnabled: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        donationsViewModel.observe(this) {
            if (it.isNotEmpty()) {
                showDonationDialog(it)
            } else {
                onDonationError(0, null)
            }
        }
        initDonations()
    }

    private fun dismissDialogs() {
        loadingDialog.dismiss()
        donationsDialog?.dismiss()
        donationsDialog = null
    }

    override fun onDestroy() {
        super.onDestroy()
        dismissDialogs()
        destroyBillingProcessor()
    }

    private fun destroyBillingProcessor() {
        donationsViewModel.destroy(this)
        billingProcessor?.release()
        billingProcessor = null
        donationsReady = false
    }

    private fun initDonations() {
        if (donationsReady) return
        if (donationsEnabled && BillingProcessor.isIabServiceAvailable(this)) {
            destroyBillingProcessor()
            billingProcessor = BillingProcessor(this, getLicKey(), this)
            billingProcessor?.let {
                if (!it.isInitialized) it.initialize()
                try {
                    donationsReady = it.isOneTimePurchaseSupported || true
                } catch (ignored: Exception) {
                }
            } ?: {
                onBillingError(0, null)
            }()
        }
    }

    internal fun launchDonationsFlow() {
        initDonations()
        if (!donationsReady) {
            showDonationErrorDialog(0, null)
            return
        }

        dismissDialogs()
        loadingDialog.setOnShowListener {
            donationsViewModel.loadItems(
                billingProcessor,
                try {
                    resources.getStringArray(R.array.donation_items)
                } catch (e: Exception) {
                    arrayOf<String>()
                }
            )
        }
        loadingDialog.isCancelable = false
        loadingDialog.show(this)
        postDelayed(3000) { loadingDialog.isCancelable = true }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        billingProcessor?.let {
            if (!(it.handleActivityResult(requestCode, resultCode, data)))
                super.onActivityResult(requestCode, resultCode, data)
        } ?: { super.onActivityResult(requestCode, resultCode, data) }()
    }

    override fun onBillingInitialized() {
        donationsReady = true
    }

    override fun onPurchaseHistoryRestored() {}

    override fun onProductPurchased(productId: String, details: TransactionDetails?) {
        billingProcessor?.let {
            if (it.consumePurchase(productId)) {
                dismissDialogs()
                donationsDialog = mdDialog {
                    title(R.string.donate_success_title)
                    message(getString(R.string.donate_success_content, getAppName()))
                    positiveButton(android.R.string.ok)
                }
                donationsDialog?.show()
            }
        }
    }

    override fun onBillingError(errorCode: Int, error: Throwable?) {
        destroyBillingProcessor()
        onDonationError(errorCode, error?.message ?: error.toString())
    }

    internal fun purchase(productId: String) {
        try {
            billingProcessor?.purchase(this, productId)
        } catch (e: Exception) {
            onDonationError(0, e.message)
        }
    }

    private fun onDonationError(code: Int = 0, reason: String? = null) {
        showDonationErrorDialog(code, reason ?: getString(R.string.donate_error_unknown))
        destroyBillingProcessor()
    }

    private fun showDonationDialog(items: List<DonationItem>) {
        if (items.isEmpty()) return
        dismissDialogs()
        donationsDialog = mdDialog {
            title(R.string.donate)
            singleChoiceItems(items, 0) { _, which ->
                billingProcessor?.purchase(this@BaseDonationsActivity, items[which].id)
            }
            negativeButton(android.R.string.cancel)
            positiveButton(R.string.donate)
        }
        donationsDialog?.show()
    }

    private fun showDonationErrorDialog(error: Int, reason: String?) {
        destroyBillingProcessor()
        dismissDialogs()
        donationsDialog = mdDialog {
            title(R.string.error)
            message(
                getString(
                    R.string.donate_error, error.toString(),
                    reason ?: getString(R.string.donate_error_unknown)
                )
            )
        }
        donationsDialog?.show()
    }
}