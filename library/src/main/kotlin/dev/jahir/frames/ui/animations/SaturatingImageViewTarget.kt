/*
 * Copyright 2019 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.jahir.frames.ui.animations

import android.graphics.drawable.Animatable
import android.graphics.drawable.Drawable
import android.widget.ImageView
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import coil.decode.DataSource
import coil.request.ImageRequest
import coil.request.ImageResult
import coil.target.PoolableViewTarget
import coil.target.Target
import com.ortiz.touchview.TouchImageView
import dev.jahir.frames.extensions.context.preferences

/**
 * A [Target], which handles setting images on an [ImageView].
 */
open class SaturatingImageViewTarget(
    override val view: ImageView,
    var shouldActuallySaturate: Boolean = true
) : PoolableViewTarget<ImageView>, DefaultLifecycleObserver, ImageRequest.Listener {

    private val afterSuccessListeners: ArrayList<((drawable: Drawable?) -> Unit)> = ArrayList()
    private var isStarted = false

    override fun onStart(placeholder: Drawable?) = setDrawable(placeholder)

    override fun onSuccess(result: Drawable) = setDrawable(result)

    override fun onSuccess(request: ImageRequest, metadata: ImageResult.Metadata) {
        val source = metadata.dataSource
        // This is called after onSuccess(Drawable) above, so we can assume the image has
        // already been set
        if ((source == DataSource.DISK || source == DataSource.NETWORK) &&
            view.drawable != null && shouldActuallySaturate && view.context.preferences.animationsEnabled
        ) {
            saturateDrawableAnimator(view.drawable, view = view).start()
        }
        afterSuccessListeners.forEach { it.invoke(view.drawable) }
        afterSuccessListeners.clear()
    }

    override fun onError(error: Drawable?) = setDrawable(error)

    override fun onClear() = setDrawable(null)

    override fun onStart(owner: LifecycleOwner) {
        isStarted = true
        updateAnimation()
    }

    override fun onStop(owner: LifecycleOwner) {
        isStarted = false
        updateAnimation()
    }

    private fun setDrawable(drawable: Drawable?) {
        (view.drawable as? Animatable)?.stop()
        // TODO: Enable
        (view as? TouchImageView)?.setImageDrawable(drawable) // , false)
            ?: view.setImageDrawable(drawable)
        updateAnimation()
    }

    private fun updateAnimation() {
        if (!view.context.preferences.animationsEnabled) return
        val animatable = view.drawable as? Animatable ?: return
        if (isStarted) animatable.start() else animatable.stop()
    }

    fun addListener(listener: (drawable: Drawable?) -> Unit): SaturatingImageViewTarget =
        this.apply { this.afterSuccessListeners.add(listener) }
}
