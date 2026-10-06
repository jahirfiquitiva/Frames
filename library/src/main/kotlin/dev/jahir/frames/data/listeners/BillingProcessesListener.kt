package dev.jahir.frames.data.listeners

import com.android.billingclient.api.ProductDetails
import dev.jahir.frames.data.models.BillingError
import dev.jahir.frames.data.models.DetailedPurchaseRecord

interface BillingProcessesListener {
    fun onBillingClientReady() {}
    fun onBillingClientDisconnected() {}
    fun onInAppProductDetailsListUpdated(productDetailsList: List<ProductDetails>) {}
    fun onSubscriptionsProductDetailsListUpdated(productDetailsList: List<ProductDetails>) {}
    fun onInAppPurchasesHistoryUpdated(inAppPurchasesHistory: List<DetailedPurchaseRecord>) {}
    fun onSubscriptionsPurchasesHistoryUpdated(subscriptionsPurchasesHistory: List<DetailedPurchaseRecord>) {}
    fun onProductPurchaseSuccess(purchase: DetailedPurchaseRecord? = null)
    fun onProductPurchaseError(error: BillingError, purchase: DetailedPurchaseRecord? = null)
}
