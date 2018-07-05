/*
 * Copyright (c) 2018. Jahir Fiquitiva
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
package jahirfiquitiva.libs.frames.helpers.glide

import android.graphics.ColorMatrix

/**
 * An extension to [ColorMatrix] which caches the saturation value for animation purposes.
 * Credits: https://github.com/chrisbanes/tivi/
 */
class TiviColorMatrix : ColorMatrix() {
    @Suppress("PropertyName")
    var _saturation = 1f
        private set
    
    override fun setSaturation(sat: Float) {
        _saturation = sat
        super.setSaturation(sat)
    }
    
    companion object {
        private val saturationFloatProp = object : FloatProp<TiviColorMatrix>("saturation") {
            override operator fun get(o: TiviColorMatrix): Float = o._saturation
            override operator fun set(o: TiviColorMatrix, value: Float) =
                o.setSaturation(value)
        }
        val PROP_SATURATION = createFloatProperty(saturationFloatProp)
    }
}