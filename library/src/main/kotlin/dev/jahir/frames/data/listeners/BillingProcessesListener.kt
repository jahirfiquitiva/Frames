package dev.jahir.frames.data.listeners

import com.android.billingclient.api.ProductDetails
import dev.jahir.frames.data.models.DetailedPurchaseRecord

interface BillingProcessesListener {
    fun onBillingClientReady() {}
    fun onBillingClientDisconnected() {}
    fun onInAppProductDetailsListUpdated(productDetailsList: List<ProductDetails>) {}
    fun onSubscriptionsProductDetailsListUpdated(productDetailsList: List<ProductDetails>) {}
    fun onInAppPurchasesHistoryUpdated(inAppPurchasesHistory: List<DetailedPurchaseRecord>) {}
    fun onSubscriptionsPurchasesHistoryUpdated(subscriptionsPurchasesHistory: List<DetailedPurchaseRecord>) {}
    fun onProductPurchaseSuccess(purchase: DetailedPurchaseRecord? = null)
    fun onProductPurchaseError(purchase: DetailedPurchaseRecord? = null)

    @Deprecated(
        "Use 'onInAppProductDetailsListUpdated' instead",
        ReplaceWith("onInAppProductDetailsListUpdated"),
        DeprecationLevel.ERROR
    )
    fun onInAppSkuDetailsListUpdated(productDetailsList: List<ProductDetails>) {
        onInAppProductDetailsListUpdated(productDetailsList)
    }

    @Deprecated(
        "Use 'onSubscriptionsProductDetailsListUpdated' instead",
        ReplaceWith("onSubscriptionsProductDetailsListUpdated"),
        DeprecationLevel.ERROR
    )
    fun onSubscriptionsSkuDetailsListUpdated(productDetailsList: List<ProductDetails>) {
        onSubscriptionsProductDetailsListUpdated(productDetailsList)
    }

    @Deprecated(
        "Use 'onProductPurchaseSuccess' instead",
        ReplaceWith("onProductPurchaseSuccess"),
        DeprecationLevel.ERROR
    )
    fun onSkuPurchaseSuccess(purchase: DetailedPurchaseRecord? = null) {
        onProductPurchaseSuccess(purchase)
    }

    @Deprecated(
        "Use 'onProductPurchaseError' instead",
        ReplaceWith("onProductPurchaseError"),
        DeprecationLevel.ERROR
    )
    fun onSkuPurchaseError(purchase: DetailedPurchaseRecord? = null) {
        onProductPurchaseError(purchase)
    }
}
