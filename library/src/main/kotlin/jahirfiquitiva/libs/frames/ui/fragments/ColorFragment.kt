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
package jahirfiquitiva.libs.frames.ui.fragments

import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import jahirfiquitiva.libs.frames.R

class ColorFragment : Fragment() {
    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
                             ): View? {
        val view = inflater.inflate(R.layout.color_fragment, container, false)
        view?.findViewById<View>(R.id.colored)?.setBackgroundColor(Color.parseColor(color))
        return view
    }
    
    private var color = "#2196f3"
    
    companion object {
        fun create(color: String): ColorFragment = ColorFragment().apply { this.color = color }
    }
}