package dev.jahir.frames.ui.widgets

import android.content.Context
import android.graphics.Matrix
import android.graphics.drawable.Drawable
import android.net.Uri
import android.util.AttributeSet
import com.github.chrisbanes.photoview.PhotoView

class FramesPhotoView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : PhotoView(context, attrs, defStyle) {

    init {
        scaleType = ScaleType.CENTER_CROP
        scale = 1F
        attacher?.update()
    }

    override fun setImageDrawable(drawable: Drawable?) {
        setImageToLastScale { super.setImageDrawable(drawable) }
    }

    override fun setImageResource(resId: Int) {
        setImageToLastScale { super.setImageResource(resId) }
    }

    override fun setImageURI(uri: Uri?) {
        setImageToLastScale { super.setImageURI(uri) }
    }

    private fun setImageToLastScale(setImage: () -> Unit) {
        val lastMatrix = Matrix().apply { getSuppMatrix(this) }
        setImage()
        setSuppMatrix(lastMatrix)
    }
}