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
package jahirfiquitiva.libs.frames.ui.activities

import android.annotation.SuppressLint
import android.arch.persistence.room.Room
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.view.ViewCompat
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.widget.FrameLayout
import android.widget.TextView
import ca.allanwang.kau.utils.contentView
import ca.allanwang.kau.utils.postDelayed
import jahirfiquitiva.libs.archhelpers.extensions.lazyViewModel
import jahirfiquitiva.libs.frames.R
import jahirfiquitiva.libs.frames.data.models.Collection
import jahirfiquitiva.libs.frames.data.models.Wallpaper
import jahirfiquitiva.libs.frames.data.models.db.FavoritesDatabase
import jahirfiquitiva.libs.frames.helpers.extensions.framesPostponeEnterTransition
import jahirfiquitiva.libs.frames.helpers.utils.DATABASE_NAME
import jahirfiquitiva.libs.frames.helpers.utils.FL
import jahirfiquitiva.libs.frames.helpers.utils.FramesKonfigs
import jahirfiquitiva.libs.frames.providers.viewmodels.FavoritesViewModel
import jahirfiquitiva.libs.frames.ui.activities.base.FavsDbManager
import jahirfiquitiva.libs.frames.ui.fragments.WallpapersInCollectionFragment
import jahirfiquitiva.libs.frames.ui.widgets.CustomToolbar
import jahirfiquitiva.libs.kext.extensions.bind
import jahirfiquitiva.libs.kext.extensions.buildSnackbar
import jahirfiquitiva.libs.kext.extensions.getActiveIconsColorFor
import jahirfiquitiva.libs.kext.extensions.getPrimaryTextColorFor
import jahirfiquitiva.libs.kext.extensions.getSecondaryTextColorFor
import jahirfiquitiva.libs.kext.extensions.primaryColor
import jahirfiquitiva.libs.kext.extensions.setItemVisibility
import jahirfiquitiva.libs.kext.extensions.tint
import jahirfiquitiva.libs.kext.ui.activities.ActivityWFragments
import jahirfiquitiva.libs.kext.ui.widgets.CustomSearchView
import org.jetbrains.anko.doAsync

class CollectionActivity : ActivityWFragments<FramesKonfigs>(), FavsDbManager {
    
    override val configs: FramesKonfigs by lazy { FramesKonfigs(this) }
    override fun lightTheme(): Int = R.style.Frames_LightTheme
    override fun darkTheme(): Int = R.style.Frames_DarkTheme
    override fun transparentTheme(): Int = R.style.Frames_TransparentTheme
    override fun amoledTheme(): Int = R.style.Frames_AmoledTheme
    override fun fragmentsContainer(): Int = R.id.fragments_container
    
    private var fragmentLoaded = false
    private var closing = false
    private var collection: Collection? = null
    
    private var frag: WallpapersInCollectionFragment? = null
    
    private val toolbar: CustomToolbar? by bind(R.id.toolbar)
    private var searchView: CustomSearchView? = null
    
    private var errorSnackbar: Snackbar? = null
    
    override val favsViewModel: FavoritesViewModel by lazyViewModel()
    override val favsDB: FavoritesDatabase by lazy {
        Room.databaseBuilder(this, FavoritesDatabase::class.java, DATABASE_NAME)
            .fallbackToDestructiveMigration().build()
    }
    
    @SuppressLint("MissingSuperCall", "InlinedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        framesPostponeEnterTransition { loadFragment() }
        
        setContentView(R.layout.activity_collection_settings)
        supportStartPostponedEnterTransition()
        
        toolbar?.bindToActivity(this)
        
        val container: FrameLayout? by bind(fragmentsContainer())
        container?.let { with(it) { setPadding(paddingLeft, paddingTop, paddingRight, 0) } }
        
        collection = intent?.getParcelableExtra("item")
        
        initContent(true)
        
        favsViewModel.observe(this) { notifyFavsToFrags(it) }
        doAsync { favsViewModel.loadData(favsDB.favoritesDao(), true) }
    }
    
    @SuppressLint("MissingSuperCall")
    override fun onResume() {
        super.onResume()
        initContent()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        favsViewModel.destroy(this)
    }
    
