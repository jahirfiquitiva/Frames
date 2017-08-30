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

import android.content.Context
import android.os.Bundle
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import com.bumptech.glide.Glide
import com.pluscubed.recyclerfastscroll.RecyclerFastScroller
import de.psdev.licensesdialog.LicenseResolver
import de.psdev.licensesdialog.LicensesDialog
import de.psdev.licensesdialog.licenses.License
import jahirfiquitiva.libs.frames.R
import jahirfiquitiva.libs.frames.ui.adapters.CreditsAdapter
import jahirfiquitiva.libs.frames.ui.adapters.viewholders.Credit
import jahirfiquitiva.libs.frames.ui.widgets.EmptyViewRecyclerView
import jahirfiquitiva.libs.kauextensions.activities.ThemedActivity
import jahirfiquitiva.libs.kauextensions.extensions.dividerColor
import jahirfiquitiva.libs.kauextensions.extensions.getActiveIconsColorFor
import jahirfiquitiva.libs.kauextensions.extensions.getPrimaryTextColorFor
import jahirfiquitiva.libs.kauextensions.extensions.getSecondaryTextColorFor
import jahirfiquitiva.libs.kauextensions.extensions.getStringArray
import jahirfiquitiva.libs.kauextensions.extensions.isInHorizontalMode
import jahirfiquitiva.libs.kauextensions.extensions.primaryColor
import jahirfiquitiva.libs.kauextensions.extensions.tint

open class CreditsActivity:ThemedActivity() {
    
    override fun lightTheme():Int = R.style.LightTheme
    override fun darkTheme():Int = R.style.DarkTheme
    override fun transparentTheme():Int = R.style.TransparentTheme
    override fun amoledTheme():Int = R.style.AmoledTheme
    override fun autoStatusBarTint():Boolean = true
    
    private lateinit var toolbar:Toolbar
    private lateinit var rv:EmptyViewRecyclerView
    private lateinit var fastScroll:RecyclerFastScroller
    
    override fun onCreate(savedInstanceState:Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_credits)
        
        registerCCLicense()
        
        toolbar = findViewById(R.id.toolbar)
        
        setSupportActionBar(toolbar)
        supportActionBar?.title = getString(R.string.about)
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        
        rv = findViewById(R.id.list_rv)
        rv.itemAnimator = DefaultItemAnimator()
        rv.state = EmptyViewRecyclerView.State.LOADING
        
        val layoutManager = GridLayoutManager(this, if (isInHorizontalMode) 2 else 1,
                                              GridLayoutManager.VERTICAL, false)
        rv.layoutManager = layoutManager
        
        val adapter = CreditsAdapter(Glide.with(this), buildCreditsList())
        adapter.setLayoutManager(layoutManager)
        rv.adapter = adapter
        
        fastScroll = findViewById(R.id.fast_scroller)
        fastScroll.attachRecyclerView(rv)
        
        try {
            adapter.collapseSection(2)
            adapter.collapseSection(3)
        } catch (ignored:Exception) {
        }
        
