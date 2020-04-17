package dev.jahir.frames.ui.widgets

import android.content.Context
import android.util.AttributeSet
import com.google.android.material.imageview.ShapeableImageView
import dev.jahir.frames.R

open class PortraitImageView @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    style: Int = 0
) : ShapeableImageView(context, attributeSet, style) {

    private var heightMultiplier: Float = 1.25F

    init {
        init(context, attributeSet)
    }

    private fun init(context: Context, attributeSet: AttributeSet?) {
        val a = context.obtainStyledAttributes(attributeSet, R.styleable.PortraitImageView, 0, 0)
        try {
            heightMultiplier = a.getFloat(R.styleable.PortraitImageView_heightMultiplier, 1.25F)
        } finally {
            a.recycle()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        setMeasuredDimension(measuredWidth, (measuredWidth * heightMultiplier).toInt())
    }
}