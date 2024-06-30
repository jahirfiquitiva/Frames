package dev.jahir.frames.data.viewmodels

import android.app.Application
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ConsumeParams
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchaseHistoryParams
import com.android.billingclient.api.QueryPurchasesParams
import com.android.billingclient.api.consumePurchase
import com.android.billingclient.api.queryPurchasesAsync
import dev.jahir.frames.data.listeners.BillingProcessesListener
import dev.jahir.frames.data.models.DetailedPurchaseRecord
import dev.jahir.frames.extensions.utils.asDetailedPurchase
import dev.jahir.frames.extensions.utils.context
import dev.jahir.frames.extensions.utils.lazyMutableLiveData
import dev.jahir.frames.extensions.utils.priceAmountMicros
import dev.jahir.frames.extensions.utils.tryToObserve
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Suppress("MemberVisibilityCanBePrivate", "unused")
class BillingViewModel(application: Application) : AndroidViewModel(application),
    BillingClientStateListener, PurchasesUpdatedListener {

    var billingProcessesListener: BillingProcessesListener? = null

    private var billingClient: BillingClient? = null

    private val subscriptionsPurchasesHistoryData: MutableLiveData<List<DetailedPurchaseRecord>> by lazyMutableLiveData()
    val subscriptionsPurchasesHistory: List<DetailedPurchaseRecord>
        get() = subscriptionsPurchasesHistoryData.value.orEmpty()

    private val inAppPurchasesHistoryData: MutableLiveData<List<DetailedPurchaseRecord>> by lazyMutableLiveData()
    val inAppPurchasesHistory: List<DetailedPurchaseRecord>
        get() = inAppPurchasesHistoryData.value.orEmpty()

    private val inAppProductDetailsData: MutableLiveData<List<ProductDetails>> by lazyMutableLiveData()
    val inAppProductDetails: List<ProductDetails>
        get() = inAppProductDetailsData.value.orEmpty()

    private val subscriptionsProductDetailsData: MutableLiveData<List<ProductDetails>> by lazyMutableLiveData()
    val subscriptionsProductDetails: List<ProductDetails>
        get() = subscriptionsProductDetailsData.value.orEmpty()

    private val billingClientReadyData: MutableLiveData<Boolean> by lazyMutableLiveData()
    val isBillingClientReady: Boolean
        get() = billingClientReadyData.value == true && billingClient?.isReady == true

    fun initialize() {
        billingClient = BillingClient
            .newBuilder(context)
            .setListener(this)
            .enablePendingPurchases(
                PendingPurchasesParams.newBuilder()
                    .enableOneTimeProducts().build()
            )
            .build()
        billingClient?.startConnection(this)
    }

    private fun buildQueryProductDetailsParams(
        productItemsIds: List<String>,
        @BillingClient.ProductType productType: String
    ): QueryProductDetailsParams =
        QueryProductDetailsParams.newBuilder()
            .setProductList(productItemsIds.map { productId ->
                QueryProductDetailsParams.Product.newBuilder()
                    .setProductId(productId)
                    .setProductType(productType)
                    .build()
            })
            .build()


    private suspend fun internalQueryProductDetailsList(
        productItemsIds: List<String>,
        @BillingClient.ProductType productType: String
    ) {
        if (!isBillingClientReady || productItemsIds.isEmpty()) return
        withContext(IO) {
            billingClient?.queryProductDetailsAsync(
                buildQueryProductDetailsParams(productItemsIds, productType)
            ) { _, detailsList ->
                val details = detailsList.sortedBy { it.priceAmountMicros }
                when (productType) {
                    BillingClient.ProductType.INAPP -> {
                        inAppProductDetailsData.postValue(details)
                    }

                    BillingClient.ProductType.SUBS -> {
                        subscriptionsProductDetailsData.postValue(details)
                    }
                }
            }
        }
    }

    fun queryInAppProductDetailsList(productItemsIds: List<String>) {
        viewModelScope.launch {
            internalQueryProductDetailsList(productItemsIds, BillingClient.ProductType.INAPP)
        }
    }

    fun querySubscriptionsProductDetailsList(productItemsIds: List<String>) {
        viewModelScope.launch {
            internalQueryProductDetailsList(productItemsIds, BillingClient.ProductType.SUBS)
        }
    }

    fun launchBillingFlow(activity: FragmentActivity?, productDetails: ProductDetails?) {
        activity ?: return
        productDetails ?: return
        billingClient?.launchBillingFlow(
            activity,
            BillingFlowParams.newBuilder().setProductDetailsParamsList(
                listOf(
                    BillingFlowParams.ProductDetailsParams.newBuilder()
                        .setProductDetails(productDetails)
                        .build()
                )
            ).build()
        )
    }

    private fun postPurchasesHistory(
        @BillingClient.ProductType productType: String,
        newPurchases: List<DetailedPurchaseRecord>
    ) {
        val actualPurchases = ArrayList(
            when (productType) {
                BillingClient.ProductType.INAPP -> inAppPurchasesHistory
                BillingClient.ProductType.SUBS -> subscriptionsPurchasesHistory
                else -> listOf()
            }
        )
        actualPurchases.addAll(newPurchases)
        when (productType) {
            BillingClient.ProductType.INAPP -> {
                inAppPurchasesHistoryData.postValue(
                    actualPurchases.sortedByDescending { it.purchaseTime }
                )
            }

            BillingClient.ProductType.SUBS -> {
                subscriptionsPurchasesHistoryData.postValue(
                    actualPurchases.sortedByDescending { it.purchaseTime }
                )
            }
        }
    }

    private suspend fun queryPurchases(@BillingClient.ProductType productType: String) {
        if (!isBillingClientReady) return
        val params = QueryPurchasesParams.newBuilder().setProductType(productType).build()
        withContext(IO) {
            billingClient?.queryPurchasesAsync(params) { billingResult, purchasesList ->
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    postPurchasesHistory(productType,
                        purchasesList.mapNotNull { purchase -> purchase.asDetailedPurchase() })
                }
            }
        }
    }

    private suspend fun queryPurchasesHistory(@BillingClient.ProductType productType: String) {
        if (!isBillingClientReady) return
        val params = QueryPurchasesParams.newBuilder().setProductType(productType).build()
        withContext(IO) {
            billingClient?.queryPurchasesAsync(params) { billingResult, purchaseHistoryRecordList ->
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    postPurchasesHistory(productType,
                        purchaseHistoryRecordList.orEmpty()
                            .mapNotNull { purchase -> purchase.asDetailedPurchase() })
                }
            }
        }
    }

    fun loadPastPurchases() {
        if (!isBillingClientReady) return
        viewModelScope.launch {
            queryPurchasesHistory(BillingClient.ProductType.SUBS)
            queryPurchases(BillingClient.ProductType.SUBS)
            queryPurchasesHistory(BillingClient.ProductType.INAPP)
            queryPurchases(BillingClient.ProductType.INAPP)
        }
    }

    private suspend fun handlePurchase(purchase: Purchase) {
        if (!isBillingClientReady) return
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            // Consumable Purchases (Can be consumed and purchased multiple times)
            try {
                val consumeParams =
                    ConsumeParams.newBuilder()
                        .setPurchaseToken(purchase.purchaseToken)
                        .build()
                val consumeResult = withContext(IO) {
                    billingClient?.consumePurchase(consumeParams)
                }
                consumeResult?.billingResult?.let {
                    if (it.responseCode == BillingClient.BillingResponseCode.OK) {
                        billingProcessesListener?.onProductPurchaseSuccess(purchase.asDetailedPurchase())
                    } else {
                        billingProcessesListener?.onProductPurchaseError(purchase.asDetailedPurchase())
                    }
                } ?: billingProcessesListener?.onProductPurchaseError(purchase.asDetailedPurchase())
            } catch (e: Exception) {
                billingProcessesListener?.onProductPurchaseError(purchase.asDetailedPurchase())
            }

            /* Non-Consumable Purchases (One-time only purchases)
            if (!purchase.isAcknowledged) {
                val acknowledgePurchaseParams =
                    AcknowledgePurchaseParams.newBuilder()
                        .setPurchaseToken(purchase.purchaseToken)
                        .build()
                try {
                    billingClient?.acknowledgePurchase(acknowledgePurchaseParams) {
                        if (it.responseCode == BillingClient.BillingResponseCode.OK) {
                            billingProcessesListener?.onProductPurchaseSuccess(purchase.asDetailedPurchase())
                        } else {
                            billingProcessesListener?.onProductPurchaseError(purchase.asDetailedPurchase())
                        }
                    } ?: {
                        billingProcessesListener?.onProductPurchaseError(purchase.asDetailedPurchase())
                    }()
                } catch (e: Exception) {
                    billingProcessesListener?.onProductPurchaseError(purchase.asDetailedPurchase())
                }
            }
            */
        }
    }

    override fun onBillingSetupFinished(billingResult: BillingResult) {
        billingClientReadyData.postValue(billingResult.responseCode == BillingClient.BillingResponseCode.OK)
    }

    override fun onBillingServiceDisconnected() {
        billingClientReadyData.postValue(false)
        inAppProductDetailsData.postValue(null)
        inAppPurchasesHistoryData.postValue(null)
        subscriptionsProductDetailsData.postValue(null)
        subscriptionsPurchasesHistoryData.postValue(null)
    }

    override fun onPurchasesUpdated(
        billingResult: BillingResult,
        purchases: MutableList<Purchase>?
    ) {
        purchases ?: return
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases.isNotEmpty()) {
            viewModelScope.launch {
                purchases.forEach {
                    handlePurchase(it)
                }
            }
            loadPastPurchases()
        }
    }

    fun observe(owner: LifecycleOwner?) {
        owner ?: return
        destroy(owner, false)
        try {
            billingClientReadyData.tryToObserve(owner) { ready ->
                if (ready) {
                    loadPastPurchases()
                    billingProcessesListener?.onBillingClientReady()
                } else billingProcessesListener?.onBillingClientDisconnected()
            }
            inAppProductDetailsData.tryToObserve(owner) {
                billingProcessesListener?.onInAppProductDetailsListUpdated(it)
            }
            inAppPurchasesHistoryData.tryToObserve(owner) {
                billingProcessesListener?.onInAppPurchasesHistoryUpdated(it)
            }
            subscriptionsProductDetailsData.tryToObserve(owner) {
                billingProcessesListener?.onSubscriptionsProductDetailsListUpdated(it)
            }
            subscriptionsPurchasesHistoryData.tryToObserve(owner) {
                billingProcessesListener?.onSubscriptionsPurchasesHistoryUpdated(it)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun destroy(owner: LifecycleOwner?, shouldDestroyBillingClient: Boolean = true) {
        owner?.let {
            inAppProductDetailsData.removeObservers(owner)
            inAppPurchasesHistoryData.removeObservers(owner)
            subscriptionsProductDetailsData.removeObservers(owner)
            subscriptionsPurchasesHistoryData.removeObservers(owner)
            billingClientReadyData.removeObservers(owner)
        }
        if (shouldDestroyBillingClient) {
            billingClient?.endConnection()
            billingClient = null
            billingProcessesListener = null
        }
    }
}
