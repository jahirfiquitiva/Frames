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
import android.content.Context
import android.os.Bundle
import android.support.annotation.StringRes
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.GridLayoutManager
import android.view.Menu
import android.view.MenuItem
import com.bumptech.glide.Glide
import com.pluscubed.recyclerfastscroll.RecyclerFastScroller
import de.psdev.licensesdialog.LicenseResolver
import de.psdev.licensesdialog.LicensesDialog
import de.psdev.licensesdialog.licenses.License
import jahirfiquitiva.libs.frames.R
import jahirfiquitiva.libs.frames.helpers.utils.FL
import jahirfiquitiva.libs.frames.helpers.utils.TRANSLATION_SITE
import jahirfiquitiva.libs.frames.ui.adapters.CreditsAdapter
import jahirfiquitiva.libs.frames.ui.adapters.viewholders.Credit
import jahirfiquitiva.libs.frames.ui.widgets.CustomToolbar
import jahirfiquitiva.libs.frames.ui.widgets.EmptyViewRecyclerView
import jahirfiquitiva.libs.kauextensions.extensions.bind
import jahirfiquitiva.libs.kauextensions.extensions.dividerColor
import jahirfiquitiva.libs.kauextensions.extensions.getActiveIconsColorFor
import jahirfiquitiva.libs.kauextensions.extensions.getPrimaryTextColorFor
import jahirfiquitiva.libs.kauextensions.extensions.getSecondaryTextColorFor
import jahirfiquitiva.libs.kauextensions.extensions.isInHorizontalMode
import jahirfiquitiva.libs.kauextensions.extensions.openLink
import jahirfiquitiva.libs.kauextensions.extensions.primaryColor
import jahirfiquitiva.libs.kauextensions.extensions.stringArray
import jahirfiquitiva.libs.kauextensions.extensions.tint
import jahirfiquitiva.libs.kauextensions.ui.activities.ThemedActivity

open class CreditsActivity : ThemedActivity() {
    
    override fun lightTheme(): Int = R.style.LightTheme
    override fun darkTheme(): Int = R.style.DarkTheme
    override fun transparentTheme(): Int = R.style.TransparentTheme
    override fun amoledTheme(): Int = R.style.AmoledTheme
    
    override fun autoTintStatusBar(): Boolean = true
    override fun autoTintNavigationBar(): Boolean = true
    
