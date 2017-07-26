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
package jahirfiquitiva.libs.frames.activities

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.view.ViewPager
import android.support.v7.widget.SearchView
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.view.inputmethod.EditorInfo
import ca.allanwang.kau.utils.statusBarColor
import ca.allanwang.kau.utils.statusBarLight
import jahirfiquitiva.libs.frames.R
import jahirfiquitiva.libs.frames.adapters.FragmentsAdapter
import jahirfiquitiva.libs.frames.fragments.CollectionsFragment
import jahirfiquitiva.libs.frames.fragments.FavoritesFragment
import jahirfiquitiva.libs.frames.fragments.WallpapersFragment
import jahirfiquitiva.libs.frames.fragments.base.BaseFramesFragment
import jahirfiquitiva.libs.kauextensions.extensions.*


abstract class FramesActivity:BaseFramesActivity() {

    private lateinit var toolbar:Toolbar
    private lateinit var pager:ViewPager
    private lateinit var tabs:TabLayout
    private var searchView:SearchView? = null

    private var lastSection = 1

    override fun onCreate(savedInstanceState:Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        statusBarColor = primaryDarkColor
        statusBarLight = primaryDarkColor.isColorLight(0.6F)
        pager = findViewById<ViewPager>(R.id.pager)

        pager.adapter = FragmentsAdapter(supportFragmentManager, CollectionsFragment(),
                                         WallpapersFragment(), FavoritesFragment())
        tabs = findViewById<TabLayout>(R.id.tabs)
        tabs.setTabTextColors(getDisabledTextColorFor(primaryColor, 0.6F),
                              getPrimaryTextColorFor(primaryColor, 0.6F))
        tabs.setSelectedTabIndicatorColor(getPrimaryTextColorFor(primaryColor, 0.6F))
        tabs.addTab(tabs.newTab().setText(R.string.collections))
        tabs.addTab(tabs.newTab().setText(R.string.all))
        tabs.addTab(tabs.newTab().setText(R.string.favorites))
        tabs.addOnTabSelectedListener(object:TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab:TabLayout.Tab?) {
                return
            }

            override fun onTabUnselected(tab:TabLayout.Tab?) {
                // Do nothing
            }

            override fun onTabSelected(tab:TabLayout.Tab?) {
                tab?.let {
                    lastSection = it.position
                    pager.setCurrentItem(it.position, true)
                    searchView?.let {
                        val hint = tabs.getTabAt(tabs.selectedTabPosition)?.text.toString()
                        it.queryHint = getString(R.string.search_x, hint.toLowerCase())
                    }
                    val isClosed = searchView?.isIconified ?: true
                    if (!isClosed) {
                        doSearch()
                        invalidateOptionsMenu()
                    }
                }
            }
        })
        pager.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabs))
        pager.offscreenPageLimit = tabs.tabCount
        pager.setCurrentItem(1, true)
    }

    override fun onCreateOptionsMenu(menu:Menu?):Boolean {
        menuInflater.inflate(R.menu.frames_menu, menu)

        menu?.let {
            val searchItem = it.findItem(R.id.search)
            searchView = searchItem.actionView as SearchView
            searchView?.let {
                with(it) {
                    val hint = tabs.getTabAt(tabs.selectedTabPosition)?.text.toString()
                    queryHint = getString(R.string.search_x, hint.toLowerCase())
                    setOnQueryTextListener(object:SearchView.OnQueryTextListener {
                        override fun onQueryTextSubmit(query:String?):Boolean {
                            query?.let {
                                doSearch(it.trim())
                            }
                            return false
                        }

                        override fun onQueryTextChange(newText:String?):Boolean {
                            newText?.let {
                                doSearch(it.trim())
                            }
                            return false
                        }
                    })
                    imeOptions = EditorInfo.IME_ACTION_DONE
                }
            }
            searchItem.setOnActionExpandListener(object:MenuItem.OnActionExpandListener {
                override fun onMenuItemActionExpand(item:MenuItem?):Boolean = true

                override fun onMenuItemActionCollapse(item:MenuItem?):Boolean {
                    printInfo("Search collapsed")
                    searchView?.setQuery("", true)
                    doSearch()
                    return true
                }
            })
        }

        toolbar.tint(getPrimaryTextColorFor(primaryColor, 0.6F),
                     getSecondaryTextColorFor(primaryColor, 0.6F),
                     getActiveIconsColorFor(primaryColor, 0.6F))
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item:MenuItem?):Boolean {
        item?.let {
            if (it.itemId == R.id.about) {
                startActivity(Intent(this, CreditsActivity::class.java))
                return true
            }
            // TODO: Manage other items
        }
        return super.onOptionsItemSelected(item)
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

    override fun onDestroy() {
        super.onDestroy()
    }

    private fun doSearch(filter:String = "") {
        val adapter = pager.adapter
        if (adapter is FragmentsAdapter) {
            val frag = adapter.getItem(lastSection)
            frag?.let {
                if (it is BaseFramesFragment<*, *>) {
                    try {
                        it.applyFilter(filter)
                    } catch (ignored:Exception) {
                    }
                }
            }
        }
    }

}