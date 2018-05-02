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

import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewPager
import android.view.Menu
import android.view.MenuItem
import ca.allanwang.kau.utils.boolean
import ca.allanwang.kau.utils.postDelayed
import ca.allanwang.kau.utils.tint
import ca.allanwang.kau.xml.showChangelog
import jahirfiquitiva.libs.frames.R
import jahirfiquitiva.libs.frames.data.models.Wallpaper
import jahirfiquitiva.libs.frames.helpers.utils.FL
import jahirfiquitiva.libs.frames.ui.activities.base.BaseFramesActivity
import jahirfiquitiva.libs.frames.ui.fragments.CollectionsFragment
import jahirfiquitiva.libs.frames.ui.fragments.FavoritesFragment
import jahirfiquitiva.libs.frames.ui.fragments.WallpapersFragment
import jahirfiquitiva.libs.frames.ui.fragments.base.BaseFramesFragment
import jahirfiquitiva.libs.frames.ui.widgets.CustomToolbar
import jahirfiquitiva.libs.kauextensions.extensions.bind
import jahirfiquitiva.libs.kauextensions.extensions.getActiveIconsColorFor
import jahirfiquitiva.libs.kauextensions.extensions.getDisabledTextColorFor
import jahirfiquitiva.libs.kauextensions.extensions.getInactiveIconsColorFor
import jahirfiquitiva.libs.kauextensions.extensions.getPrimaryTextColorFor
import jahirfiquitiva.libs.kauextensions.extensions.getSecondaryTextColorFor
import jahirfiquitiva.libs.kauextensions.extensions.hasContent
import jahirfiquitiva.libs.kauextensions.extensions.primaryColor
import jahirfiquitiva.libs.kauextensions.extensions.secondaryTextColor
import jahirfiquitiva.libs.kauextensions.extensions.tint
import jahirfiquitiva.libs.kauextensions.ui.fragments.adapters.FragmentsPagerAdapter
import jahirfiquitiva.libs.kauextensions.ui.layouts.CustomTabLayout
import jahirfiquitiva.libs.kauextensions.ui.widgets.CustomSearchView

abstract class FramesActivity : BaseFramesActivity() {
    
    private val toolbar: CustomToolbar? by bind(R.id.toolbar)
    private val pager: ViewPager? by bind(R.id.pager)
    private val tabs: CustomTabLayout? by bind(R.id.tabs)
    
    private var searchItem: MenuItem? = null
    private var searchView: CustomSearchView? = null
    
