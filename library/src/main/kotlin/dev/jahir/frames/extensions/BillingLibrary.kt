package dev.jahir.frames.extensions

import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchaseHistoryRecord
import com.android.billingclient.api.SkuDetails
import com.google.gson.Gson
import dev.jahir.frames.data.models.CleanSkuDetails
import dev.jahir.frames.data.models.DetailedPurchaseRecord

fun SkuDetails.asCleanSkuDetails(): CleanSkuDetails? = try {
    CleanSkuDetails(sku, title, description, price, type)
} catch (e: Exception) {
    null
}

fun Purchase.asDetailedPurchase(): DetailedPurchaseRecord? = try {
    Gson().fromJson(originalJson, DetailedPurchaseRecord::class.java)
} catch (e: Exception) {
    null
}

fun PurchaseHistoryRecord.asDetailedPurchase(): DetailedPurchaseRecord? = try {
    Gson().fromJson(originalJson, DetailedPurchaseRecord::class.java)
} catch (e: Exception) {
    null
}