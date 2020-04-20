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
import android.view.View
import android.view.ViewGroup
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
    private var gradientColors: IntArray = intArrayOf(0x80000000.toInt(), 0x33000000, 0x00000000)

    init {
        init(context, attributeSet)
    }

    private fun init(context: Context, attributeSet: AttributeSet?) {
        val a = context.obtainStyledAttributes(attributeSet, R.styleable.PortraitImageView, 0, 0)
        try {
            heightMultiplier = a.getFloat(R.styleable.PortraitImageView_heightMultiplier, 1.25F)
            setOverlayColor(
                a.getColor(R.styleable.PortraitImageView_overlayColor, Color.TRANSPARENT)
            )
        } finally {
            a.recycle()
        }
    }

    fun setOverlayColor(color: Int) {
        overlay.color = color
    }

    fun setGradientColors(colors: IntArray) {
        gradientColors = colors
        updateGradientParams()
        invalidate()
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
        updateOverlayParams()
        updateGradientParams()
    }

    private fun updateOverlayParams() {
        overlayBounds.set(0F, 0F, measuredWidth.toFloat(), measuredHeight.toFloat())
        overlayPath.reset()
        pathProvider.calculatePath(shapeAppearanceModel, 1F, overlayBounds, overlayPath)
    }

    private fun updateGradientParams() {
        val textsHeight: Int = try {
            (parent?.parent as? ViewGroup)
                ?.findViewById<View?>(R.id.wallpaper_details_background)
                ?.measuredHeight ?: 0
        } catch (e: Exception) {
            0
        }
        val gradientEnd: Float = measuredHeight.toFloat()
        val gradientStart: Float =
            if (textsHeight > 0) (gradientEnd - textsHeight.toFloat()) else (gradientEnd * 0.7F)
        overlayGradientBounds.set(0F, gradientStart, measuredWidth.toFloat(), gradientEnd)
        overlayGradient.shader = LinearGradient(
            0F, gradientEnd, 0F, gradientStart,
            gradientColors, floatArrayOf(0F, 0.5F, 1F), Shader.TileMode.CLAMP
        )
        overlayGradientPath.reset()
        pathProvider.calculatePath(
            overlayGradientModel, 1F, overlayGradientBounds,
            overlayGradientPath
        )
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