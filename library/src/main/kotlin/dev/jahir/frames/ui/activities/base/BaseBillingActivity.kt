package dev.jahir.frames.ui.activities.base

import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import dev.jahir.frames.R
import dev.jahir.frames.data.listeners.BillingProcessesListener
import dev.jahir.frames.data.models.CleanSkuDetails
import dev.jahir.frames.data.models.DetailedPurchaseRecord
import dev.jahir.frames.data.viewmodels.BillingViewModel
import dev.jahir.frames.extensions.getAppName
import dev.jahir.frames.extensions.lazyViewModel
import dev.jahir.frames.extensions.mdDialog
import dev.jahir.frames.extensions.message
import dev.jahir.frames.extensions.negativeButton
import dev.jahir.frames.extensions.positiveButton
import dev.jahir.frames.extensions.singleChoiceItems
import dev.jahir.frames.extensions.title
import dev.jahir.frames.ui.fragments.viewer.DownloaderDialog
import dev.jahir.frames.utils.Prefs

@Suppress("MemberVisibilityCanBePrivate")
abstract class BaseBillingActivity<out P : Prefs> : BaseLicenseCheckerActivity<P>(),
    BillingProcessesListener {

    val billingViewModel: BillingViewModel by lazyViewModel()
    val isBillingClientReady: Boolean
        get() = billingEnabled && billingViewModel.isBillingClientReady

    private val loadingDialog: DownloaderDialog by lazy { DownloaderDialog.create() }
    private var purchasesDialog: AlertDialog? = null

    open val billingEnabled: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (billingEnabled) {
            billingViewModel.billingProcessesListener = this
            billingViewModel.initialize(this)
        }
    }

    private fun dismissDialogs() {
        try {
            loadingDialog.dismiss()
        } catch (e: Exception) {
        }
        try {
            purchasesDialog?.dismiss()
        } catch (e: Exception) {
        }
        purchasesDialog = null
    }

    override fun onDestroy() {
        super.onDestroy()
        dismissDialogs()
        billingViewModel.destroy(this)
    }

    fun showInAppPurchasesDialog() {
        if (!isBillingClientReady) {
            onSkuPurchaseError()
            return
        }
        val skuDetailsList =
            billingViewModel.inAppSkuDetails.map { CleanSkuDetails(it) }.orEmpty()
        if (skuDetailsList.isEmpty()) return
        dismissDialogs()
        purchasesDialog = mdDialog {
            title(R.string.donate)
            singleChoiceItems(skuDetailsList, 0) { _, which ->
                billingViewModel.launchBillingFlow(
                    this@BaseBillingActivity,
                    skuDetailsList[which].originalDetails
                )
            }
            negativeButton(android.R.string.cancel)
            positiveButton(R.string.donate)
        }
        purchasesDialog?.show()
    }

    override fun onSkuPurchaseSuccess(purchase: DetailedPurchaseRecord?) {
        dismissDialogs()
        purchasesDialog = mdDialog {
            title(R.string.donate_success_title)
            message(getString(R.string.donate_success_content, getAppName()))
            positiveButton(android.R.string.ok)
        }
        purchasesDialog?.show()
    }

    override fun onSkuPurchaseError(purchase: DetailedPurchaseRecord?) {
        dismissDialogs()
        purchasesDialog = mdDialog {
            title(R.string.error)
            message(getString(R.string.unexpected_error_occurred))
        }
        purchasesDialog?.show()
    }

    override fun onBillingClientReady() {
        super.onBillingClientReady()
        billingViewModel.queryInAppSkuDetailsList(getInAppPurchasesItemsIds())
        billingViewModel.querySubscriptionsSkuDetailsList(getSubscriptionsItemsIds())
    }

    open fun getInAppPurchasesItemsIds(): List<String> = try {
        resources.getStringArray(R.array.donation_items).asList()
    } catch (e: Exception) {
        listOf()
    }

    open fun getSubscriptionsItemsIds(): List<String> = listOf()
}