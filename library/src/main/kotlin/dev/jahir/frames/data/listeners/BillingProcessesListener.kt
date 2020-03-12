package dev.jahir.frames.data.listeners

import com.android.billingclient.api.SkuDetails
import dev.jahir.frames.data.models.DetailedPurchaseRecord

interface BillingProcessesListener {
    fun onBillingClientReady() {}
    fun onInAppSkuDetailsListUpdated(skuDetailsList: List<SkuDetails>) {}
    fun onSubscriptionsSkuDetailsListUpdated(skuDetailsList: List<SkuDetails>) {}
    fun onInAppPurchasesHistoryUpdated(skuDetailsList: List<DetailedPurchaseRecord>) {}
    fun onSubscriptionsPurchasesHistoryUpdated(skuDetailsList: List<DetailedPurchaseRecord>) {}
    fun onSkuPurchaseSuccess(purchase: DetailedPurchaseRecord? = null)
    fun onSkuPurchaseError(purchase: DetailedPurchaseRecord? = null)
}