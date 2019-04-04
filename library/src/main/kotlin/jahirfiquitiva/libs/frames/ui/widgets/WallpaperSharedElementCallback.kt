/*
 * Copyright (c) 2017. André Mion
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

package jahirfiquitiva.libs.frames.ui.widgets

import android.view.View
import androidx.core.app.SharedElementCallback
import androidx.core.view.ViewCompat
import java.util.ArrayList

/**
 * Some hacks pulled from https://github.com/googlesamples/android-unsplash
 */
class WallpaperSharedElementCallback : SharedElementCallback() {
    
    private val mSharedElementViews: MutableList<View?>
    
    init {
        mSharedElementViews = ArrayList()
    }
    
    fun setSharedElementViews(vararg sharedElementViews: View?) {
        mSharedElementViews.clear()
        mSharedElementViews.addAll(sharedElementViews)
    }
    
    override fun onMapSharedElements(
        names: MutableList<String>,
        sharedElements: MutableMap<String, View?>
                                    ) {
        if (!mSharedElementViews.isEmpty()) {
            removeObsoleteElements(names, sharedElements, mapObsoleteElements(names))
            for (sharedElementView in mSharedElementViews) {
                val transitionName =
                    sharedElementView?.let { ViewCompat.getTransitionName(it) ?: "" } ?: ""
                names.add(transitionName)
                sharedElements[transitionName] = sharedElementView
            }
        }
    }
    
    override fun onSharedElementEnd(
        sharedElementNames: List<String>,
        sharedElements: List<View>,
        sharedElementSnapshots: List<View>
                                   ) {
        for (sharedElementView in mSharedElementViews) {
            forceSharedElementLayout(sharedElementView)
        }
    }
    
    /**
     * Maps all views that don't start with "android" namespace.
     *
     * @param names All shared element names.
     * @return The obsolete shared element names.
     */
    private fun mapObsoleteElements(names: List<String>): List<String> {
        val elementsToRemove = ArrayList<String>(names.size)
        for (name in names) {
            if (name.startsWith("android")) continue
            elementsToRemove.add(name)
        }
        return elementsToRemove
    }
    
    /**
     * Removes obsolete elements from names and shared elements.
     *
     * @param names            Shared element names.
     * @param sharedElements   Shared elements.
     * @param elementsToRemove The elements that should be removed.
     */
    private fun removeObsoleteElements(
        names: MutableList<String>,
        sharedElements: MutableMap<String, View?>,
        elementsToRemove: List<String>
                                      ) {
        if (elementsToRemove.isNotEmpty()) {
            names.removeAll(elementsToRemove)
            for (elementToRemove in elementsToRemove) {
                sharedElements.remove(elementToRemove)
            }
        }
    }
    
    private fun forceSharedElementLayout(view: View?) {
        view ?: return
        val widthSpec = View.MeasureSpec.makeMeasureSpec(view.width, View.MeasureSpec.EXACTLY)
        val heightSpec = View.MeasureSpec.makeMeasureSpec(view.height, View.MeasureSpec.EXACTLY)
        view.measure(widthSpec, heightSpec)
        view.layout(view.left, view.top, view.right, view.bottom)
    }
}
