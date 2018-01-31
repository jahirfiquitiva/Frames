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
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.support.v4.view.ViewCompat
import android.support.v7.widget.Toolbar
import android.transition.Transition
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import ca.allanwang.kau.utils.postDelayed
import jahirfiquitiva.libs.frames.R
import jahirfiquitiva.libs.frames.data.models.Collection
import jahirfiquitiva.libs.frames.data.models.Wallpaper
import jahirfiquitiva.libs.frames.ui.fragments.WallpapersInCollectionFragment
import jahirfiquitiva.libs.frames.ui.widgets.CustomToolbar
import jahirfiquitiva.libs.kauextensions.extensions.bind
import jahirfiquitiva.libs.kauextensions.extensions.changeOptionVisibility
import jahirfiquitiva.libs.kauextensions.extensions.getActiveIconsColorFor
import jahirfiquitiva.libs.kauextensions.extensions.getPrimaryTextColorFor
import jahirfiquitiva.libs.kauextensions.extensions.getSecondaryTextColorFor
import jahirfiquitiva.libs.kauextensions.extensions.hideAllItems
import jahirfiquitiva.libs.kauextensions.extensions.primaryColor
import jahirfiquitiva.libs.kauextensions.extensions.showAllItems
import jahirfiquitiva.libs.kauextensions.extensions.tint
import jahirfiquitiva.libs.kauextensions.ui.activities.FragmentsActivity
import jahirfiquitiva.libs.kauextensions.ui.widgets.CustomSearchView

open class CollectionActivity : FragmentsActivity() {
    
    override fun lightTheme(): Int = R.style.LightTheme
    override fun darkTheme(): Int = R.style.DarkTheme
    override fun transparentTheme(): Int = R.style.TransparentTheme
    override fun amoledTheme(): Int = R.style.AmoledTheme
    override fun fragmentsContainer(): Int = R.id.fragments_container
    
    private var fragmentLoaded = false
    private var closing = false
    private var collection: Collection? = null
    
    private var frag: WallpapersInCollectionFragment? = null
    
    private val toolbar: CustomToolbar by bind(R.id.toolbar)
    private var searchView: CustomSearchView? = null
    
    override fun autoTintStatusBar(): Boolean = true
    override fun autoTintNavigationBar(): Boolean = true
    
    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportPostponeEnterTransition()
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val decor = window.decorView
            val statusBar: View by decor.bind(android.R.id.statusBarBackground)
            val navBar: View by decor.bind(android.R.id.navigationBarBackground)
            val actionBar: View by decor.bind(R.id.action_bar_container)
            
            val viewsToExclude = arrayOf(statusBar, navBar, actionBar)
            val extraViewsToExclude = arrayOf(R.id.appbar, R.id.toolbar, R.id.tabs)
            
            viewsToExclude.forEach { window.sharedElementEnterTransition?.excludeTarget(it, true) }
            extraViewsToExclude.forEach {
                window.sharedElementEnterTransition?.excludeTarget(it, true)
            }
            
            window.enterTransition?.addListener(
                    object : Transition.TransitionListener {
                        override fun onTransitionPause(p0: Transition?) = loadFragment()
                        override fun onTransitionCancel(p0: Transition?) = loadFragment()
                        override fun onTransitionEnd(p0: Transition?) = loadFragment()
                        override fun onTransitionStart(p0: Transition?) {}
                        override fun onTransitionResume(p0: Transition?) {}
                    })
        }
        
        setContentView(R.layout.activity_collection_settings)
        supportStartPostponedEnterTransition()
        
        toolbar.bindToActivity(this)
        
        val container: FrameLayout by bind(fragmentsContainer())
        with(container) {
            setPadding(paddingLeft, paddingTop, paddingRight, 0)
        }
        
        collection = intent?.getParcelableExtra("item")
        initContent()
    }
    
    private fun initContent(loadFragment: Boolean = false) {
        setupToolbarTitle(toolbar)
        
        val number = collection?.wallpapers?.size ?: 0
        if (number > 0) toolbar.subtitle = getString(R.string.x_wallpapers, number.toString())
        toolbar.tint(
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
            it.changeOptionVisibility(R.id.donate, false)
            it.changeOptionVisibility(R.id.about, false)
            it.changeOptionVisibility(R.id.settings, false)
            
            val searchItem = it.findItem(R.id.search)
            searchView = searchItem.actionView as? CustomSearchView
            searchView?.onExpand = { it.hideAllItems() }
            searchView?.onCollapse = {
                it.showAllItems()
                doSearch()
            }
            searchView?.onQueryChanged = { doSearch(it) }
            searchView?.onQuerySubmit = { doSearch(it) }
            searchView?.bindToItem(searchItem)
            searchView?.queryHint = getString(R.string.search_x, getString(R.string.wallpapers))
            
            searchView?.tint(getPrimaryTextColorFor(primaryColor, 0.6F))
            it.tint(getActiveIconsColorFor(primaryColor, 0.6F))
        }
        
        toolbar.tint(
                getPrimaryTextColorFor(primaryColor, 0.6F),
                getSecondaryTextColorFor(primaryColor, 0.6F),
                getActiveIconsColorFor(primaryColor, 0.6F))
        return super.onCreateOptionsMenu(menu)
    }
    
    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item?.itemId == android.R.id.home) {
            doFinish()
        }
        return super.onOptionsItemSelected(item)
    }
    
    override fun onBackPressed() = doFinish()
    
    private val lock = Any()
    private fun doSearch(filter: String = "") {
        try {
            synchronized(
                    lock, {
                postDelayed(200, { frag?.applyFilter(filter) })
            })
        } catch (ignored: Exception) {
        }
    }
    
    private fun doFinish() {
        if (!closing) {
            closing = true
            val intent = Intent()
            try {
                intent.putExtra("nFavs", frag?.newFavs ?: ArrayList<Wallpaper>())
            } catch (ignored: Exception) {
            }
            setResult(11, intent)
            supportFinishAfterTransition()
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
    
    @SuppressLint("MissingSuperCall")
    override fun onResume() {
        super.onResume()
        initContent()
    }
}