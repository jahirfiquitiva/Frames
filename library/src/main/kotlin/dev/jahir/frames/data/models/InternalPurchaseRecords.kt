package dev.jahir.frames.data.models

import android.os.Parcelable
import com.android.billingclient.api.Purchase
import dev.jahir.frames.extensions.utils.purchaseStateToText
import kotlinx.parcelize.Parcelize
import org.json.JSONObject

@Parcelize
data class PseudoDetailedPurchaseRecord(
    val productId: String? = "",
    val purchaseTime: Long? = 0L,
    val acknowledged: Boolean? = false,
    val autoRenewing: Boolean? = false
) : Parcelable

@Suppress("unused", "MemberVisibilityCanBePrivate")
data class InternalDetailedPurchaseRecord(
    private val pseudoDetailedRecord: PseudoDetailedPurchaseRecord? = null,
    private val originalPurchaseRecord: Purchase? = null,
    val isAsync: Boolean = false
) {
    val sku: String?
        get() = originalPurchaseRecord?.sku ?: pseudoDetailedRecord?.productId

    val productId: String?
        get() = sku

    val developerPayload: String?
        get() = originalPurchaseRecord?.developerPayload

    val isAutoRenewing: Boolean?
        get() = originalPurchaseRecord?.isAutoRenewing ?: pseudoDetailedRecord?.autoRenewing

    val isAcknowledged: Boolean?
        get() = originalPurchaseRecord?.isAcknowledged ?: pseudoDetailedRecord?.acknowledged

    val orderId: String?
        get() = originalPurchaseRecord?.orderId

    val originalJson: String?
        get() = originalPurchaseRecord?.originalJson

    val packageName: String?
        get() = originalPurchaseRecord?.packageName

    @Purchase.PurchaseState
    val purchaseState: Int
        get() = originalPurchaseRecord?.purchaseState ?: Purchase.PurchaseState.UNSPECIFIED_STATE

    val purchaseTime: Long?
        get() = originalPurchaseRecord?.purchaseTime ?: pseudoDetailedRecord?.purchaseTime

    val purchaseToken: String?
        get() = originalPurchaseRecord?.purchaseToken

    val signature: String?
        get() = originalPurchaseRecord?.signature

    val toJson: JSONObject
        get() = JSONObject().apply {
            put("sku", sku)
            put("productId", productId)
            put("developerPayload", developerPayload)
            put("autoRenewing", isAutoRenewing)
            put("acknowledged", isAcknowledged)
            put("orderId", orderId)
            put("packageName", packageName)
            put("purchaseState", purchaseState)
            put("purchaseStateText", purchaseStateToText(purchaseState))
            put("purchaseTime", purchaseTime)
            put("purchaseToken", purchaseToken)
            put("signature", signature)
            put("originalJson", originalJson)
            put("isAsync", isAsync)
        }

    override fun toString(): String {
        return "DetailedPurchaseRecord => ( isAsync: $isAsync ; sku: $sku ; " +
                "developerPayload: $developerPayload ; isAutoRenewing: $isAutoRenewing ; " +
                "isAcknowledged: $isAcknowledged ; orderId: $orderId ; packageName: $packageName ; " +
                "purchaseState: ${purchaseStateToText(purchaseState)} ; purchaseTime: $purchaseTime ; " +
                "purchaseToken: $purchaseToken ; signature: $signature )"
    }

    fun toJSONString(indentSpaces: Int = 2): String = toJson.toString(indentSpaces)
}
