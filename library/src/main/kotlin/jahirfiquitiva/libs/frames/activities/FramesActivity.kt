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

import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.view.ViewPager
import android.support.v7.widget.Toolbar
import ca.allanwang.kau.utils.statusBarColor
import ca.allanwang.kau.utils.statusBarLight
import jahirfiquitiva.libs.frames.R
import jahirfiquitiva.libs.frames.adapters.FragmentsAdapter
import jahirfiquitiva.libs.frames.fragments.CollectionsFragment
import jahirfiquitiva.libs.frames.fragments.FavoritesFragment
import jahirfiquitiva.libs.frames.fragments.WallpapersFragment
import jahirfiquitiva.libs.kauextensions.extensions.*

abstract class FramesActivity:BaseFramesActivity() {

    private lateinit var pager:ViewPager
    private lateinit var tabs:TabLayout

    override fun onCreate(savedInstanceState:Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar:Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.tint(getPrimaryTextColorFor(primaryColor), getSecondaryTextColorFor(primaryColor),
                     getActiveIconsColorFor(primaryColor))
        statusBarColor = primaryDarkColor
        statusBarLight = primaryDarkColor.isColorLight()
        pager = findViewById<ViewPager>(R.id.pager)
		
        pager.adapter = FragmentsAdapter(supportFragmentManager, CollectionsFragment(),
                                         WallpapersFragment(), FavoritesFragment())
        tabs = findViewById<TabLayout>(R.id.tabs)
        tabs.setTabTextColors(getDisabledTextColorFor(primaryColor),
                              getPrimaryTextColorFor(primaryColor))
        tabs.setSelectedTabIndicatorColor(getPrimaryTextColorFor(primaryColor))
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
                pager.setCurrentItem(tab?.position ?: 0, true)
                /*
                if (pager.adapter is FragmentsAdapter) {
                    (pager.adapter as FragmentsAdapter).getItem(tab?.position ?: 0)?.let {
                        if (it is FavoritesFragment) {
                            it.loadDataFromViewModel()
                        } else if (it is WallpapersFragment) {
                            it.loadDataFromViewModel()
                        }
                    }
                }
                */
            }
        })
        pager.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabs))
        pager.offscreenPageLimit = tabs.tabCount
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}