package dev.jahir.frames.ui.widgets

import android.content.Context
import android.util.AttributeSet
import com.google.android.material.imageview.ShapeableImageView

open class SquaredImageView @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    style: Int = 0
) :
    ShapeableImageView(context, attributeSet, style) {

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        setMeasuredDimension(widthMeasureSpec, widthMeasureSpec)
    }
}