        rv.state = EmptyViewRecyclerView.State.NORMAL
    }
    
    override fun onCreateOptionsMenu(menu:Menu?):Boolean {
        menuInflater.inflate(R.menu.about_menu, menu)
        toolbar.tint(getPrimaryTextColorFor(primaryColor, 0.6F),
                     getSecondaryTextColorFor(primaryColor, 0.6F),
                     getActiveIconsColorFor(primaryColor, 0.6F))
        return super.onCreateOptionsMenu(menu)
    }
    
    override fun onOptionsItemSelected(item:MenuItem?):Boolean {
        item?.let {
            if (it.itemId == android.R.id.home) finish()
            else if (it.itemId == R.id.licenses) {
                LicensesDialog.Builder(this)
                        .setTitle(R.string.licenses)
                        .setNotices(R.raw.notices)
                        .setShowFullLicenseText(false)
                        .setIncludeOwnLicense(false)
                        .setDividerColor(dividerColor)
                        .build().show()
            }
        }
        return super.onOptionsItemSelected(item)
    }
    
    private fun registerCCLicense() {
        val ccLicense = object:License() {
            override fun getName():String = "CreativeCommons Attribution-ShareAlike 4.0 International License"
            
            override fun readSummaryTextFromResources(
                    context:Context):String = readFullTextFromResources(context)
            
            override fun readFullTextFromResources(context:Context):String =
                    "\tLicensed under the CreativeCommons Attribution-ShareAlike\n\t4.0 " +
                            "International License. You may not use this file except in compliance \n" +
                            "\twith the License. You may obtain a copy of the License at\n\n\t\t" +
                            "http://creativecommons.org/licenses/by-sa/4.0/legalcode\n\n" +
                            "\tUnless required by applicable law or agreed to in writing, software\n" +
                            "\tdistributed under the License is distributed on an \"AS IS\" BASIS,\n" +
                            "\tWITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.\n" +
                            "\tSee the License for the specific language governing permissions and\n" +
                            "\tlimitations under the License."
            
            override fun getVersion():String = "4.0"
            
            override fun getUrl():String = "http://creativecommons.org/licenses/by-sa/4.0/legalcode"
        }
        
        val eclLicense = object:License() {
            override fun getName():String = "Educational Community License v2.0"
            
            override fun readSummaryTextFromResources(
                    context:Context):String = readFullTextFromResources(context)
            
            override fun readFullTextFromResources(context:Context):String =
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
            
            override fun getVersion():String = "2.0"
            
            override fun getUrl():String = "https://opensource.org/licenses/ECL-2.0"
        }
        LicenseResolver.registerLicense(ccLicense)
        LicenseResolver.registerLicense(eclLicense)
    }
    
    private fun buildCreditsList():ArrayList<Credit> {
        val list = ArrayList<Credit>()
        
        try {
            val titles = getStringArray(R.array.credits_titles)
            val descriptions = getStringArray(R.array.credits_descriptions)
            val photos = getStringArray(R.array.credits_photos)
            val buttons = getStringArray(R.array.credits_buttons)
            val links = getStringArray(R.array.credits_links)
            
            if (descriptions.size == titles.size && photos.size == titles.size) {
                (0 until titles.size).mapTo(list) {
                    Credit(Credit.Type.CREATOR, photos[it], titles[it], descriptions[it],
                           buttons[it].split("|"), links[it].split("|"))
                }
            }
            
            list.add(Credit(Credit.Type.DASHBOARD, JAHIR_PHOTO_URL, "Jahir Fiquitiva",
                            getString(R.string.dashboard_copyright), JAHIR_BUTTONS.split("|"),
                            JAHIR_LINKS.split("|")))
            
            list.add(Credit(Credit.Type.DASHBOARD, ALLAN_PHOTO_URL, "Allan Wang",
                            getString(R.string.allan_description), ALLAN_BUTTONS.split("|"),
                            ALLAN_LINKS.split("|")))
            
            list.add(Credit(Credit.Type.DASHBOARD, SHERRY_PHOTO_URL, "Sherry Sabatine",
                            getString(R.string.sherry_description), SHERRY_BUTTONS.split("|"),
                            SHERRY_LINKS.split("|")))
            
            list.add(Credit(Credit.Type.DEV_CONTRIBUTION, JAMES_PHOTO_URL, "James Fenn", "",
                            link = "https://plus.google.com/+JamesFennJAFFA2157"))
            
            list.add(Credit(Credit.Type.DEV_CONTRIBUTION, MAX_PHOTO_URL, "Maximilian Keppeler", "",
                            link = "https://plus.google.com/+MaxKeppeler"))
            
            list.add(Credit(Credit.Type.UI_CONTRIBUTION, PATRYK_PHOTO_URL, "Patryk Goworowski",
                            link = "https://plus.google.com/+PatrykGoworowski"))
            
            list.add(Credit(Credit.Type.UI_CONTRIBUTION, LUMIQ_PHOTO_URL, "Lumiq Creative",
                            link = "https://plus.google.com/+LumiqCreative"))
        } catch (e:Exception) {
            e.printStackTrace()
        }
        
        return list
    }
}

const val JAHIR_PHOTO_URL = "https://github.com/jahirfiquitiva/Website-Resources/raw/master/myself/me-square-white.png"
const val JAHIR_BUTTONS = "Website|Google+|Play Store"
const val JAHIR_LINKS = "https://www.jahirfiquitiva.me/|https://www.google.com/+JahirFiquitivaR|https://play.google.com/store/apps/dev?id=7438639276314720952"

const val ALLAN_PHOTO_URL = "https://avatars0.githubusercontent.com/u/6251823?v=4&s=400"
const val ALLAN_BUTTONS = "GitHub|Google+|Play Store"
const val ALLAN_LINKS = "https://github.com/AllanWang|https://plus.google.com/+AllanWPitchedApps|https://play.google.com/store/apps/dev?id=9057916668129524571"

const val SHERRY_PHOTO_URL = "https://pbs.twimg.com/profile_images/853258651326459904/yogDkP9p.jpg"
const val SHERRY_BUTTONS = "Website|Google+"
const val SHERRY_LINKS = "http://photography-by-sherry.com/home|https://plus.google.com/+SherrySabatine"

const val JAMES_PHOTO_URL = "https://lh3.googleusercontent.com/H1lDr6FlSvHQe4oIogYUGNWIDLb69LcIVCYciPUzql7Q_Nrq4wp-3yKh1uSfTPV3iM0DnC1icD-80YQ=w1107-h623-rw-no"
const val MAX_PHOTO_URL = "https://lh3.googleusercontent.com/yvcLR6mThBOpHYo6iIG9SlyEHmmVgO1LaPIv_Eu9unSGqt99fnaBVLtR1rom16c_t98tz_sxGeo8Ba5MPCI=w1107-h623-rw-no"
const val LUMIQ_PHOTO_URL = "https://lh3.googleusercontent.com/AEM9NXPSVn77YGo4SIQIeMyyTb7BWkwp96XcJlnYfHZU1fFxDZ2cvXlJSzu-3Nb-rj7Sl4x-0QMG8m_3Rg=w1107-h623-rw-no"
const val PATRYK_PHOTO_URL = "https://lh3.googleusercontent.com/EpfG2M4si7jn_lk01ure5CGDPF07Aw3YPA88NMvoG1txfGIPc-feN2LdrBby_5W8VPJNCBNGjzCtOYclHck=w1107-h623-rw-no"