    private var hasCollections = false
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
                getDisabledTextColorFor(primaryColor, 0.6F),
                getPrimaryTextColorFor(primaryColor, 0.6F))
        tabs?.setSelectedTabIndicatorColor(getPrimaryTextColorFor(primaryColor, 0.6F))
        
        buildTabs()
        
        tabs?.addOnTabSelectedListener(
                object : TabLayout.ViewPagerOnTabSelectedListener(pager) {
                    override fun onTabSelected(tab: TabLayout.Tab?) {
                        tab?.let { navigateToSection(it.position) }
                    }
                    
                    override fun onTabReselected(tab: TabLayout.Tab?) = scrollToTop()
                    override fun onTabUnselected(tab: TabLayout.Tab?) {}
                })
        pager?.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabs))
        
        pager?.offscreenPageLimit = tabs?.tabCount ?: 2
        
        navigateToSection(lastSection, true)
    }
    
    private fun initPagerAdapter() {
        pager?.adapter = if (hasCollections) {
            FragmentsPagerAdapter(
                    supportFragmentManager,
                    CollectionsFragment.create(getLicenseChecker() != null),
                    WallpapersFragment.create(getLicenseChecker() != null),
                    FavoritesFragment.create(getLicenseChecker() != null))
        } else {
            FragmentsPagerAdapter(
                    supportFragmentManager,
                    WallpapersFragment.create(getLicenseChecker() != null),
                    FavoritesFragment.create(getLicenseChecker() != null))
        }
    }
    
    private fun buildTabs() {
        val showTexts = boolean(R.bool.show_texts_in_tabs)
        val showIcons = boolean(R.bool.show_icons_in_tabs)
        val reallyShowTexts = showTexts || (!showTexts && !showIcons)
        
        tabs?.removeAllTabs()
        for (i in 0 until 3) {
            if (i == 0 && !hasCollections) continue
            
            val icon = when (i) {
                0 -> R.drawable.ic_collections
                1 -> R.drawable.ic_all_wallpapers
                2 -> R.drawable.ic_heart
                else -> 0
            }
            
            val text = when (i) {
                0 -> R.string.collections
                1 -> R.string.all
                2 -> R.string.favorites
                else -> 0
            }
            
            var iconDrawable: Drawable? = null
            
            if (showIcons && icon != 0)
                iconDrawable = ContextCompat.getDrawable(this, icon)?.tint(
                        if (i != (if (hasCollections) 0 else 1))
                            getInactiveIconsColorFor(primaryColor, 0.6F)
                        else getActiveIconsColorFor(primaryColor, 0.6F))
            
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
            tab?.let { tabs?.addTab(it) }
        }
    }
    
    private fun navigateToSection(position: Int, force: Boolean = false) {
        if (lastSection != position || force) {
            lastSection = position
            if (boolean(R.bool.show_icons_in_tabs)) {
                tabs?.setTabsIconsColors(
                        getInactiveIconsColorFor(primaryColor, 0.6F),
                        getActiveIconsColorFor(primaryColor, 0.6F))
            }
            searchItem?.collapseActionView()
            searchView?.let { search ->
                tabs?.let {
                    val hint = it.getTabAt(it.selectedTabPosition)?.text.toString()
                    search.queryHint =
                            getString(R.string.search_x, hint.toLowerCase())
                }
            }
            pager?.setCurrentItem(lastSection, true)
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
                doSearch()
            }
            searchView?.onQueryChanged = { doSearch(it) }
            searchView?.onQuerySubmit = { doSearch(it) }
            searchView?.bindToItem(searchItem)
            
            tabs?.let {
                val hint = it.getTabAt(it.selectedTabPosition)?.text.toString()
                searchView?.queryHint = getString(R.string.search_x, hint.toLowerCase())
            }
            
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
        item?.let {
            when (it.itemId) {
                R.id.refresh -> refreshContent()
                R.id.changelog -> showChangelog(R.xml.changelog, secondaryTextColor)
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
    
    @Suppress("UNCHECKED_CAST")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 22) {
            data?.let {
                val cleared = it.getBooleanExtra("clearedFavs", false)
                if (cleared) reloadFavorites()
            }
        } else if (requestCode == 11) {
            try {
                data?.let {
                    try {
                        val nFavs = data.getSerializableExtra("nFavs") as? ArrayList<Wallpaper>
                        nFavs?.let { if (it.isNotEmpty()) setNewFavorites(it) }
                    } catch (e: Exception) {
                        FL.e { e.message }
                    }
                }
            } catch (e: Exception) {
                FL.e { e.message }
            }
        }
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
        pager?.adapter?.let {
            (it as? FragmentsPagerAdapter)?.getItem(lastSection)?.let {
                try {
                    (it as? BaseFramesFragment<*, *>)?.scrollToTop()
                } catch (ignored: Exception) {
                }
            }
        }
    }
    
    private val lock by lazy { Any() }
    private fun doSearch(filter: String = "") {
        pager?.adapter?.let {
            (it as? FragmentsPagerAdapter)?.getItem(lastSection)?.let {
                try {
                    (it as? BaseFramesFragment<*, *>)?.enableRefresh(!filter.hasContent())
                    synchronized(
                            lock, {
                        postDelayed(200) {
                            (it as? BaseFramesFragment<*, *>)?.applyFilter(filter)
                        }
                    })
                } catch (ignored: Exception) {
                    ignored.printStackTrace()
                }
            }
        }
    }
    
    private fun refreshContent() {
        val adapt = pager?.adapter
        adapt?.let {
            (it as? FragmentsPagerAdapter)?.getItem(lastSection)?.let {
                try {
                    (it as? BaseFramesFragment<*, *>)?.reloadData(
                            lastSection + if (hasCollections) 0 else 1)
                } catch (ignored: Exception) {
                }
            }
        }
    }
    
    private fun reloadFavorites() {
        pager?.adapter?.let {
            (it as? FragmentsPagerAdapter)?.getItem(lastSection)?.let {
                try {
                    (it as? BaseFramesFragment<*, *>)?.reloadData(2)
                } catch (ignored: Exception) {
                }
            }
        }
    }
    
    private fun setNewFavorites(list: ArrayList<Wallpaper>) {
        pager?.adapter?.let {
            (it as? FragmentsPagerAdapter)?.getItem(if (hasCollections) 2 else 1)?.let {
                try {
                    (it as? BaseFramesFragment<*, *>)?.let {
                        with(it) {
                            getDatabase()?.let { favoritesModel.forceUpdateFavorites(it, list) }
                        }
                    }
                } catch (ignored: Exception) {
                }
            }
        }
    }
}