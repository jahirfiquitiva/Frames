package dev.jahir.frames.data.network

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import java.lang.ref.WeakReference

abstract class ContextAwareWorker(context: Context, parameters: WorkerParameters) :
    Worker(context, parameters) {
    private var weakContext: WeakReference<Context?>? = null
    val context: Context?
        get() = weakContext?.get()

    init {
        weakContext = WeakReference(context)
    }
}