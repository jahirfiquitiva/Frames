package dev.jahir.frames.data.models

import com.android.billingclient.api.SkuDetails

@Suppress("unused")
data class CleanSkuDetails(val originalDetails: SkuDetails) {
    override fun toString(): String = "${originalDetails.title} - ${originalDetails.price}"
}