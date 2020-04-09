package dev.jahir.frames.extensions.utils

import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchaseHistoryRecord
import com.google.gson.Gson
import dev.jahir.frames.data.models.DetailedPurchaseRecord
import dev.jahir.frames.data.models.PseudoDetailedPurchaseRecord

fun Purchase.asDetailedPurchase(): DetailedPurchaseRecord? =
    try {
        val pseudoDetailedRecord =
            Gson().fromJson(originalJson, PseudoDetailedPurchaseRecord::class.java)
        DetailedPurchaseRecord(pseudoDetailedRecord, this)
    } catch (e: Exception) {
        null
    }

fun PurchaseHistoryRecord.asDetailedPurchase(): DetailedPurchaseRecord? =
    try {
        val pseudoDetailedRecord =
            Gson().fromJson(originalJson, PseudoDetailedPurchaseRecord::class.java)
        DetailedPurchaseRecord(pseudoDetailedRecord, Purchase(originalJson, signature), true)
    } catch (e: Exception) {
        null
    }