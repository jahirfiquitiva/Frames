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
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewPager
import android.view.Menu
import android.view.MenuItem
import ca.allanwang.kau.utils.postDelayed
import ca.allanwang.kau.utils.tint
import jahirfiquitiva.libs.frames.R
import jahirfiquitiva.libs.frames.data.models.Wallpaper
import jahirfiquitiva.libs.frames.helpers.utils.FL
import jahirfiquitiva.libs.frames.ui.activities.base.BaseFramesActivity
import jahirfiquitiva.libs.frames.ui.fragments.CollectionsFragment
import jahirfiquitiva.libs.frames.ui.fragments.FavoritesFragment
import jahirfiquitiva.libs.frames.ui.fragments.WallpapersFragment
import jahirfiquitiva.libs.frames.ui.fragments.base.BaseFramesFragment
import jahirfiquitiva.libs.frames.ui.widgets.CustomToolbar
import jahirfiquitiva.libs.kauextensions.extensions.accentColor
import jahirfiquitiva.libs.kauextensions.extensions.bind
import jahirfiquitiva.libs.kauextensions.extensions.getActiveIconsColorFor
import jahirfiquitiva.libs.kauextensions.extensions.getBoolean
import jahirfiquitiva.libs.kauextensions.extensions.getDisabledTextColorFor
import jahirfiquitiva.libs.kauextensions.extensions.getInactiveIconsColorFor
import jahirfiquitiva.libs.kauextensions.extensions.getPrimaryTextColorFor
import jahirfiquitiva.libs.kauextensions.extensions.getSecondaryTextColorFor
import jahirfiquitiva.libs.kauextensions.extensions.hasContent
import jahirfiquitiva.libs.kauextensions.extensions.primaryColor
import jahirfiquitiva.libs.kauextensions.extensions.tint
import jahirfiquitiva.libs.kauextensions.ui.fragments.adapters.FragmentsAdapter
import jahirfiquitiva.libs.kauextensions.ui.widgets.CustomSearchView
import jahirfiquitiva.libs.kauextensions.ui.widgets.CustomTabLayout

abstract class FramesActivity : BaseFramesActivity() {
    
    private val toolbar: CustomToolbar by bind(R.id.toolbar)
    private val pager: ViewPager by bind(R.id.pager)
    private val tabs: CustomTabLayout by bind(R.id.tabs)
    
    private var searchView: CustomSearchView? = null
    
    private var hasCollections = false
    private var lastSection = 0
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        hasCollections = getBoolean(R.bool.show_collections_tab)
        if (hasCollections) lastSection = 1
        
        setContentView(R.layout.activity_main)
        
        setSupportActionBar(toolbar)
        
        pager.adapter = if (hasCollections) {
            FragmentsAdapter(
                    supportFragmentManager,
                    CollectionsFragment.create(getLicenseChecker() != null),
                    WallpapersFragment.create(getLicenseChecker() != null),
                    FavoritesFragment.create(getLicenseChecker() != null))
        } else {
            FragmentsAdapter(
                    supportFragmentManager,
                    WallpapersFragment.create(getLicenseChecker() != null),
                    FavoritesFragment.create(getLicenseChecker() != null))
        }
        
        val useAccentColor = getBoolean(R.bool.enable_accent_color_in_tabs)
        tabs.setTabTextColors(
                getDisabledTextColorFor(primaryColor, 0.6F),
                if (useAccentColor) accentColor else
                    getPrimaryTextColorFor(primaryColor, 0.6F))
        tabs.setSelectedTabIndicatorColor(
                if (useAccentColor) accentColor else getPrimaryTextColorFor(primaryColor, 0.6F))
        
        buildTabs()
        
