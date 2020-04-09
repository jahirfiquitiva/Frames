package dev.jahir.frames.data.models

import android.os.Parcelable
import com.android.billingclient.api.Purchase
import kotlinx.android.parcel.Parcelize

@Parcelize
data class PseudoDetailedPurchaseRecord(
    val productId: String? = "",
    val purchaseTime: Long? = 0L,
    val acknowledged: Boolean? = false,
    val autoRenewing: Boolean? = false
) : Parcelable

@Suppress("unused", "MemberVisibilityCanBePrivate")
data class DetailedPurchaseRecord(
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

    val isAutoRenewing: Boolean
        get() =
            (originalPurchaseRecord?.isAutoRenewing ?: pseudoDetailedRecord?.autoRenewing) == true

    val isAcknowledged: Boolean
        get() =
            (originalPurchaseRecord?.isAcknowledged ?: pseudoDetailedRecord?.acknowledged) == true

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
}