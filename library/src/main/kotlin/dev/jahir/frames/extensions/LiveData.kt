package dev.jahir.frames.extensions

import androidx.lifecycle.MutableLiveData

inline fun <reified T> lazyMutableLiveData(): Lazy<MutableLiveData<T>?> {
    return lazy { MutableLiveData<T>() }
}
