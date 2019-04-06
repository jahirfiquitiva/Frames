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
package jahirfiquitiva.libs.frames.ui.fragments.base

import android.content.Context
import androidx.annotation.CallSuper
import androidx.recyclerview.widget.RecyclerView
import jahirfiquitiva.libs.archhelpers.extensions.getViewModel
import jahirfiquitiva.libs.frames.data.models.Collection
import jahirfiquitiva.libs.frames.data.models.Wallpaper
import jahirfiquitiva.libs.frames.ui.activities.base.FavsDbManager
import jahirfiquitiva.libs.frames.viewmodels.CollectionsViewModel
import jahirfiquitiva.libs.frames.viewmodels.WallpapersViewModel
import jahirfiquitiva.libs.kext.extensions.SafeAccess
import jahirfiquitiva.libs.kext.extensions.context

abstract class BaseFramesFragment<in T, in VH : RecyclerView.ViewHolder> :
    BaseDatabaseFragment<T, VH>() {
    
    internal var wallpapersModel: WallpapersViewModel? = null
    internal var collectionsModel: CollectionsViewModel? = null
    
    override fun initViewModels() {
        wallpapersModel = getViewModel()
        collectionsModel = getViewModel()
    }
    
    @CallSuper
    override fun registerObservers() {
        wallpapersModel?.observe(this) {
            doOnWallpapersChange(ArrayList(it), fromCollectionActivity())
        }
        collectionsModel?.observe(this) {
            doOnCollectionsChange(ArrayList(it))
        }
    }
    
    @CallSuper
    override fun loadDataFromViewModel() {
        if (!fromCollectionActivity()) context { wallpapersModel?.loadData(it) }
    }
    
    open fun doOnCollectionsChange(data: ArrayList<Collection>) {}
    
    override fun doOnWallpapersChange(data: ArrayList<Wallpaper>, fromCollectionActivity: Boolean) {
        super.doOnWallpapersChange(data, fromCollectionActivity)
        context { if (!fromCollectionActivity) collectionsModel?.loadWithContext(it, data) }
    }
    
    open fun reloadData(section: Int) {
        when (section) {
            0, 1 -> context(object : SafeAccess<Context> {
                override fun ifNotNull(obj: Context) {
                    super.ifNotNull(obj)
                    wallpapersModel?.loadData(obj, true)
                }
                
                override fun ifNull() {
                    super.ifNull()
                    showErrorSnackBar()
                }
            })
            2 -> (activity as? FavsDbManager)?.reloadFavorites()
        }
    }
    
    abstract fun enableRefresh(enable: Boolean)
    abstract fun applyFilter(filter: String, closed: Boolean)
    abstract fun scrollToTop()
}
