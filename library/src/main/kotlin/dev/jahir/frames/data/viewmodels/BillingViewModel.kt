package dev.jahir.frames.data.viewmodels

import android.content.Context
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.observe
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.SkuDetails
import com.android.billingclient.api.SkuDetailsParams
import dev.jahir.frames.data.listeners.BillingProcessesListener
import dev.jahir.frames.data.models.DetailedPurchaseRecord
import dev.jahir.frames.extensions.asDetailedPurchase
import dev.jahir.frames.extensions.lazyMutableLiveData

@Suppress("MemberVisibilityCanBePrivate", "unused")
class BillingViewModel : ViewModel(), BillingClientStateListener, PurchasesUpdatedListener {

    var billingProcessesListener: BillingProcessesListener? = null

    var billingClient: BillingClient? = null
        private set

    private val subscriptionsPurchasesHistoryData: MutableLiveData<List<DetailedPurchaseRecord>> by lazyMutableLiveData()
    val subscriptionsPurchasesHistory: List<DetailedPurchaseRecord>
        get() = subscriptionsPurchasesHistoryData.value.orEmpty()

    private val inAppPurchasesHistoryData: MutableLiveData<List<DetailedPurchaseRecord>> by lazyMutableLiveData()
    val inAppPurchasesHistory: List<DetailedPurchaseRecord>
        get() = inAppPurchasesHistoryData.value.orEmpty()

    private val inAppSkuDetailsData: MutableLiveData<List<SkuDetails>> by lazyMutableLiveData()
    val inAppSkuDetails: List<SkuDetails>
        get() = inAppSkuDetailsData.value.orEmpty()

    private val subscriptionsSkuDetailsData: MutableLiveData<List<SkuDetails>> by lazyMutableLiveData()
    val subscriptionsSkuDetails: List<SkuDetails>
        get() = subscriptionsSkuDetailsData.value.orEmpty()

    private val billingClientReadyData: MutableLiveData<Boolean> by lazyMutableLiveData()
    val isBillingClientReady: Boolean
        get() = billingClientReadyData.value == true && billingClient?.isReady == true

    fun initialize(context: Context?, owner: LifecycleOwner?) {
        context ?: return
        billingClient = BillingClient
            .newBuilder(context)
            .setListener(this)
            .enablePendingPurchases()
            .build()
        owner?.let {
            destroy(it, false)
            internalInitializeObservers(it)
        }
        billingClient?.startConnection(this)
    }

    fun initialize(activity: FragmentActivity?) = initialize(activity, activity)

    private fun buildSkuDetailsParams(
        skuItemsIds: List<String>,
        @BillingClient.SkuType skuType: String
    ): SkuDetailsParams =
        SkuDetailsParams.newBuilder().setSkusList(skuItemsIds).setType(skuType).build()

    fun queryInAppSkuDetailsList(skuItemsIds: List<String>) {
        if (!isBillingClientReady || skuItemsIds.isNullOrEmpty()) return
        billingClient?.querySkuDetailsAsync(
            buildSkuDetailsParams(skuItemsIds, BillingClient.SkuType.INAPP)
        ) { billingResult2, detailsList ->
            if (billingResult2.responseCode == BillingClient.BillingResponseCode.OK) {
                inAppSkuDetailsData.postValue(detailsList)
            }
        }
    }

    fun querySubscriptionsSkuDetailsList(skuItemsIds: List<String>) {
        if (!isBillingClientReady || skuItemsIds.isNullOrEmpty()) return
        billingClient?.querySkuDetailsAsync(
            buildSkuDetailsParams(skuItemsIds, BillingClient.SkuType.SUBS)
        ) { billingResult2, detailsList ->
            if (billingResult2.responseCode == BillingClient.BillingResponseCode.OK) {
                subscriptionsSkuDetailsData.postValue(detailsList)
            }
        }
    }

    fun launchBillingFlow(activity: FragmentActivity?, skuDetails: SkuDetails?) {
        activity ?: return
        skuDetails ?: return
        billingClient?.launchBillingFlow(
            activity,
            BillingFlowParams.newBuilder().setSkuDetails(skuDetails).build()
        )
    }

