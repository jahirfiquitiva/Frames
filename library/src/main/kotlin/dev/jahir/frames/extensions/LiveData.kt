package dev.jahir.frames.extensions

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner

inline fun <reified T> lazyMutableLiveData(): Lazy<MutableLiveData<T>> {
    return lazy { MutableLiveData<T>() }
}

inline fun <reified T : ViewModel> ViewModelStoreOwner.lazyViewModel(): Lazy<T?> {
    return lazy {
        try {
            ViewModelProvider(this).get(T::class.java)
        } catch (e: Exception) {
            null
        }
    }
}