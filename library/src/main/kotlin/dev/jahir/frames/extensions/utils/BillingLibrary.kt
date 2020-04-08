package dev.jahir.frames.extensions.utils

import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchaseHistoryRecord
import com.google.gson.Gson
import dev.jahir.frames.data.models.DetailedPurchaseRecord

fun Purchase.asDetailedPurchase(): DetailedPurchaseRecord? = try {
    Gson().fromJson(originalJson, DetailedPurchaseRecord::class.java).apply {
        originalPurchaseRecord = this@asDetailedPurchase
    }
} catch (e: Exception) {
    null
}

fun PurchaseHistoryRecord.asDetailedPurchase(): DetailedPurchaseRecord? = try {
    Gson().fromJson(originalJson, DetailedPurchaseRecord::class.java).apply {
        originalPurchaseRecord = Purchase(originalJson, signature)
    }
} catch (e: Exception) {
    null
}