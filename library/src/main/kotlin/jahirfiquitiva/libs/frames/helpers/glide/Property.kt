/*
 * Copyright (c) 2019. Jahir Fiquitiva
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

import android.os.Build
import android.util.FloatProperty
import android.util.Property

/**
 * A delegate for creating a [Property] of `float` type.
 * Credits: https://github.com/chrisbanes/tivi/
 */
abstract class FloatProp<T>(val name: String) {
    abstract operator fun set(o: T, value: Float)
    abstract operator fun get(o: T): Float
}

fun <T> createFloatProperty(impl: FloatProp<T>): Property<T, Float> {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        object : FloatProperty<T>(impl.name) {
            override fun get(o: T): Float = impl[o]
            
            override fun setValue(o: T, value: Float) {
                impl[o] = value
            }
        }
    } else {
        object : Property<T, Float>(Float::class.java, impl.name) {
            override fun get(o: T): Float = impl[o]
            
            override fun set(o: T, value: Float) {
                impl[o] = value
            }
        }
    }
}
