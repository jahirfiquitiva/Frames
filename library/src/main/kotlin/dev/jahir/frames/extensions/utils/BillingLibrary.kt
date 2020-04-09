package dev.jahir.frames.extensions.utils

import android.util.Log
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchaseHistoryRecord
import com.google.gson.Gson
import dev.jahir.frames.data.models.DetailedPurchaseRecord
import dev.jahir.frames.data.models.InternalDetailedPurchaseRecord
import dev.jahir.frames.data.models.PseudoDetailedPurchaseRecord

fun Purchase.asDetailedPurchase(): DetailedPurchaseRecord? =
    try {
        val pseudoDetailedRecord =
            Gson().fromJson(originalJson, PseudoDetailedPurchaseRecord::class.java)
        val internalDetailedRecord = InternalDetailedPurchaseRecord(pseudoDetailedRecord, this)
        Gson().fromJson(internalDetailedRecord.toJSONString(0), DetailedPurchaseRecord::class.java)
    } catch (e: Exception) {
        Log.e("Frames", e.message, e)
        null
    }

fun PurchaseHistoryRecord.asDetailedPurchase(): DetailedPurchaseRecord? =
    try {
        val purchase = Purchase(originalJson, signature)
        val pseudoDetailedRecord =
            Gson().fromJson(originalJson, PseudoDetailedPurchaseRecord::class.java)
        val internalDetailedRecord =
            InternalDetailedPurchaseRecord(pseudoDetailedRecord, purchase, true)
        Gson().fromJson(internalDetailedRecord.toJSONString(0), DetailedPurchaseRecord::class.java)
    } catch (e: Exception) {
        Log.e("Frames", e.message, e)
        null
    }

internal fun purchaseStateToText(@Purchase.PurchaseState purchaseState: Int): String =
    when (purchaseState) {
        Purchase.PurchaseState.PENDING -> "Pending"
        Purchase.PurchaseState.PURCHASED -> "Purchased"
        else -> "Unspecified"
    }