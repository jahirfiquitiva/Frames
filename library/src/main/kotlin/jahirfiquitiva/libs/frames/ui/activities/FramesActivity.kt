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
package jahirfiquitiva.libs.frames.ui.activities

import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewPager
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import ca.allanwang.kau.utils.postDelayed
import ca.allanwang.kau.utils.tint
import jahirfiquitiva.libs.frames.R
import jahirfiquitiva.libs.frames.data.models.Wallpaper
import jahirfiquitiva.libs.frames.ui.activities.base.BaseFramesActivity
import jahirfiquitiva.libs.frames.ui.fragments.CollectionsFragment
import jahirfiquitiva.libs.frames.ui.fragments.FavoritesFragment
import jahirfiquitiva.libs.frames.ui.fragments.WallpapersFragment
import jahirfiquitiva.libs.frames.ui.fragments.adapters.FragmentsAdapter
import jahirfiquitiva.libs.frames.ui.fragments.base.BaseFramesFragment
import jahirfiquitiva.libs.frames.ui.widgets.CustomTabLayout
import jahirfiquitiva.libs.frames.ui.widgets.SearchView
import jahirfiquitiva.libs.frames.ui.widgets.bindSearchView
import jahirfiquitiva.libs.kauextensions.extensions.accentColor
import jahirfiquitiva.libs.kauextensions.extensions.bind
import jahirfiquitiva.libs.kauextensions.extensions.getActiveIconsColorFor
import jahirfiquitiva.libs.kauextensions.extensions.getBoolean
import jahirfiquitiva.libs.kauextensions.extensions.getDisabledTextColorFor
import jahirfiquitiva.libs.kauextensions.extensions.getInactiveIconsColorFor
import jahirfiquitiva.libs.kauextensions.extensions.getPrimaryTextColorFor
import jahirfiquitiva.libs.kauextensions.extensions.getSecondaryTextColorFor
import jahirfiquitiva.libs.kauextensions.extensions.primaryColor
import jahirfiquitiva.libs.kauextensions.extensions.tint

abstract class FramesActivity:BaseFramesActivity() {
    
    private val toolbar:Toolbar by bind(R.id.toolbar)
    private val pager:ViewPager by bind(R.id.pager)
    private val tabs:CustomTabLayout by bind(R.id.tabs)
    
    private var searchView:SearchView? = null
    private var lastSection = 1
    
    override fun onCreate(savedInstanceState:Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        setSupportActionBar(toolbar)
        
        pager.adapter = FragmentsAdapter(supportFragmentManager, CollectionsFragment(),
                                         WallpapersFragment(), FavoritesFragment())
        
        val useAccentColor = getBoolean(R.bool.enable_accent_color_in_tabs)
        tabs.setTabTextColors(getDisabledTextColorFor(primaryColor, 0.6F),
                              if (useAccentColor) accentColor else
                                  getPrimaryTextColorFor(primaryColor, 0.6F))
        tabs.setSelectedTabIndicatorColor(
                if (useAccentColor) accentColor else getPrimaryTextColorFor(primaryColor, 0.6F))
        
        val showTexts = getBoolean(R.bool.show_texts_in_tabs)
        val showIcons = getBoolean(R.bool.show_icons_in_tabs)
        val reallyShowTexts = showTexts || (!showTexts && !showIcons)
        
        tabs.removeAllTabs()
        for (i in 0 until 3) {
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
            
            var iconDrawable:Drawable? = null
            
            if (showIcons && icon != 0)
                iconDrawable = ContextCompat.getDrawable(this, icon)
                        .tint(if (i != 1) getInactiveIconsColorFor(primaryColor, 0.6F)
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
        
        tabs.addOnTabSelectedListener(object:TabLayout.ViewPagerOnTabSelectedListener(pager) {
            override fun onTabSelected(tab:TabLayout.Tab?) {
                tab?.let {
                    if (lastSection == it.position) return
                    lastSection = it.position
                    if (showIcons) {
                        tabs.setTabsIconsColors(getInactiveIconsColorFor(primaryColor, 0.6F),
                                                if (useAccentColor) accentColor else
                                                    getActiveIconsColorFor(primaryColor, 0.6F))
                    }
                    searchView?.let {
                        it.revealClose()
                        val hint = tabs.getTabAt(tabs.selectedTabPosition)?.text.toString()
                        it.hintText = getString(R.string.search_x, hint.toLowerCase())
                    }
                    invalidateOptionsMenu()
                    pager.setCurrentItem(lastSection, true)
                }
            }
            
            override fun onTabReselected(tab:TabLayout.Tab?) = scrollToTop()
            override fun onTabUnselected(tab:TabLayout.Tab?) {}
        })
        pager.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabs))
        
        pager.offscreenPageLimit = tabs.tabCount
        pager.setCurrentItem(1, true)
    }
    
