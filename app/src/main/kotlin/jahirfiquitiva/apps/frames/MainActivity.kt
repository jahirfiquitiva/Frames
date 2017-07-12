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
package jahirfiquitiva.apps.frames

import android.os.Bundle
import com.github.javiersantos.piracychecker.PiracyChecker
import jahirfiquitiva.libs.frames.activities.FramesActivity

class MainActivity:FramesActivity() {
    override fun donationsEnabled():Boolean = false
    override fun amazonInstallsEnabled():Boolean = false
    override fun getLicKey():String? = null
    override fun getLicenseChecker():PiracyChecker? = null

    override fun onCreate(savedInstanceState:Bundle?) {
        super.onCreate(savedInstanceState)
    }
}