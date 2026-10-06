package dev.jahir.frames.data.models

import com.android.billingclient.api.BillingClient.BillingResponseCode
import com.android.billingclient.api.BillingClient.OnPurchasesUpdatedSubResponseCode
import com.android.billingclient.api.BillingResult

sealed interface BillingError {
    data object Unavailable : BillingError
    data object Network : BillingError
    data object ItemUnavailable : BillingError
    data object AlreadyOwned : BillingError
    data object PaymentDeclined : BillingError
    data object NotEligible : BillingError
    data class Unknown(@BillingResponseCode val responseCode: Int) : BillingError

    companion object {
        fun from(billingResult: BillingResult): BillingError {
            when (billingResult.onPurchasesUpdatedSubResponseCode) {
                OnPurchasesUpdatedSubResponseCode.PAYMENT_DECLINED_DUE_TO_INSUFFICIENT_FUNDS ->
                    return PaymentDeclined

                OnPurchasesUpdatedSubResponseCode.USER_INELIGIBLE -> return NotEligible
            }
            return when (billingResult.responseCode) {
                BillingResponseCode.BILLING_UNAVAILABLE,
                BillingResponseCode.SERVICE_UNAVAILABLE,
                BillingResponseCode.SERVICE_DISCONNECTED,
                BillingResponseCode.FEATURE_NOT_SUPPORTED -> Unavailable

                BillingResponseCode.NETWORK_ERROR -> Network
                BillingResponseCode.ITEM_UNAVAILABLE -> ItemUnavailable
                BillingResponseCode.ITEM_ALREADY_OWNED -> AlreadyOwned
                else -> Unknown(billingResult.responseCode)
            }
        }
    }
}