    override fun onCreateOptionsMenu(menu:Menu?):Boolean {
        menuInflater.inflate(R.menu.frames_menu, menu)
        
        menu?.let {
            val donationItem = it.findItem(R.id.donate)
            donationItem?.isVisible = donationsEnabled
            
            searchView = bindSearchView(it, R.id.search)
            searchView?.listener = object:SearchView.SearchListener {
                override fun onQueryChanged(query:String) {
                    doSearch(query)
                }
                
                override fun onQuerySubmit(query:String) {
                    doSearch(query)
                }
                
                override fun onSearchOpened(searchView:SearchView) {
                    // Do nothing
                }
                
                override fun onSearchClosed(searchView:SearchView) {
                    doSearch()
                }
            }
            val hint = tabs.getTabAt(tabs.selectedTabPosition)?.text.toString()
            searchView?.hintText = getString(R.string.search_x, hint.toLowerCase())
        }
        
        toolbar.tint(getPrimaryTextColorFor(primaryColor, 0.6F),
                     getSecondaryTextColorFor(primaryColor, 0.6F),
                     getActiveIconsColorFor(primaryColor, 0.6F))
        return super.onCreateOptionsMenu(menu)
    }
    
    override fun onOptionsItemSelected(item:MenuItem?):Boolean {
        item?.let {
            val id = it.itemId
            when (id) {
                R.id.refresh -> refreshContent()
                R.id.about -> startActivity(Intent(this, CreditsActivity::class.java))
                R.id.settings -> startActivityForResult(Intent(this, SettingsActivity::class.java),
                                                        22)
                R.id.donate -> doDonation()
            }
        }
        return super.onOptionsItemSelected(item)
    }
    
    override fun onBackPressed() {
        val open = searchView?.onBackPressed() ?: false
        if (!open) super.onBackPressed()
    }
    
    @Suppress("UNCHECKED_CAST")
    override fun onActivityResult(requestCode:Int, resultCode:Int, data:Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 22) {
            data?.let {
                val cleared = it.getBooleanExtra("clearedFavs", false)
                if (cleared) {
                    reloadFavorites()
                }
            }
        } else if (requestCode == 11) {
            try {
                data?.let {
                    try {
                        val nFavs = data.getSerializableExtra("nFavs") as ArrayList<Wallpaper>
                        if (nFavs.isNotEmpty()) setNewFavorites(nFavs)
                    } catch (e:Exception) {
                        e.printStackTrace()
                    }
                }
            } catch (e:Exception) {
                e.printStackTrace()
            }
        }
    }
    
    override fun onSaveInstanceState(outState:Bundle?) {
        outState?.putInt("current", lastSection)
        super.onSaveInstanceState(outState)
    }
    
    override fun onRestoreInstanceState(savedInstanceState:Bundle?) {
        super.onRestoreInstanceState(savedInstanceState)
        lastSection = savedInstanceState?.getInt("current", 1) ?: 1
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
                    } catch (ignored:Exception) {
                    }
                }
            }
        }
    }
    
    private val LOCK = Any()
    private fun doSearch(filter:String = "") {
        val adapter = pager.adapter
        if (adapter is FragmentsAdapter) {
            val frag = adapter.getItem(lastSection)
            frag?.let {
                if (it is BaseFramesFragment<*, *>) {
                    try {
                        synchronized(LOCK, {
                            postDelayed(200, { it.applyFilter(filter) })
                        })
                    } catch (ignored:Exception) {
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
                        it.reloadData(lastSection)
                    } catch (ignored:Exception) {
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
                    } catch (ignored:Exception) {
                    }
                }
            }
        }
    }
    
    private fun setNewFavorites(list:ArrayList<Wallpaper>) {
        val adapter = pager.adapter
        if (adapter is FragmentsAdapter) {
            val frag = adapter.getItem(2)
            frag?.let {
                if (it is BaseFramesFragment<*, *>) {
                    try {
                        it.favoritesModel?.stopTask(true)
                        it.favoritesModel?.forceUpdateFavorites(list)
                    } catch (ignored:Exception) {
                    }
                }
            }
        }
    }
}