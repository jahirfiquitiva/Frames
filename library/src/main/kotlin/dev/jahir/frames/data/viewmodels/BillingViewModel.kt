package dev.jahir.frames.data.viewmodels

import android.app.Application
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
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
import dev.jahir.frames.extensions.utils.asDetailedPurchase
import dev.jahir.frames.extensions.utils.context
import dev.jahir.frames.extensions.utils.lazyMutableLiveData
import dev.jahir.frames.extensions.utils.tryToObserve
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Suppress("MemberVisibilityCanBePrivate", "unused")
class BillingViewModel(application: Application) : AndroidViewModel(application),
    BillingClientStateListener, PurchasesUpdatedListener {

    var billingProcessesListener: BillingProcessesListener? = null

    private var billingClient: BillingClient? = null

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

    fun initialize() {
        billingClient = BillingClient
            .newBuilder(context)
            .setListener(this)
            .enablePendingPurchases()
            .build()
        billingClient?.startConnection(this)
    }

    private fun buildSkuDetailsParams(
        skuItemsIds: List<String>,
        @BillingClient.SkuType skuType: String
    ): SkuDetailsParams =
        SkuDetailsParams.newBuilder().setSkusList(skuItemsIds).setType(skuType).build()

    private suspend fun internalQuerySkuDetailsList(
        skuItemsIds: List<String>,
        @BillingClient.SkuType skuType: String
    ) {
        if (!isBillingClientReady || skuItemsIds.isNullOrEmpty()) return
        withContext(IO) {
            billingClient?.querySkuDetailsAsync(
                buildSkuDetailsParams(skuItemsIds, skuType)
            ) { billingResult2, detailsList ->
                if (billingResult2.responseCode == BillingClient.BillingResponseCode.OK) {
                    when (skuType) {
                        BillingClient.SkuType.INAPP -> {
                            inAppSkuDetailsData.postValue(detailsList.sortedBy { it.priceAmountMicros })
                        }
                        BillingClient.SkuType.SUBS -> {
                            subscriptionsSkuDetailsData.postValue(detailsList.sortedBy { it.priceAmountMicros })
                        }
                    }
                }
            }
        }
    }

    fun queryInAppSkuDetailsList(skuItemsIds: List<String>) {
        viewModelScope.launch {
            internalQuerySkuDetailsList(skuItemsIds, BillingClient.SkuType.INAPP)
        }
    }

    fun querySubscriptionsSkuDetailsList(skuItemsIds: List<String>) {
        viewModelScope.launch {
            internalQuerySkuDetailsList(skuItemsIds, BillingClient.SkuType.SUBS)
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

    private fun postPurchasesHistory(
        @BillingClient.SkuType skuType: String,
        newPurchases: List<DetailedPurchaseRecord>
    ) {
        val actualPurchases = ArrayList(
            when (skuType) {
                BillingClient.SkuType.INAPP -> inAppPurchasesHistory
                BillingClient.SkuType.SUBS -> subscriptionsPurchasesHistory
                else -> listOf()
            }
        )
        actualPurchases.addAll(newPurchases)
        when (skuType) {
            BillingClient.SkuType.INAPP -> {
                inAppPurchasesHistoryData.postValue(
                    actualPurchases.sortedByDescending { it.purchaseTime }
                )
            }
            BillingClient.SkuType.SUBS -> {
                subscriptionsPurchasesHistoryData.postValue(
                    actualPurchases.sortedByDescending { it.purchaseTime }
                )
            }
        }
    }

    private suspend fun queryPurchases(@BillingClient.SkuType skuType: String) {
        if (!isBillingClientReady) return
        withContext(IO) {
            billingClient?.queryPurchases(skuType)?.let {
                if (it.billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    postPurchasesHistory(skuType,
                        it.purchasesList.orEmpty()
                            .mapNotNull { purchase -> purchase.asDetailedPurchase() })
                }
            }
        }
    }

    private suspend fun queryPurchasesHistory(@BillingClient.SkuType skuType: String) {
        if (!isBillingClientReady) return
        withContext(IO) {
            billingClient?.queryPurchaseHistoryAsync(skuType) { billingResult, purchaseHistoryRecordList ->
                if (billingResult?.responseCode == BillingClient.BillingResponseCode.OK) {
                    postPurchasesHistory(skuType,
                        purchaseHistoryRecordList.orEmpty()
                            .mapNotNull { purchase -> purchase.asDetailedPurchase() })
                }
            }
        }
    }

    fun loadPastPurchases() {
        if (!isBillingClientReady) return
        viewModelScope.launch {
            queryPurchasesHistory(BillingClient.SkuType.SUBS)
            queryPurchases(BillingClient.SkuType.SUBS)
            queryPurchasesHistory(BillingClient.SkuType.INAPP)
            queryPurchases(BillingClient.SkuType.INAPP)
        }
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
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases.isNotEmpty()) {
            purchases.forEach { handlePurchase(it) }
            loadPastPurchases()
        }
    }

    fun observe(owner: LifecycleOwner?) {
        owner ?: return
        destroy(owner, false)
        try {
            billingClientReadyData.tryToObserve(owner) { ready ->
                if (ready) {
                    loadPastPurchases()
                    billingProcessesListener?.onBillingClientReady()
                } else billingProcessesListener?.onBillingClientDisconnected()
            }
            inAppSkuDetailsData.tryToObserve(owner) {
                billingProcessesListener?.onInAppSkuDetailsListUpdated(it)
            }
            inAppPurchasesHistoryData.tryToObserve(owner) {
                billingProcessesListener?.onInAppPurchasesHistoryUpdated(it)
            }
            subscriptionsSkuDetailsData.tryToObserve(owner) {
                billingProcessesListener?.onSubscriptionsSkuDetailsListUpdated(it)
            }
            subscriptionsPurchasesHistoryData.tryToObserve(owner) {
                billingProcessesListener?.onSubscriptionsPurchasesHistoryUpdated(it)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun destroy(owner: LifecycleOwner?, shouldDestroyBillingClient: Boolean = true) {
        owner?.let {
            inAppSkuDetailsData.removeObservers(owner)
            inAppPurchasesHistoryData.removeObservers(owner)
            subscriptionsSkuDetailsData.removeObservers(owner)
            subscriptionsPurchasesHistoryData.removeObservers(owner)
            billingClientReadyData.removeObservers(owner)
        }
        if (shouldDestroyBillingClient) {
            billingClient?.endConnection()
            billingClient = null
            billingProcessesListener = null
        }
    }
}