package dev.jahir.frames.ui.widgets

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Shader
import android.util.AttributeSet
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.shape.ShapeAppearanceModel
import com.google.android.material.shape.ShapeAppearancePathProvider
import dev.jahir.frames.R

open class PortraitImageView @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    style: Int = 0
) : ShapeableImageView(context, attributeSet, style) {

    private var heightMultiplier: Float = 1.25F

    private val pathProvider: ShapeAppearancePathProvider = ShapeAppearancePathProvider()

    private val overlay: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val overlayBounds: RectF = RectF()
    private val overlayPath: Path = Path()

    private val overlayGradient: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var overlayGradientModel: ShapeAppearanceModel = ShapeAppearanceModel()
    private val overlayGradientBounds: RectF = RectF()
    private val overlayGradientPath: Path = Path()
    private val gradientColors: IntArray = intArrayOf(0x80000000.toInt(), 0x33000000, 0x00000000)

    init {
        init(context, attributeSet)
    }

    private fun init(context: Context, attributeSet: AttributeSet?) {
        val a = context.obtainStyledAttributes(attributeSet, R.styleable.PortraitImageView, 0, 0)
        try {
            heightMultiplier = a.getFloat(R.styleable.PortraitImageView_heightMultiplier, 1.25F)
            setOverlayColor(a.getColor(R.styleable.PortraitImageView_overlayColor, Color.TRANSPARENT))
        } finally {
            a.recycle()
        }
    }

    fun setOverlayColor(color: Int) {
        overlay.color = color
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        updateOverlayModel()
    }

    override fun setShapeAppearanceModel(shapeAppearanceModel: ShapeAppearanceModel) {
        super.setShapeAppearanceModel(shapeAppearanceModel)
        updateOverlayModel()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val targetHeight: Int = (MeasureSpec.getSize(widthMeasureSpec) * heightMultiplier).toInt()
        super.onMeasure(widthMeasureSpec, targetHeight or MeasureSpec.EXACTLY)
        setMeasuredDimension(measuredWidth, (measuredHeight ))
        updateOverlayParams()
        updateGradientParams()
    }

    private fun updateOverlayParams() {
        overlayBounds.set(0f, 0f, measuredWidth.toFloat(), measuredHeight.toFloat())
        overlayPath.reset()
        pathProvider.calculatePath(shapeAppearanceModel, 1f, overlayBounds, overlayPath)
    }

    private fun updateGradientParams() {
        val gradientEnd: Float = measuredHeight.toFloat()
        val gradientStart: Float = gradientEnd * 0.7f
        overlayGradientBounds.set(0f, gradientStart, measuredWidth.toFloat(), gradientEnd)
        overlayGradient.shader = LinearGradient(0f, gradientEnd, 0f, gradientStart,
            gradientColors, floatArrayOf(0f, 0.5f, 1f), Shader.TileMode.CLAMP)
        overlayGradientPath.reset()
        pathProvider.calculatePath(overlayGradientModel, 1f, overlayGradientBounds,
            overlayGradientPath)
    }

    private fun updateOverlayModel() {
        overlayGradientModel = overlayGradientModel.toBuilder()
            .setBottomLeftCorner(shapeAppearanceModel.bottomLeftCorner)
            .setBottomRightCorner(shapeAppearanceModel.bottomRightCorner)
            .setBottomLeftCornerSize(shapeAppearanceModel.bottomLeftCornerSize)
            .setBottomRightCornerSize(shapeAppearanceModel.bottomRightCornerSize)
            .build()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawPath(overlayPath, overlay)
        canvas.drawPath(overlayGradientPath, overlayGradient)
    }

}