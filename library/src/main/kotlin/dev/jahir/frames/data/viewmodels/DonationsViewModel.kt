package dev.jahir.frames.data.viewmodels

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anjlab.android.iab.v3.BillingProcessor
import com.anjlab.android.iab.v3.SkuDetails
import dev.jahir.frames.data.models.DonationItem
import dev.jahir.frames.extensions.hasContent
import dev.jahir.frames.extensions.lazyMutableLiveData
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DonationsViewModel : ViewModel() {

    private val data: MutableLiveData<List<DonationItem>>? by lazyMutableLiveData()

    private suspend fun getDonationItemDetails(
        billingProcessor: BillingProcessor,
        donationItemId: String
    ): SkuDetails? {
        if (!donationItemId.hasContent()) return null
        return withContext(Default) {
            try {
                billingProcessor.getPurchaseListingDetails(donationItemId)
            } catch (e: Exception) {
                null
            }
        }
    }

    fun loadItems(
        billingProcessor: BillingProcessor? = null,
        donationItemsIds: Array<String> = arrayOf()
    ) {
        billingProcessor?.let { bp ->
            if (donationItemsIds.isEmpty() || !bp.isInitialized) {
                data?.value = null
                data?.postValue(arrayListOf())
            } else {
                viewModelScope.launch {
                    val newItems = ArrayList<DonationItem>()
                    donationItemsIds.forEach { itemId ->
                        val donationItem = getDonationItemDetails(bp, itemId)
                        donationItem?.let {
                            val max = it.title.indexOf("(")
                            val name =
                                it.title.substring(0, if (max > 0) max else it.title.length).trim()
                            newItems.add(DonationItem(itemId, name, it.priceText.trim()))
                        }
                    }
                    data?.value = null
                    data?.postValue(newItems)
                }
            }
        } ?: {
            data?.value = null
            data?.postValue(arrayListOf())
        }()
    }

    fun observe(owner: LifecycleOwner, onUpdated: (List<DonationItem>) -> Unit) {
        data?.observe(owner, Observer<List<DonationItem>> { r -> r?.let { onUpdated(it) } })
    }

    fun destroy(owner: LifecycleOwner) {
        data?.removeObservers(owner)
    }
}