/*
 * Copyright (c) 2017. Jahir Fiquitiva
 *
 * Licensed under the CreativeCommons Attribution-ShareAlike
 * 4.0 International License. You may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *    http://creativecommons.org/licenses/by-sa/4.0/legalcode
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jahirfiquitiva.libs.frames.ui.widgets

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.ViewTreeObserver
import android.widget.ImageView
import jahirfiquitiva.libs.frames.helpers.utils.AsyncTaskManager
import jahirfiquitiva.libs.kauextensions.ui.views.LandscapeImageView

/**
 * Based on Jiazhe Guo's ScrollParallaxImageView
 * https://github.com/gjiazhe/ScrollParallaxImageView
 */
class ParallaxImageView:LandscapeImageView, ViewTreeObserver.OnScrollChangedListener {
    
    private var viewLocation = IntArray(2)
    
    constructor(context:Context):super(context)
    constructor(context:Context, attributeSet:AttributeSet):super(context, attributeSet)
    constructor(context:Context, attributeSet:AttributeSet, defStyleAttr:Int)
            :super(context, attributeSet, defStyleAttr)
    
    override fun onMeasure(widthMeasureSpec:Int, heightMeasureSpec:Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        invalidate()
    }
    
    override fun setImageBitmap(bm:Bitmap?) {
        super.setImageBitmap(bm)
        bm?.let {
            AsyncTaskManager(it, {}, { true }, { invalidate() }).execute()
        }
    }
    
    override fun onDraw(canvas:Canvas) {
        if (drawable == null) {
            super.onDraw(canvas)
            return
        }
        getLocationInWindow(viewLocation)
        transform(canvas, viewLocation[1])
        super.onDraw(canvas)
    }
    
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        viewTreeObserver.addOnScrollChangedListener(this)
    }
    
    override fun onDetachedFromWindow() {
        viewTreeObserver.removeOnScrollChangedListener(this)
        super.onDetachedFromWindow()
    }
    
    override fun onScrollChanged() {
        invalidate()
    }
    
    private fun transform(canvas:Canvas, y:Int) {
        var nY = y
        if (scaleType != ImageView.ScaleType.CENTER_CROP) {
            return
        }
        
        // image's width and height
        val iWidth = drawable.intrinsicWidth
        val iHeight = drawable.intrinsicHeight
        if (iWidth <= 0 || iHeight <= 0) {
            return
        }
        
        // view's width and height
        val vWidth = width - paddingLeft - paddingRight
        val vHeight = height - paddingTop - paddingBottom
        
        // device's height
        val dHeight = resources.displayMetrics.heightPixels
        
        if (iWidth * vHeight < iHeight * vWidth) {
            // avoid over scroll
            if (nY < -vHeight) {
                nY = -vHeight
            } else if (nY > dHeight) {
                nY = dHeight
            }
            
            val imgScale = vWidth.toFloat() / iWidth.toFloat()
            val max_dy = Math.abs((iHeight * imgScale - vHeight) * 0.5f)
            val translateY = -(2f * max_dy * nY.toFloat() + max_dy * (vHeight - dHeight)) / (vHeight + dHeight)
            canvas.translate(0f, translateY)
        }
    }
}