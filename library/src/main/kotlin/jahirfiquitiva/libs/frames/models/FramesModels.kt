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

package jahirfiquitiva.libs.frames.models

data class Wallpaper(val name:String, val author:String, val collections:String, val url:String,
                     val thumbUrl:String = url) {
    override fun equals(other:Any?):Boolean {
        if (other !is Wallpaper) return false
        return url.equals(other.url, true) || thumbUrl.equals(other.thumbUrl, true)
    }

    override fun hashCode():Int {
        var result = name.hashCode()
        result = 31 * result + author.hashCode()
        result = 31 * result + url.hashCode()
        result = 31 * result + thumbUrl.hashCode()
        return result
    }
}

data class Collection(val name:String, val wallpapers:ArrayList<Wallpaper>)