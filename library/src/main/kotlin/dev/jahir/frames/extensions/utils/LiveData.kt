@file:Suppress("RemoveExplicitTypeArguments", "unused")

package dev.jahir.frames.extensions.utils

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.work.WorkManager
import java.util.*

inline fun <reified T> lazyMutableLiveData(): Lazy<MutableLiveData<T>> {
    return lazy { MutableLiveData<T>() }
}

inline fun <reified T : ViewModel> FragmentActivity.lazyViewModel(): Lazy<T> {
    return lazy { getViewModel<T>() }
}

inline fun <reified T : ViewModel> Fragment.lazyViewModel(): Lazy<T> {
    return lazy { getViewModel<T>() }
}

inline fun <reified T : ViewModel> ViewModelStoreOwner.getViewModel(clazz: Class<T>): T =
    ViewModelProvider(this).get(clazz)

inline fun <reified T : ViewModel> ViewModelStoreOwner.getViewModel(): T =
    getViewModel<T>(T::class.java)

inline fun <T> LiveData<T>.tryToObserve(
    owner: LifecycleOwner,
    crossinline onChanged: (t: T) -> Unit
) {
    observe(owner, Observer<T> { t ->
        try {
            onChanged.invoke(t)
        } catch (e: Exception) {
        }
    })
}

fun WorkManager.getWorkInfoValue(uuid: UUID) =
    getWorkInfoByIdLiveData(uuid).value