    private val toolbar: CustomToolbar by bind(R.id.toolbar)
    private val recyclerView: EmptyViewRecyclerView by bind(R.id.list_rv)
    private val fastScroll: RecyclerFastScroller by bind(R.id.fast_scroller)
    
    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_credits)
        
        registerCCLicense()
        
        toolbar.bindToActivity(this)
        supportActionBar?.title = getString(R.string.about)
        
        val refreshLayout: SwipeRefreshLayout by bind(R.id.swipe_to_refresh)
        refreshLayout.isEnabled = false
        
        recyclerView.itemAnimator = DefaultItemAnimator()
        recyclerView.state = EmptyViewRecyclerView.State.LOADING
        
        val layoutManager = GridLayoutManager(
                this, if (isInHorizontalMode) 2 else 1,
                GridLayoutManager.VERTICAL, false)
        recyclerView.layoutManager = layoutManager
        
        val adapter = CreditsAdapter(getDashboardTitle(), Glide.with(this), buildCreditsList())
        adapter.setLayoutManager(layoutManager)
        recyclerView.adapter = adapter
        
        fastScroll.attachRecyclerView(recyclerView)
        
        try {
            adapter.collapseSection(2)
            adapter.collapseSection(3)
        } catch (ignored: Exception) {
        }
        
        recyclerView.state = EmptyViewRecyclerView.State.NORMAL
    }
    
    @StringRes
    open fun getDashboardTitle() = R.string.frames_dashboard
    
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.about_settings_menu, menu)
        toolbar.tint(
                getPrimaryTextColorFor(primaryColor, 0.6F),
                getSecondaryTextColorFor(primaryColor, 0.6F),
                getActiveIconsColorFor(primaryColor, 0.6F))
        return super.onCreateOptionsMenu(menu)
    }
    
    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        item?.let {
            when (it.itemId) {
                android.R.id.home -> finish()
                R.id.translate -> try {
                    openLink(getTranslationSite())
                } catch (ignored: Exception) {
                }
                R.id.licenses -> LicensesDialog.Builder(this)
                        .setTitle(R.string.licenses)
                        .setNotices(R.raw.notices)
                        .setShowFullLicenseText(false)
                        .setIncludeOwnLicense(false)
                        .setDividerColor(dividerColor)
                        .build().show()
                else -> {
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }
    
    open fun getTranslationSite(): String = TRANSLATION_SITE
    
    private fun registerCCLicense() {
        val ccLicense = object : License() {
            override fun getName(): String =
                    "CreativeCommons Attribution-ShareAlike 4.0 International License"
            
            override fun readSummaryTextFromResources(
                    context: Context
                                                     ): String = readFullTextFromResources(context)
            
            override fun readFullTextFromResources(context: Context): String =
                    "\tLicensed under the CreativeCommons Attribution-ShareAlike\n\t4.0 " +
                            "International License. You may not use this file except in compliance \n" +
                            "\twith the License. You may obtain a copy of the License at\n\n\t\t" +
                            "http://creativecommons.org/licenses/by-sa/4.0/legalcode\n\n" +
                            "\tUnless required by applicable law or agreed to in writing, software\n" +
                            "\tdistributed under the License is distributed on an \"AS IS\" BASIS,\n" +
                            "\tWITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.\n" +
                            "\tSee the License for the specific language governing permissions and\n" +
                            "\tlimitations under the License."
            
            override fun getVersion(): String = "4.0"
            
            override fun getUrl(): String =
                    "http://creativecommons.org/licenses/by-sa/4.0/legalcode"
        }
        
        val eclLicense = object : License() {
            override fun getName(): String = "Educational Community License v2.0"
            
            override fun readSummaryTextFromResources(
                    context: Context
                                                     ): String = readFullTextFromResources(context)
            
            override fun readFullTextFromResources(context: Context): String =
                    "The Educational Community License version 2.0 (\"ECL\") consists of the " +
                            "Apache 2.0 license, modified to change the scope of the patent grant in " +
                            "section 3 to be specific to the needs of the education communities " +
                            "using this license.\n\nLicensed under the Apache License, Version 2.0 " +
                            "(the \"License\");\n" + "you may not use this file except in compliance with " +
                            "the License.\nYou may obtain a copy of the License at\n\n\t" +
                            "http://www.apache.org/licenses/LICENSE-2.0\n\nUnless required by applicable " +
                            "law or agreed to in writing, software\ndistributed under the License is " +
                            "distributed on an \"AS IS\" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY " +
                            "KIND, either express or implied.\nSee the License for the specific " +
                            "language governing permissions and\nlimitations under the License."
            
            override fun getVersion(): String = "2.0"
            
            override fun getUrl(): String = "https://opensource.org/licenses/ECL-2.0"
        }
        LicenseResolver.registerLicense(ccLicense)
        LicenseResolver.registerLicense(eclLicense)
    }
    
    private fun buildCreditsList(): ArrayList<Credit> {
        val list = ArrayList<Credit>()
        
        try {
            val titles = stringArray(R.array.credits_titles)
            val descriptions = stringArray(R.array.credits_descriptions)
            val photos = stringArray(R.array.credits_photos)
            val buttons = stringArray(R.array.credits_buttons)
            val links = stringArray(R.array.credits_links)
            
            if (descriptions.size == titles.size && photos.size == titles.size) {
                (0 until titles.size).mapTo(list) {
                    Credit(
                            titles[it], photos[it], Credit.Type.CREATOR,
                            description = descriptions[it],
                            buttonsTitles = buttons[it].split("|"),
                            buttonsLinks = links[it].split("|"))
                }
            }
            
            list.add(
                    Credit(
                            "Jahir Fiquitiva", JAHIR_PHOTO_URL, Credit.Type.DASHBOARD,
                            description = getString(R.string.dashboard_copyright),
                            buttonsTitles = JAHIR_BUTTONS.split("|"),
                            buttonsLinks = JAHIR_LINKS.split("|")))
            
            list.add(
                    Credit(
                            "Allan Wang", ALLAN_PHOTO_URL, Credit.Type.DASHBOARD,
                            description = getString(R.string.allan_description),
                            buttonsTitles = ALLAN_BUTTONS.split("|"),
                            buttonsLinks = ALLAN_LINKS.split("|")))
            
            list.add(
                    Credit(
                            "Sherry Sabatine", SHERRY_PHOTO_URL, Credit.Type.DASHBOARD,
                            description = getString(R.string.sherry_description),
                            buttonsTitles = SHERRY_BUTTONS.split("|"),
                            buttonsLinks = SHERRY_LINKS.split("|")))
            
            list.addAll(Credit.EXTRA_CREDITS)
        } catch (e: Exception) {
            FL.e { e.message }
        }
        return list
    }
    
    private companion object {
        const val JAHIR_PHOTO_URL =
                "https://github.com/jahirfiquitiva/Website-Resources/raw/master/myself/me-square-white.png"
        const val JAHIR_BUTTONS = "Website|Google+|Play Store"
        const val JAHIR_LINKS =
                "https://www.jahirfiquitiva.me/|https://www.google.com/+JahirFiquitivaR|https://play.google.com/store/apps/dev?id=7438639276314720952"
        
        const val ALLAN_PHOTO_URL = "https://avatars0.githubusercontent.com/u/6251823?v=4&s=400"
        const val ALLAN_BUTTONS = "GitHub|Google+|Play Store"
        const val ALLAN_LINKS =
                "https://github.com/AllanWang|https://plus.google.com/+AllanWPitchedApps|https://play.google.com/store/apps/dev?id=9057916668129524571"
        
        const val SHERRY_PHOTO_URL =
                "https://pbs.twimg.com/profile_images/853258651326459904/yogDkP9p.jpg"
        const val SHERRY_BUTTONS = "Website|Google+"
        const val SHERRY_LINKS =
                "http://photography-by-sherry.com/home|https://plus.google.com/+SherrySabatine"
    }
}