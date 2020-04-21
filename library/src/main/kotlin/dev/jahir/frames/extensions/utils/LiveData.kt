@file:Suppress("RemoveExplicitTypeArguments", "unused")

package dev.jahir.frames.extensions.utils

import android.content.Context
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.annotation.MainThread
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.work.WorkManager
import java.util.UUID

inline fun <reified T> lazyMutableLiveData(): Lazy<MutableLiveData<T>> {
    return lazy { MutableLiveData<T>() }
}

@MainThread
inline fun <reified VM : ViewModel> ComponentActivity.lazyViewModel(
    noinline factoryProducer: (() -> ViewModelProvider.Factory)? = null
): Lazy<VM> = viewModels(factoryProducer)

@MainThread
inline fun <reified VM : ViewModel> Fragment.lazyViewModel(
    noinline ownerProducer: () -> ViewModelStoreOwner = { this },
    noinline factoryProducer: (() -> ViewModelProvider.Factory)? = null
): Lazy<VM> = viewModels(ownerProducer, factoryProducer)

@MainThread
inline fun <reified VM : ViewModel> Fragment.lazyActivityViewModels(
    noinline factoryProducer: (() -> ViewModelProvider.Factory)? = null
): Lazy<VM> = activityViewModels(factoryProducer)

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

inline val AndroidViewModel.context: Context
    get() = getApplication()