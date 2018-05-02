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
package jahirfiquitiva.libs.frames.ui.fragments.base

import android.content.Context
import android.support.v7.widget.RecyclerView
import jahirfiquitiva.libs.archhelpers.extensions.lazyViewModel
import jahirfiquitiva.libs.frames.data.models.Collection
import jahirfiquitiva.libs.frames.data.models.Wallpaper
import jahirfiquitiva.libs.frames.providers.viewmodels.CollectionsViewModel
import jahirfiquitiva.libs.frames.providers.viewmodels.WallpapersViewModel
import jahirfiquitiva.libs.kauextensions.extensions.SafeAccess
import jahirfiquitiva.libs.kauextensions.extensions.ctxt

@Suppress("DEPRECATION")
abstract class BaseFramesFragment<in T, in VH : RecyclerView.ViewHolder> :
        BaseDatabaseFragment<T, VH>() {
    
    internal val wallpapersModel: WallpapersViewModel by lazyViewModel()
    internal val collectionsModel: CollectionsViewModel by lazyViewModel()
    
    override fun registerObservers() {
        super.registerObservers()
        wallpapersModel.observe(this) {
            doOnWallpapersChange(ArrayList(it), fromCollectionActivity())
        }
        collectionsModel.observe(this) {
            doOnCollectionsChange(ArrayList(it))
        }
    }
    
    override fun loadDataFromViewModel() {
        super.loadDataFromViewModel()
        ctxt { if (!fromCollectionActivity()) wallpapersModel.loadData(it) }
    }
    
    override fun unregisterObservers() {
        super.unregisterObservers()
        wallpapersModel.destroy(this)
        collectionsModel.destroy(this)
    }
    
    open fun doOnCollectionsChange(data: ArrayList<Collection>) {}
    
    override fun doOnWallpapersChange(data: ArrayList<Wallpaper>, fromCollectionActivity: Boolean) {
        super.doOnWallpapersChange(data, fromCollectionActivity)
        ctxt { if (!fromCollectionActivity) collectionsModel.loadWithContext(it, data) }
    }
    
    abstract fun enableRefresh(enable: Boolean)
    
    open fun reloadData(section: Int) {
        when (section) {
            0, 1 -> ctxt(object : SafeAccess<Context> {
                override fun ifNotNull(obj: Context) {
                    super.ifNotNull(obj)
                    wallpapersModel.loadData(obj, true)
                }
                
                override fun ifNull() {
                    super.ifNull()
                    showErrorSnackbar()
                }
            })
            2 -> getDatabase()?.let { favoritesModel.loadData(it, true) } ?: showErrorSnackbar()
        }
    }
    
    abstract fun applyFilter(filter: String)
    abstract fun scrollToTop()
}