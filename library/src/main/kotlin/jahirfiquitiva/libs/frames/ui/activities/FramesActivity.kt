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
package jahirfiquitiva.libs.frames.ui.activities

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import androidx.room.Room
import androidx.viewpager.widget.ViewPager
import ca.allanwang.kau.utils.contentView
import ca.allanwang.kau.utils.postDelayed
import ca.allanwang.kau.utils.tint
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import jahirfiquitiva.libs.archhelpers.extensions.lazyViewModel
import jahirfiquitiva.libs.frames.R
import jahirfiquitiva.libs.frames.data.models.Wallpaper
import jahirfiquitiva.libs.frames.data.models.db.FavoritesDatabase
import jahirfiquitiva.libs.frames.helpers.extensions.showChanges
import jahirfiquitiva.libs.frames.helpers.utils.DATABASE_NAME
import jahirfiquitiva.libs.frames.helpers.utils.FramesKonfigs
import jahirfiquitiva.libs.frames.ui.activities.base.BaseFramesActivity
import jahirfiquitiva.libs.frames.ui.activities.base.FavsDbManager
import jahirfiquitiva.libs.frames.ui.fragments.base.BaseDatabaseFragment
import jahirfiquitiva.libs.frames.ui.fragments.base.BaseFramesFragment
import jahirfiquitiva.libs.frames.ui.fragments.base.BaseWallpapersFragment
import jahirfiquitiva.libs.frames.ui.widgets.CustomToolbar
import jahirfiquitiva.libs.frames.viewmodels.FavoritesViewModel
import jahirfiquitiva.libs.kext.extensions.accentColor
import jahirfiquitiva.libs.kext.extensions.bind
import jahirfiquitiva.libs.kext.extensions.boolean
import jahirfiquitiva.libs.kext.extensions.buildSnackbar
import jahirfiquitiva.libs.kext.extensions.drawable
import jahirfiquitiva.libs.kext.extensions.getActiveIconsColorFor
import jahirfiquitiva.libs.kext.extensions.getDisabledTextColorFor
import jahirfiquitiva.libs.kext.extensions.getInactiveIconsColorFor
import jahirfiquitiva.libs.kext.extensions.getPrimaryTextColorFor
import jahirfiquitiva.libs.kext.extensions.getSecondaryTextColorFor
import jahirfiquitiva.libs.kext.extensions.hasContent
import jahirfiquitiva.libs.kext.extensions.primaryColor
import jahirfiquitiva.libs.kext.extensions.string
import jahirfiquitiva.libs.kext.extensions.tint
import jahirfiquitiva.libs.kext.ui.layouts.CustomTabLayout
import jahirfiquitiva.libs.kext.ui.widgets.CustomSearchView
import org.jetbrains.anko.doAsync

abstract class FramesActivity : BaseFramesActivity<FramesKonfigs>(), FavsDbManager {
    
    override val prefs: FramesKonfigs by lazy { FramesKonfigs(this) }
    
    private val toolbar: CustomToolbar? by bind(R.id.toolbar)
    private val pager: ViewPager? by bind(R.id.pager)
    private var pagerAdapter: FramesSectionsAdapter? = null
    private val tabs: CustomTabLayout? by bind(R.id.tabs)
    
    private var searchItem: MenuItem? = null
    private var searchView: CustomSearchView? = null
    
    private var errorSnackbar: Snackbar? = null
    
    override val favsViewModel: FavoritesViewModel by lazyViewModel()
    override val favsDB: FavoritesDatabase by lazy {
        Room.databaseBuilder(this, FavoritesDatabase::class.java, DATABASE_NAME)
            .fallbackToDestructiveMigration().build()
    }
    
    private var hasCollections = true
    private var lastSection = 0
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        hasCollections = boolean(R.bool.show_collections_tab)
        val correct = if (hasCollections) 1 else 0
        lastSection = savedInstanceState?.getInt("current", correct) ?: correct
        
        setContentView(R.layout.activity_main)
        
        setSupportActionBar(toolbar)
        
        initPagerAdapter()
        
        tabs?.setTabTextColors(
            getDisabledTextColorFor(primaryColor),
            if (boolean(R.bool.accent_in_tabs)) accentColor
            else getPrimaryTextColorFor(primaryColor))
        tabs?.setSelectedTabIndicatorColor(
            if (boolean(R.bool.accent_in_tabs)) accentColor
            else getPrimaryTextColorFor(primaryColor))
        if (boolean(R.bool.show_icons_in_tabs)) {
            tabs?.setTabsIconsColors(
                getInactiveIconsColorFor(primaryColor),
                if (boolean(R.bool.accent_in_tabs)) accentColor
                else getActiveIconsColorFor(primaryColor))
        }
        
