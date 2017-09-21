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
package jahirfiquitiva.libs.frames.helpers.extensions

import java.io.File

fun Long.toReadableByteCount(si:Boolean = false):String {
    if (this <= 0L) return "-0"
    try {
        val unit = if (si) 1000 else 1024
        if (this < unit) return "$this B"
        val exp = (Math.log(this.toDouble()) / Math.log(unit.toDouble())).toInt()
        val pre = (if (si) "kMGTPE" else "KMGTPE")[exp - 1] + if (si) "" else "i"
        return String.format("%.1f %sB", this / Math.pow(unit.toDouble(), exp.toDouble()), pre)
    } catch (ignored:Exception) {
        return "-0"
    }
}

val File.dirSize:Long
    get() {
        if (exists()) {
            var result:Long = 0
            listFiles().forEach {
                result += if (it.isDirectory) it.dirSize else it.length()
            }
            return result
        }
        return 0
    }