    private fun queryPurchasesHistory(@BillingClient.SkuType skuType: String) {
        if (!isBillingClientReady) return
        billingClient?.queryPurchaseHistoryAsync(skuType) { result, history ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                val previousPurchases = mutableListOf<DetailedPurchaseRecord>()
                history?.forEachIndexed { _, it ->
                    it.asDetailedPurchase()?.let { detailed -> previousPurchases.add(detailed) }
                }
                val rightPreviousPurchases = previousPurchases.sortedBy { it.purchaseTime }
                when (skuType) {
                    BillingClient.SkuType.INAPP -> {
                        inAppPurchasesHistoryData.postValue(rightPreviousPurchases)
                    }
                    BillingClient.SkuType.SUBS -> {
                        subscriptionsPurchasesHistoryData.postValue(rightPreviousPurchases)
                    }
                }
            }
        }
    }

    private fun loadPastPurchases() {
        queryPurchasesHistory(BillingClient.SkuType.SUBS)
        queryPurchasesHistory(BillingClient.SkuType.INAPP)
    }

    private fun handlePurchase(purchase: Purchase) {
        if (!isBillingClientReady) return
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            if (!purchase.isAcknowledged) {
                val acknowledgePurchaseParams =
                    AcknowledgePurchaseParams.newBuilder()
                        .setPurchaseToken(purchase.purchaseToken)
                        .build()
                try {
                    billingClient?.acknowledgePurchase(acknowledgePurchaseParams) {
                        if (it.responseCode == BillingClient.BillingResponseCode.OK) {
                            billingProcessesListener?.onSkuPurchaseSuccess(purchase.asDetailedPurchase())
                        } else {
                            billingProcessesListener?.onSkuPurchaseError(purchase.asDetailedPurchase())
                        }
                    } ?: {
                        billingProcessesListener?.onSkuPurchaseError(purchase.asDetailedPurchase())
                    }()
                } catch (e: Exception) {
                    billingProcessesListener?.onSkuPurchaseError(purchase.asDetailedPurchase())
                }
            }
        }
    }

    override fun onBillingSetupFinished(billingResult: BillingResult?) {
        billingClientReadyData.postValue(billingResult?.responseCode == BillingClient.BillingResponseCode.OK)
    }

    override fun onBillingServiceDisconnected() {
        billingClientReadyData.postValue(false)
        inAppSkuDetailsData.postValue(null)
        inAppPurchasesHistoryData.postValue(null)
        subscriptionsSkuDetailsData.postValue(null)
        subscriptionsPurchasesHistoryData.postValue(null)
    }

    override fun onPurchasesUpdated(
        billingResult: BillingResult?,
        purchases: MutableList<Purchase>?
    ) {
        billingResult ?: return
        purchases ?: return
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK &&
            purchases.isNotEmpty()) {
            purchases.forEach { handlePurchase(it) }
            loadPastPurchases()
        }
    }

    private fun internalInitializeObservers(owner: LifecycleOwner) {
        billingClientReadyData.observe(owner) { ready ->
            if (ready) {
                loadPastPurchases()
                billingProcessesListener?.onBillingClientReady()
            }
        }
        inAppSkuDetailsData.observe(owner) {
            billingProcessesListener?.onInAppSkuDetailsListUpdated(it)
        }
        inAppPurchasesHistoryData.observe(owner) {
            billingProcessesListener?.onInAppPurchasesHistoryUpdated(it)
        }
        subscriptionsSkuDetailsData.observe(owner) {
            billingProcessesListener?.onSubscriptionsSkuDetailsListUpdated(it)
        }
        subscriptionsPurchasesHistoryData.observe(owner) {
            billingProcessesListener?.onSubscriptionsPurchasesHistoryUpdated(it)
        }
    }

    fun destroy(owner: LifecycleOwner, shouldDestroyBillingClient: Boolean = true) {
        inAppSkuDetailsData.removeObservers(owner)
        inAppPurchasesHistoryData.removeObservers(owner)
        subscriptionsSkuDetailsData.removeObservers(owner)
        subscriptionsPurchasesHistoryData.removeObservers(owner)
        billingClientReadyData.removeObservers(owner)
        if (shouldDestroyBillingClient) {
            billingClient?.endConnection()
            billingClient = null
            billingProcessesListener = null
        }
    }
}