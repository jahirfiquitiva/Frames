package dev.jahir.frames.data.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class DetailedPurchaseRecord(
    val productId: String,
    val purchaseTime: Long,
    val acknowledged: Boolean? = false,
    val autoRenewing: Boolean? = false
) : Parcelable