        tabs.addOnTabSelectedListener(
                object : TabLayout.ViewPagerOnTabSelectedListener(pager) {
                    override fun onTabSelected(tab: TabLayout.Tab?) {
                        tab?.let {
                            if (lastSection == it.position) return
                            lastSection = it.position
                            if (getBoolean(R.bool.show_icons_in_tabs)) {
                                tabs.setTabsIconsColors(
                                        getInactiveIconsColorFor(primaryColor, 0.6F),
                                        if (useAccentColor) accentColor else
                                            getActiveIconsColorFor(primaryColor, 0.6F))
                            }
                            searchView?.let {
                                it.onActionViewCollapsed()
                                val hint = tabs.getTabAt(tabs.selectedTabPosition)?.text.toString()
                                it.queryHint = getString(R.string.search_x, hint.toLowerCase())
                            }
                            invalidateOptionsMenu()
                            pager.setCurrentItem(lastSection, true)
                        }
                    }
                    
                    override fun onTabReselected(tab: TabLayout.Tab?) = scrollToTop()
                    override fun onTabUnselected(tab: TabLayout.Tab?) {}
                })
        pager.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabs))
        
        pager.offscreenPageLimit = tabs.tabCount
        pager.setCurrentItem(lastSection, true)
    }
    
    private fun buildTabs() {
        val showTexts = getBoolean(R.bool.show_texts_in_tabs)
        val showIcons = getBoolean(R.bool.show_icons_in_tabs)
        val reallyShowTexts = showTexts || (!showTexts && !showIcons)
        
        tabs.removeAllTabs()
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
            
            val tab = tabs.newTab()
            if (reallyShowTexts) {
                if (text != 0) tab.setText(text)
                if (showIcons && iconDrawable != null) {
                    tab.icon = iconDrawable
                }
            } else {
                if (showIcons) {
                    if (iconDrawable != null) tab.icon = iconDrawable
                } else {
                    if (text != 0) tab.setText(text)
                }
            }
            tabs.addTab(tab)
        }
    }
    
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.frames_menu, menu)
        
        menu?.let {
            val donationItem = it.findItem(R.id.donate)
            donationItem?.isVisible = donationsEnabled
            
            val searchItem = it.findItem(R.id.search)
            searchView = searchItem.actionView as? CustomSearchView
            searchView?.onExpand = { toolbar.enableScroll(false) }
            searchView?.onCollapse = {
                toolbar.enableScroll(true)
                doSearch()
            }
            searchView?.onQueryChanged = { doSearch(it) }
            searchView?.onQuerySubmit = { doSearch(it) }
            searchView?.bindToItem(searchItem)
            
            val hint = tabs.getTabAt(tabs.selectedTabPosition)?.text.toString()
            searchView?.queryHint = getString(R.string.search_x, hint.toLowerCase())
            
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
        item?.let {
            when (it.itemId) {
                R.id.refresh -> refreshContent()
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
    
    @SuppressLint("MissingSuperCall")
    override fun onResume() {
        super.onResume()
        invalidateOptionsMenu()
    }
    
    override fun onPause() {
        super.onPause()
        searchView?.onActionViewCollapsed()
    }
    
    override fun onSaveInstanceState(outState: Bundle?) {
        outState?.putInt("current", lastSection)
        super.onSaveInstanceState(outState)
    }
    
    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        super.onRestoreInstanceState(savedInstanceState)
        lastSection = savedInstanceState?.getInt("current", 0) ?: 0
        pager.setCurrentItem(lastSection, true)
    }
    
    private fun scrollToTop() {
        val adapter = pager.adapter
        if (adapter is FragmentsAdapter) {
            val frag = adapter.getItem(lastSection)
            frag?.let {
                if (it is BaseFramesFragment<*, *>) {
                    try {
                        it.scrollToTop()
                    } catch (ignored: Exception) {
                    }
                }
            }
        }
    }
    
    private val lock = Any()
    private fun doSearch(filter: String = "") {
        val adapter = pager.adapter
        if (adapter is FragmentsAdapter) {
            val frag = adapter.getItem(lastSection)
            frag?.let {
                if (it is BaseFramesFragment<*, *>) {
                    try {
                        it.enableRefresh(!filter.hasContent())
                        synchronized(
                                lock, {
                            postDelayed(200, { it.applyFilter(filter) })
                        })
                    } catch (ignored: Exception) {
                    }
                }
            }
        }
    }
    
    private fun refreshContent() {
        val adapter = pager.adapter
        if (adapter is FragmentsAdapter) {
            val frag = adapter.getItem(lastSection)
            frag?.let {
                if (it is BaseFramesFragment<*, *>) {
                    try {
                        it.reloadData(lastSection + if (hasCollections) 0 else 1)
                    } catch (ignored: Exception) {
                    }
                }
            }
        }
    }
    
    private fun reloadFavorites() {
        val adapter = pager.adapter
        if (adapter is FragmentsAdapter) {
            val frag = adapter.getItem(lastSection)
            frag?.let {
                if (it is BaseFramesFragment<*, *>) {
                    try {
                        it.reloadData(2)
                    } catch (ignored: Exception) {
                    }
                }
            }
        }
    }
    
    private fun setNewFavorites(list: ArrayList<Wallpaper>) {
        val adapter = pager.adapter
        if (adapter is FragmentsAdapter) {
            val frag = adapter.getItem(if (hasCollections) 2 else 1)
            frag?.let {
                if (it is BaseFramesFragment<*, *>) {
                    try {
                        with(it) {
                            getDatabase()?.let { favoritesModel?.forceUpdateFavorites(it, list) }
                        }
                    } catch (ignored: Exception) {
                    }
                }
            }
        }
    }
}