    private fun initContent(loadFragment: Boolean = false) {
        toolbar?.let { setupToolbarTitle(it) }
        
        val number = collection?.wallpapers?.size ?: 0
        if (number > 0) toolbar?.subtitle = getString(R.string.x_wallpapers, number.toString())
        toolbar?.tint(
            getPrimaryTextColorFor(primaryColor, 0.6F),
            getSecondaryTextColorFor(primaryColor, 0.6F),
            getActiveIconsColorFor(primaryColor, 0.6F))
        
        if (loadFragment) loadFragment(true)
    }
    
    private fun loadFragment(force: Boolean = false) {
        collection?.let {
            if (!fragmentLoaded || force) {
                fragmentLoaded = true
                frag = WallpapersInCollectionFragment.create(
                    it, it.wallpapers,
                    intent?.getBooleanExtra("checker", false) ?: false)
                frag?.let { changeFragment(it) }
                doAsync { favsViewModel.loadData(favsDB.favoritesDao(), true) }
            }
        }
    }
    
    private fun setupToolbarTitle(toolbar: Toolbar) {
        val title: TextView?
        try {
            val f = toolbar.javaClass.getDeclaredField("mTitleTextView")
            f.isAccessible = true
            title = f.get(toolbar) as? TextView
            title?.text = collection?.name ?: ""
            ViewCompat.setTransitionName(title, "title")
        } catch (ignored: Exception) {
        } finally {
            toolbar.title = collection?.name ?: ""
            supportActionBar?.title = collection?.name ?: ""
        }
    }
    
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.frames_menu, menu)
        
        menu?.let {
            it.setItemVisibility(R.id.donate, false)
            it.setItemVisibility(R.id.about, false)
            it.setItemVisibility(R.id.settings, false)
            
            val searchItem = it.findItem(R.id.search)
            searchView = searchItem?.actionView as? CustomSearchView
            searchView?.onExpand = { toolbar?.enableScroll(false) }
            searchView?.onCollapse = {
                toolbar?.enableScroll(true)
                doSearch(closed = true)
            }
            searchView?.onQueryChanged = { doSearch(it) }
            searchView?.onQuerySubmit = { doSearch(it) }
            searchView?.bindToItem(searchItem)
            searchView?.queryHint = getString(R.string.search_x, getString(R.string.wallpapers))
            
            searchView?.tint(getPrimaryTextColorFor(primaryColor, 0.6F))
            it.tint(getActiveIconsColorFor(primaryColor, 0.6F))
        }
        
        toolbar?.tint(
            getPrimaryTextColorFor(primaryColor, 0.6F),
            getSecondaryTextColorFor(primaryColor, 0.6F),
            getActiveIconsColorFor(primaryColor, 0.6F))
        return super.onCreateOptionsMenu(menu)
    }
    
    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            android.R.id.home -> doFinish()
            R.id.refresh -> frag?.reloadData(1)
        }
        return super.onOptionsItemSelected(item)
    }
    
    override fun onBackPressed() = doFinish()
    
    private val lock = Any()
    private fun doSearch(filter: String = "", closed: Boolean = false) {
        try {
            synchronized(lock) {
                postDelayed(150) { frag?.applyFilter(filter, closed) }
            }
        } catch (ignored: Exception) {
        }
    }
    
    private fun doFinish() {
        if (!closing) {
            closing = true
            val intent = Intent()
            try {
                intent.putExtra("nFavs", ArrayList<Wallpaper>(frag?.newFavs.orEmpty()))
            } catch (e: Exception) {
                FL.e("Error", e)
            }
            setResult(11, intent)
            try {
                supportFinishAfterTransition()
            } catch (e: Exception) {
                finish()
            }
        }
    }
    
    override fun onSaveInstanceState(outState: Bundle?) {
        outState?.putParcelable("item", collection)
        super.onSaveInstanceState(outState)
    }
    
    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        super.onRestoreInstanceState(savedInstanceState)
        savedInstanceState?.let {
            collection = it.getParcelable("item")
            initContent(true)
        }
    }
    
    override fun notifyFavsToFrags(favs: ArrayList<Wallpaper>) {
        frag?.doOnFavoritesChange(favs)
    }
    
    override fun showSnackbar(text: String) {
        errorSnackbar?.dismiss()
        errorSnackbar = null
        errorSnackbar = contentView?.buildSnackbar(text)
        errorSnackbar?.view?.findViewById<TextView>(R.id.snackbar_text)?.setTextColor(Color.WHITE)
        errorSnackbar?.show()
    }
}