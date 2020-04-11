package dev.jahir.frames.extensions.utils

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

inline fun <reified T> lazyMutableLiveData(): Lazy<MutableLiveData<T>> {
    return lazy { MutableLiveData<T>() }
}

inline fun <reified T : ViewModel> FragmentActivity.lazyViewModel(): Lazy<T> {
    return lazy { ViewModelProvider(this).get(T::class.java) }
}

inline fun <reified T : ViewModel> Fragment.lazyViewModel(): Lazy<T> {
    return lazy { ViewModelProvider(this).get(T::class.java) }
}