        buildTabs()
        
        tabs?.addOnTabSelectedListener(
            object : TabLayout.ViewPagerOnTabSelectedListener(pager) {
                override fun onTabSelected(tab: TabLayout.Tab?) {
                    tab?.let { postDelayed(50) { navigateToSection(it.position) } }
                }
                
                override fun onTabReselected(tab: TabLayout.Tab?) = scrollToTop()
                override fun onTabUnselected(tab: TabLayout.Tab?) {}
            })
        pager?.addOnPageChangeListener(
            TabLayout.TabLayoutOnPageChangeListener(tabs))
        
        pager?.offscreenPageLimit = tabs?.tabCount ?: 2
        
        navigateToSection(lastSection, true)
        
        favsViewModel.observe(this) { notifyFavsToFrags(it) }
        doAsync { favsViewModel.loadData(favsDB.favoritesDao(), true) }
    }
    
    private fun initPagerAdapter() {
        pagerAdapter = FramesSectionsAdapter(
            supportFragmentManager, getLicenseChecker() != null, hasCollections)
        pager?.adapter = pagerAdapter
    }
    
    private fun buildTabs() {
        val showTexts = boolean(R.bool.show_texts_in_tabs)
        val showIcons = boolean(R.bool.show_icons_in_tabs)
        val reallyShowTexts = showTexts || (!showTexts && !showIcons)
        val expectedTabCount = if (hasCollections) 3 else 2
        
        tabs?.removeAllTabs()
        for (i in 0 until expectedTabCount) {
            
            val text = when (expectedTabCount - i) {
                3 -> R.string.collections
                2 -> R.string.all
                1 -> R.string.favorites
                else -> 0
            }
            
            var iconDrawable: Drawable? = null
            
            if (showIcons) {
                val icon = when (expectedTabCount - i) {
                    3 -> R.drawable.ic_collections
                    2 -> R.drawable.ic_all_wallpapers
                    1 -> R.drawable.ic_heart
                    else -> 0
                }
                
                if (icon != 0) {
                    iconDrawable = drawable(icon)?.tint(
                        if (i == lastSection)
                            if (boolean(R.bool.accent_in_tabs)) accentColor
                            else getActiveIconsColorFor(primaryColor)
                        else getInactiveIconsColorFor(primaryColor))
                }
            }
            
            if (iconDrawable != null || text != 0) {
                val tab = tabs?.newTab()
                if (reallyShowTexts) {
                    if (text != 0) tab?.setText(text)
                    if (showIcons && iconDrawable != null) {
                        tab?.icon = iconDrawable
                    }
                } else {
                    if (showIcons) {
                        if (iconDrawable != null) tab?.icon = iconDrawable
                    } else {
                        if (text != 0) tab?.setText(text)
                    }
                }
                if (i == lastSection) tab?.select()
                tab?.let { tabs?.addTab(it) }
            }
        }
    }
    
    private fun navigateToSection(position: Int, force: Boolean = false) {
        if (lastSection != position || force) {
            if (boolean(R.bool.show_icons_in_tabs)) {
                tabs?.setTabsIconsColors(
                    getInactiveIconsColorFor(primaryColor),
                    if (boolean(R.bool.accent_in_tabs)) accentColor
                    else getActiveIconsColorFor(primaryColor))
            }
            lastSection = position
            searchItem?.collapseActionView()
            updateSearchHint()
            pager?.setCurrentItem(lastSection, true)
        }
    }
    
    private fun updateSearchHint() {
        tabs?.let {
            var hint = (it.getTabAt(it.selectedTabPosition)?.text ?: "").toString()
            if (!hint.hasContent()) {
                val expectedTabCount = if (hasCollections) 3 else 2
                hint = when (expectedTabCount - it.selectedTabPosition) {
                    3 -> string(R.string.collections)
                    2 -> string(R.string.all)
                    1 -> string(R.string.favorites)
                    else -> ""
                }
            }
            searchView?.queryHint = getString(R.string.search_x, hint.toLowerCase())
        }
    }
    
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.frames_menu, menu)
        
        menu?.let {
            val donationItem = it.findItem(R.id.donate)
            donationItem?.isVisible = donationsEnabled
            
            searchItem = it.findItem(R.id.search)
            searchView = searchItem?.actionView as? CustomSearchView
            searchView?.onExpand = { toolbar?.enableScroll(false) }
            searchView?.onCollapse = {
                toolbar?.enableScroll(true)
                doSearch(closed = true)
            }
            searchView?.onQueryChanged = { doSearch(it) }
            searchView?.onQuerySubmit = { doSearch(it) }
            searchView?.bindToItem(searchItem)
            updateSearchHint()
            searchView?.tint(getPrimaryTextColorFor(primaryColor))
            it.tint(getActiveIconsColorFor(primaryColor))
        }
        
        toolbar?.tint(
            getPrimaryTextColorFor(primaryColor),
            getSecondaryTextColorFor(primaryColor),
            getActiveIconsColorFor(primaryColor))
        return super.onCreateOptionsMenu(menu)
    }
    
    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        item?.let {
            when (it.itemId) {
                R.id.refresh -> refreshContent()
                R.id.changelog -> showChanges()
                R.id.about -> startActivity(Intent(this, CreditsActivity::class.java))
                R.id.settings -> startActivityForResult(
                    Intent(this, SettingsActivity::class.java), 22)
                R.id.donate -> doDonation()
            }
        }
        return super.onOptionsItemSelected(item)
    }
    
    override fun onBackPressed() {
        val open = searchView?.isOpen == true
        if (!open) super.onBackPressed()
    }
    
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when {
            requestCode == 22 -> data?.let {
                val cleared = it.getBooleanExtra("clearedFavs", false)
                if (cleared) reloadFavorites()
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP -> {
                ((getCurrentFragment() as? BaseWallpapersFragment)
                    ?: (pagerAdapter?.get(lastSection) as? BaseWallpapersFragment))
                    ?.onActivityReenter(resultCode, data)
                    ?: super.onActivityResult(requestCode, resultCode, data)
            }
            else -> super.onActivityResult(requestCode, resultCode, data)
        }
    }
    
    override fun onActivityReenter(resultCode: Int, data: Intent?) {
        if (resultCode == 10) {
            ((getCurrentFragment() as? BaseWallpapersFragment)
                ?: (pagerAdapter?.get(lastSection) as? BaseWallpapersFragment))
                ?.onActivityReenter(resultCode, data)
                ?: super.onActivityReenter(resultCode, data)
        } else super.onActivityReenter(resultCode, data)
    }
    
    override fun onPause() {
        super.onPause()
        if (searchView?.isOpen == true) searchItem?.collapseActionView()
    }
    
    override fun onSaveInstanceState(outState: Bundle?) {
        outState?.putInt("current", lastSection)
        super.onSaveInstanceState(outState)
    }
    
    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        super.onRestoreInstanceState(savedInstanceState)
        invalidateOptionsMenu()
        hasCollections = boolean(R.bool.show_collections_tab)
        val correct = if (hasCollections) 1 else 0
        lastSection = savedInstanceState?.getInt("current", correct) ?: correct
        initPagerAdapter()
        pager?.setCurrentItem(lastSection, true)
    }
    
    private fun scrollToTop() {
        pagerAdapter?.get(lastSection)?.let {
            try {
                (it as? BaseFramesFragment<*, *>)?.scrollToTop()
            } catch (ignored: Exception) {
            }
        }
    }
    
    private val lock by lazy { Any() }
    private fun doSearch(filter: String = "", closed: Boolean = false) {
        pagerAdapter?.get(lastSection)?.let {
            try {
                (it as? BaseFramesFragment<*, *>)?.enableRefresh(!filter.hasContent())
                synchronized(lock) {
                    postDelayed(150) {
                        (it as? BaseFramesFragment<*, *>)?.applyFilter(filter, closed)
                    }
                }
            } catch (ignored: Exception) {
                ignored.printStackTrace()
            }
        }
    }
    
    private fun refreshContent() {
        val adapt = pager?.adapter
        adapt?.let {
            pagerAdapter?.get(lastSection)?.let {
                try {
                    (it as? BaseFramesFragment<*, *>)?.reloadData(
                        lastSection + if (hasCollections) 0 else 1)
                } catch (ignored: Exception) {
                }
            }
        }
    }
    
    override fun notifyFavsToFrags(favs: ArrayList<Wallpaper>) {
        pagerAdapter?.forEach { _, it ->
            (it as? BaseDatabaseFragment<*, *>)?.doOnFavoritesChange(favs)
        }
    }
    
    override fun showSnackbar(text: String) {
        errorSnackbar?.dismiss()
        errorSnackbar = null
        errorSnackbar = contentView?.buildSnackbar(text)
        errorSnackbar?.view?.findViewById<TextView>(R.id.snackbar_text)?.setTextColor(Color.WHITE)
        errorSnackbar?.show()
    }
}
