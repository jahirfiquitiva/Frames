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
package jahirfiquitiva.libs.frames.ui.graphics

import android.graphics.ColorMatrix
import android.util.FloatProperty
import android.util.Property

/**
 * Credits to Mysplash
 * @author WangDaYeeeeee
 * https://goo.gl/heH8Qv
 */
class ObservableColorMatrix : ColorMatrix() {

    private var mSaturation = 1F
    
    override fun setSaturation(sat: Float) {
        mSaturation = sat
        super.setSaturation(sat)
    }
    
    companion object {
        val SATURATION: Property<ObservableColorMatrix, Float> =
                object : FloatProperty<ObservableColorMatrix>("saturation") {
                    override fun setValue(cm: ObservableColorMatrix, value: Float) {
                        cm.setSaturation(value)
                    }
                    
                    override operator fun get(cm: ObservableColorMatrix): Float? = cm.mSaturation
                }
    }
}