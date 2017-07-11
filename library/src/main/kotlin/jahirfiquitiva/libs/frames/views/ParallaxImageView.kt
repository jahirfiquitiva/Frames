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

package jahirfiquitiva.libs.frames.views

import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.View
import ca.allanwang.kau.utils.dpToPx
import jahirfiquitiva.libs.kauextensions.ui.views.LandscapeImageView

/**
 * Based on Lukas Koller ParallaxImageView from his app Camera Roll
 */
class ParallaxImageView:LandscapeImageView {

    var parallaxOffset = 50.dpToPx
    var rvHeight = -1
    var rvLocation = intArrayOf(-1, -1)

    constructor(context:Context):super(context)
    constructor(context:Context, attributeSet:AttributeSet):super(context, attributeSet) {
        init(context, attributeSet)
    }

    constructor(context:Context, attributeSet:AttributeSet, defStyleAttr:Int)
            :super(context, attributeSet, defStyleAttr) {
        init(context, attributeSet)
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(context:Context, attributeSet:AttributeSet, defStyleAttr:Int, defStyleRes:Int)
            :super(context, attributeSet, defStyleAttr, defStyleRes) {
        init(context, attributeSet)
    }

    override fun init(context:Context, attributeSet:AttributeSet) {
        super.init(context, attributeSet)
    }

    override fun onMeasure(widthMeasureSpec:Int, heightMeasureSpec:Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec + parallaxOffset)
    }

    var isAttached = false

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        isAttached = true
        translate()
        val rv:View = rootView.findViewWithTag(PARALLAX_RV_TAG)
        if (rv is RecyclerView) {
            rv.addOnScrollListener(object:RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView:RecyclerView?, dx:Int, dy:Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    if (!isAttached) {
                        recyclerView?.removeOnScrollListener(this)
                        return
                    }
                    if (rvHeight == -1) {
                        rvHeight = rv.height
                        rv.getLocationOnScreen(rvLocation)
                    }
                    translate()
                }
            })
        }
    }

    fun translate() {
        if (rvHeight < 0) return
        val location = IntArray(2)
        getLocationOnScreen(location)
        val visible = location[1] + height > rvLocation[1] || location[1] < rvLocation[1] + rvHeight
        if (!visible) return
        val dy = location[1] - rvLocation[1]
        val translationY = parallaxOffset * dy / rvHeight
        setTranslationY(-translationY.toFloat())
    }
}

const val PARALLAX_RV_TAG = "PARALLAX_RECYCLER_VIEW"