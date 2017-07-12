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
import ca.allanwang.kau.utils.tint
import jahirfiquitiva.libs.frames.R
import jahirfiquitiva.libs.frames.adapters.FragmentsAdapter
import jahirfiquitiva.libs.frames.fragments.CollectionsFragment
import jahirfiquitiva.libs.frames.utils.destroyFavoritesDatabase
import jahirfiquitiva.libs.frames.utils.initFavoritesDatabase
import jahirfiquitiva.libs.kauextensions.extensions.disabledTextColor
import jahirfiquitiva.libs.kauextensions.extensions.getPrimaryTextColorFor
import jahirfiquitiva.libs.kauextensions.extensions.isColorLight
import jahirfiquitiva.libs.kauextensions.extensions.primaryColor
import jahirfiquitiva.libs.kauextensions.extensions.primaryDarkColor

abstract class FramesActivity:BaseFramesActivity() {

    private val pager:ViewPager by lazy(LazyThreadSafetyMode.NONE) {
        findViewById<ViewPager>(R.id.pager).also {
            // TODO: Add Wallpapers and Favorites fragments
            it.adapter = FragmentsAdapter(supportFragmentManager, CollectionsFragment())
            it.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabs))
            it.offscreenPageLimit = tabs.tabCount
        }
    }

    private val tabs:TabLayout by lazy(LazyThreadSafetyMode.NONE) {
        findViewById<TabLayout>(R.id.tabs).also {
            // TODO: Change disabled text color
            it.setTabTextColors(getPrimaryTextColorFor(primaryColor), disabledTextColor)
            it.setSelectedTabIndicatorColor(getPrimaryTextColorFor(primaryColor))
            it.addTab(it.newTab().setText(R.string.collections))
            it.addTab(it.newTab().setText(R.string.all))
            it.addTab(it.newTab().setText(R.string.favorites))
            it.addOnTabSelectedListener(object:TabLayout.OnTabSelectedListener {
                override fun onTabReselected(tab:TabLayout.Tab?) {
                    // Do nothing
                }

                override fun onTabUnselected(tab:TabLayout.Tab?) {
                    // Do nothing
                }

                override fun onTabSelected(tab:TabLayout.Tab?) {
                    pager.setCurrentItem(tab?.position ?: 0, true)
                }
            })
        }
    }

    override fun onCreate(savedInstanceState:Bundle?) {
        super.onCreate(savedInstanceState)
        initFavoritesDatabase()
        setContentView(R.layout.activity_main)
        val toolbar:Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.tint(getPrimaryTextColorFor(primaryColor), true)
        statusBarColor = primaryDarkColor
        statusBarLight = primaryDarkColor.isColorLight()
    }

    override fun onDestroy() {
        super.onDestroy()
        destroyFavoritesDatabase()
    }
}