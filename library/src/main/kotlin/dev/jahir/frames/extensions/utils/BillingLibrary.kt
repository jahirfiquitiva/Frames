package dev.jahir.frames.extensions.utils

import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchaseHistoryRecord
import dev.jahir.frames.data.models.DetailedPurchaseRecord
import dev.jahir.frames.data.models.InternalDetailedPurchaseRecord
import dev.jahir.frames.data.models.PseudoDetailedPurchaseRecord
import org.json.JSONObject

fun Purchase.asDetailedPurchase(): DetailedPurchaseRecord? =
    try {
        InternalDetailedPurchaseRecord(pseudoDetailedPurchase(originalJson), this)
            .toDetailedPurchaseRecord()
    } catch (e: Exception) {
        null
    }

fun PurchaseHistoryRecord.asDetailedPurchase(): DetailedPurchaseRecord? =
    try {
        val purchase = Purchase(originalJson, signature)
        InternalDetailedPurchaseRecord(pseudoDetailedPurchase(originalJson), purchase, true)
            .toDetailedPurchaseRecord()
    } catch (e: Exception) {
        null
    }

// Missing keys keep the defaults, as they did when Gson created this class
private fun pseudoDetailedPurchase(json: String): PseudoDetailedPurchaseRecord {
    val purchase = JSONObject(json)
    return PseudoDetailedPurchaseRecord(
        productId = purchase.optString("productId", ""),
        purchaseTime = purchase.optLong("purchaseTime", 0L),
        acknowledged = purchase.optBoolean("acknowledged", false),
        autoRenewing = purchase.optBoolean("autoRenewing", false)
    )
}

private fun InternalDetailedPurchaseRecord.toDetailedPurchaseRecord() = DetailedPurchaseRecord(
    sku = sku,
    productId = productId,
    developerPayload = developerPayload,
    autoRenewing = isAutoRenewing,
    acknowledged = isAcknowledged,
    orderId = orderId,
    packageName = packageName,
    purchaseState = purchaseState,
    purchaseStateText = purchaseStateToText(purchaseState),
    purchaseTime = purchaseTime,
    purchaseToken = purchaseToken,
    signature = signature,
    originalJson = originalJson,
    isAsync = isAsync
)

internal fun purchaseStateToText(@Purchase.PurchaseState purchaseState: Int): String =
    when (purchaseState) {
        Purchase.PurchaseState.PENDING -> "Pending"
        Purchase.PurchaseState.PURCHASED -> "Purchased"
        else -> "Unspecified"
    }

val ProductDetails.price: String
    get() = if (productType == BillingClient.ProductType.INAPP)
        oneTimePurchaseOfferDetails?.formattedPrice ?: "0"
    else subscriptionOfferDetails?.firstOrNull()?.pricingPhases?.pricingPhaseList?.firstOrNull()?.formattedPrice
        ?: "0"

val ProductDetails.priceAmountMicros: Long
    get() = if (productType == BillingClient.ProductType.INAPP)
        oneTimePurchaseOfferDetails?.priceAmountMicros ?: 0L
    else subscriptionOfferDetails?.firstOrNull()?.pricingPhases?.pricingPhaseList?.firstOrNull()?.priceAmountMicros
        ?: 0L
