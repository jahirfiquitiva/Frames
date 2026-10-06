package dev.jahir.frames.ui.animations

import android.view.View
import com.google.android.material.transition.platform.MaterialContainerTransformSharedElementCallback
import dev.jahir.frames.ui.activities.ViewerActivity

/**
 * Exit callback for screens that open the viewer. The viewer can move to other wallpapers,
 * so before returning, [setReturnTarget] points the transition at the card of the wallpaper
 * that was last shown, instead of the one that opened the viewer.
 */
internal class WallpaperReturnSharedElementCallback :
    MaterialContainerTransformSharedElementCallback() {

    private var hasReturnTarget = false
    private var returnTarget: View? = null

    /** [card] null means the wallpaper is not in the list: return without a shared element */
    fun setReturnTarget(card: View?) {
        hasReturnTarget = true
        returnTarget = card
    }

    override fun onMapSharedElements(
        names: MutableList<String>,
        sharedElements: MutableMap<String, View>
    ) {
        if (hasReturnTarget) {
            val card = returnTarget
            if (card != null) {
                sharedElements[ViewerActivity.TRANSITION_NAME] = card
            } else {
                names.remove(ViewerActivity.TRANSITION_NAME)
                sharedElements.remove(ViewerActivity.TRANSITION_NAME)
            }
            hasReturnTarget = false
            returnTarget = null
        }
        // Material reads the shape of the mapped view, so it has to run after the remap
        super.onMapSharedElements(names, sharedElements)
    }
}
