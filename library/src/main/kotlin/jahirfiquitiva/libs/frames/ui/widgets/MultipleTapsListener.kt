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

import android.os.Handler
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration

abstract class MultipleTapsListener:View.OnTouchListener {
    abstract fun onSingleTap()
    abstract fun onDoubleTap()
    abstract fun onLongPress()
    
    private val handler = Handler()
    private var tapsCount = 0
    private var lastTapTimeMs = 0L
    private var touchDownMs = 0L
    
    private val singleTapTimeout = ViewConfiguration.getTapTimeout() * 1.25F
    private val doubleTapTimeout = ViewConfiguration.getDoubleTapTimeout() * 1.25F
    private val longPressTimeout = ViewConfiguration.getLongPressTimeout() * 1.25F
    
    override fun onTouch(view:View?, me:MotionEvent?):Boolean {
        me?.let {
            when (it.action) {
                MotionEvent.ACTION_DOWN -> {
                    touchDownMs = System.currentTimeMillis()
                }
                MotionEvent.ACTION_UP -> {
                    handler.removeCallbacksAndMessages(null)
                    
                    val elapsedTime = System.currentTimeMillis() - touchDownMs
                    
                    if (elapsedTime > singleTapTimeout) {
                        if (elapsedTime >= longPressTimeout) onLongPress()
                        tapsCount = 0
                        lastTapTimeMs = 0
                        return@let
                    }
                    
                    if ((tapsCount > 0) && (System.currentTimeMillis() - lastTapTimeMs) < doubleTapTimeout) {
                        tapsCount += 1
                    } else {
                        tapsCount = 1
                    }
                    
                    lastTapTimeMs = System.currentTimeMillis()
                    
                    if (tapsCount == 2) {
                        handler.postDelayed({ onDoubleTap() }, doubleTapTimeout.toLong())
                    } else if (tapsCount == 1) {
                        handler.postDelayed({ onSingleTap() }, singleTapTimeout.toLong())
                    }
                }
            }
        }
        return true
    }
}