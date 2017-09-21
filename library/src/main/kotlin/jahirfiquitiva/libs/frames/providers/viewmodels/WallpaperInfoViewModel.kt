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
package jahirfiquitiva.libs.frames.providers.viewmodels

import jahirfiquitiva.libs.frames.data.models.Wallpaper
import jahirfiquitiva.libs.frames.helpers.utils.FramesUrlRequests
import jahirfiquitiva.libs.frames.helpers.utils.WallpaperInfo
import jahirfiquitiva.libs.kauextensions.extensions.hasContent

class WallpaperInfoViewModel:BasicViewModel<Wallpaper, WallpaperInfo>() {
    private var hasStartedLoad = false
    
    override fun loadData(parameter:Wallpaper, forceLoad:Boolean) {
        if (!hasStartedLoad || forceLoad) {
            super.loadData(parameter, forceLoad)
            hasStartedLoad = true
        }
    }
    
    override fun loadItems(param:Wallpaper):WallpaperInfo =
            FramesUrlRequests().requestFileInfo(param.url, param.dimensions.hasContent())
    
    override fun postResult(data:WallpaperInfo) {
        super.postResult(data)
        hasStartedLoad = false
    }
}