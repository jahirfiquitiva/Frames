package dev.jahir.frames.ui.widgets

import android.content.Context
import android.util.AttributeSet
import com.google.android.material.imageview.ShapeableImageView
import dev.jahir.frames.R

open class LandscapeImageView @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    style: Int = 0
) : ShapeableImageView(context, attributeSet, style) {

    private var heightDivider: Float = 3F

    init {
        init(context, attributeSet)
    }

    private fun init(context: Context, attributeSet: AttributeSet?) {
        val a = context.obtainStyledAttributes(attributeSet, R.styleable.LandscapeImageView, 0, 0)
        try {
            heightDivider = a.getFloat(R.styleable.LandscapeImageView_heightDivider, 3F)
        } finally {
            a.recycle()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        setMeasuredDimension(measuredWidth, (measuredWidth / heightDivider).toInt())
    }
}