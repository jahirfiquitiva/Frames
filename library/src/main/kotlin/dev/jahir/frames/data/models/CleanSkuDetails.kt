package dev.jahir.frames.data.models

import com.android.billingclient.api.SkuDetails

@Suppress("unused", "MemberVisibilityCanBePrivate")
data class CleanSkuDetails(val originalDetails: SkuDetails) {
    val cleanTitle: String
        get() = originalDetails.title.substring(0, originalDetails.title.lastIndexOf("(")).trim()

    override fun toString(): String = "$cleanTitle - ${originalDetails.price}"
}