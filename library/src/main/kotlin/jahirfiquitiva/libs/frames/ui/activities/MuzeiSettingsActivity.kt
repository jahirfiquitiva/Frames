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

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.LifecycleRegistry
import android.arch.lifecycle.LifecycleRegistryOwner
import android.arch.lifecycle.Observer
import android.arch.lifecycle.OnLifecycleEvent
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v7.widget.AppCompatCheckBox
import android.support.v7.widget.AppCompatSeekBar
import android.support.v7.widget.Toolbar
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import ca.allanwang.kau.utils.isNetworkAvailable
import com.afollestad.materialdialogs.MaterialDialog
import jahirfiquitiva.libs.frames.R
import jahirfiquitiva.libs.frames.data.models.Collection
import jahirfiquitiva.libs.frames.data.services.FramesArtSource
import jahirfiquitiva.libs.frames.helpers.extensions.buildMaterialDialog
import jahirfiquitiva.libs.frames.helpers.extensions.framesKonfigs
import jahirfiquitiva.libs.frames.providers.viewmodels.CollectionsViewModel
import jahirfiquitiva.libs.frames.providers.viewmodels.WallpapersViewModel
import jahirfiquitiva.libs.kauextensions.activities.ThemedActivity
import jahirfiquitiva.libs.kauextensions.extensions.dividerColor
import jahirfiquitiva.libs.kauextensions.extensions.getActiveIconsColorFor
import jahirfiquitiva.libs.kauextensions.extensions.getPrimaryTextColorFor
import jahirfiquitiva.libs.kauextensions.extensions.getSecondaryTextColorFor
import jahirfiquitiva.libs.kauextensions.extensions.primaryColor
import jahirfiquitiva.libs.kauextensions.extensions.primaryTextColor
import jahirfiquitiva.libs.kauextensions.extensions.secondaryTextColor
import jahirfiquitiva.libs.kauextensions.extensions.tint
import org.jetbrains.anko.collections.forEachWithIndex
import java.util.*
import kotlin.collections.ArrayList

class MuzeiSettingsActivity:ThemedActivity(), LifecycleRegistryOwner, LifecycleObserver {
    override fun lightTheme():Int = R.style.LightTheme
    override fun darkTheme():Int = R.style.DarkTheme
    override fun amoledTheme():Int = R.style.AmoledTheme
    override fun transparentTheme():Int = R.style.TransparentTheme
    override fun autoStatusBarTint():Boolean = true
    
    private val SEEKBAR_STEPS = 1
    private val SEEKBAR_MAX_VALUE = 13
    private val SEEKBAR_MIN_VALUE = 0
    
    private var selectedCollections = ""
    private var dialog:MaterialDialog? = null
    
    private lateinit var collsSummaryText:TextView
    private lateinit var seekBar:AppCompatSeekBar
    private lateinit var checkBox:AppCompatCheckBox
    
    private lateinit var wallsVM:WallpapersViewModel
    private lateinit var collsVM:CollectionsViewModel
    
    val lcOwner = LifecycleRegistry(this)
    override fun getLifecycle():LifecycleRegistry = lcOwner
    
    override fun onCreate(savedInstanceState:Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_muzei_settings)
        
        selectedCollections = framesKonfigs.muzeiCollections
        
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.title = getString(R.string.muzei_settings)
        
        toolbar.tint(getPrimaryTextColorFor(primaryColor, 0.6F),
                     getSecondaryTextColorFor(primaryColor, 0.6F),
                     getActiveIconsColorFor(primaryColor, 0.6F))
        
        val everyTitle = findViewById<TextView>(R.id.every_title)
        everyTitle.setTextColor(primaryTextColor)
        
        val everySummary = findViewById<TextView>(R.id.every_summary)
        everySummary.setTextColor(secondaryTextColor)
        everySummary.text = getString(R.string.every_x, textFromProgress(
                framesKonfigs.muzeiRefreshInterval).toLowerCase(Locale.getDefault()))
        
        seekBar = findViewById(R.id.every_seekbar)
        seekBar.progress = framesKonfigs.muzeiRefreshInterval
        seekBar.incrementProgressBy(SEEKBAR_STEPS)
        seekBar.max = (SEEKBAR_MAX_VALUE - SEEKBAR_MIN_VALUE) / SEEKBAR_STEPS
        
        findViewById<View>(R.id.divider).background = ColorDrawable(dividerColor)
        findViewById<View>(R.id.other_divider).background = ColorDrawable(dividerColor)
        
        findViewById<TextView>(R.id.wifi_only_title).setTextColor(primaryTextColor)
        findViewById<TextView>(R.id.wifi_only_summary).setTextColor(secondaryTextColor)
        
        checkBox = findViewById(R.id.wifi_checkbox)
        checkBox.isChecked = framesKonfigs.refreshMuzeiOnWiFiOnly
        
        findViewById<LinearLayout>(R.id.wifi_only).setOnClickListener {
            checkBox.toggle()
            saveChanges()
        }
        
        findViewById<TextView>(R.id.choose_collections_title).setTextColor(primaryTextColor)
        
        collsSummaryText = findViewById(R.id.choose_collections_summary)
        collsSummaryText.setTextColor(secondaryTextColor)
        collsSummaryText.text = getString(R.string.choose_collections_summary, selectedCollections)
        
        findViewById<LinearLayout>(R.id.choose_collections).setOnClickListener {
            if (!isNetworkAvailable) {
                showNotConnectedDialog()
            } else {
                showChooseCollectionsDialog()
            }
        }
        
        seekBar.setOnSeekBarChangeListener(object:SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar:SeekBar?, progress:Int, fromUser:Boolean) {
                val value = SEEKBAR_MIN_VALUE + (progress * SEEKBAR_STEPS)
                everySummary.text = resources.getString(R.string.every_x,
                                                        textFromProgress(value).toLowerCase(
                                                                Locale.getDefault()))
                saveChanges()
            }
            
            override fun onStartTrackingTouch(p0:SeekBar?) {}
            
            override fun onStopTrackingTouch(p0:SeekBar?) {}
        })
        
    }
    
    override fun onOptionsItemSelected(item:MenuItem?):Boolean {
        item?.let {
            if (it.itemId == android.R.id.home) {
                doFinish()
            }
        }
        return super.onOptionsItemSelected(item)
    }
    
    override fun onBackPressed() = doFinish()
    
    private fun saveChanges() {
        framesKonfigs.muzeiRefreshInterval = seekBar.progress
        framesKonfigs.refreshMuzeiOnWiFiOnly = checkBox.isChecked
        framesKonfigs.muzeiCollections = selectedCollections
    }
    
    private fun showNotConnectedDialog() {
        destroyDialog()
        dialog = buildMaterialDialog {
            title(R.string.muzei_not_connected_title)
            content(R.string.muzei_not_connected_content)
            positiveText(android.R.string.ok)
        }
        dialog?.show()
    }
    
    private fun showChooseCollectionsDialog() {
        destroyDialog()
        dialog = buildMaterialDialog {
            content(R.string.loading)
            progress(true, 0)
            cancelable(false)
        }
        
        try {
            wallsVM.items.removeObservers(this)
        } catch (ignored:Exception) {
        }
        try {
            collsVM.items.removeObservers(this)
        } catch (ignored:Exception) {
        }
        
        wallsVM = ViewModelProviders.of(this).get(WallpapersViewModel::class.java)
        wallsVM.items.observe(this, Observer { list ->
            destroyDialog()
            list?.let {
                collsVM = ViewModelProviders.of(this).get(CollectionsViewModel::class.java)
                collsVM.items.observe(this, Observer { colls ->
                    colls?.let {
                        destroyDialog()
                        val correct = ArrayList<Collection>()
                        correct.addAll(it.distinct())
                        correct.add(0, Collection("Favorites"))
                        correct.distinct()
                        
                        val eachSelectedCollection = selectedCollections.split(",")
                        var selectedIndexes:Array<Int> = arrayOf()
                        correct.forEachIndexed { index, (name) ->
                            eachSelectedCollection.forEach {
                                if (name.equals(it, true))
                                    selectedIndexes = selectedIndexes.plus(index)
                            }
                        }
                        
                        destroyDialog()
                        dialog = buildMaterialDialog {
                            title(R.string.choose_collections_title)
                            items(correct)
                            itemsCallbackMultiChoice(selectedIndexes,
                                                     { _, _, text ->
                                                         val sb = StringBuilder()
                                                         text.forEachWithIndex { i, item ->
                                                             if (i > 0 && i < text.size)
                                                                 sb.append(",")
                                                             sb.append(item)
                                                         }
                                                         selectedCollections = sb.toString()
                                                         collsSummaryText.text = getString(
                                                                 R.string.choose_collections_summary,
                                                                 selectedCollections)
                                                         saveChanges()
                                                         true
                                                     })
                            positiveText(android.R.string.ok)
                            negativeText(android.R.string.cancel)
                        }
                        dialog?.show()
                    }
                })
                collsVM.loadData(it)
            }
        })
        wallsVM.loadData(this)
        dialog?.show()
    }
    
    private fun doFinish() {
        destroyDialog()
        saveChanges()
        try {
            wallsVM.items.removeObservers(this)
        } catch (ignored:Exception) {
        }
        try {
            collsVM.items.removeObservers(this)
        } catch (ignored:Exception) {
        }
        val intent = Intent(this, FramesArtSource::class.java)
        intent.putExtra("restart", true)
        startService(intent)
        ActivityCompat.finishAfterTransition(this)
    }
    
    private fun destroyDialog() {
        dialog?.dismiss()
        dialog = null
    }
    
    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    override fun onDestroy() {
        super.onDestroy()
        doFinish()
    }
    
    private fun textFromProgress(progress:Int):String {
        when (progress) {
            0 -> return 15.toString() + " " + resources.getString(R.string.minutes)
            1 -> return 30.toString() + " " + resources.getString(R.string.minutes)
            2 -> return 45.toString() + " " + resources.getString(R.string.minutes)
            3 -> return 1.toString() + " " + resources.getString(R.string.hours)
            4 -> return 2.toString() + " " + resources.getString(R.string.hours)
            5 -> return 3.toString() + " " + resources.getString(R.string.hours)
            6 -> return 6.toString() + " " + resources.getString(R.string.hours)
            7 -> return 9.toString() + " " + resources.getString(R.string.hours)
            8 -> return 12.toString() + " " + resources.getString(R.string.hours)
            9 -> return 18.toString() + " " + resources.getString(R.string.hours)
            10 -> return 1.toString() + " " + resources.getString(R.string.days)
            11 -> return 3.toString() + " " + resources.getString(R.string.days)
            12 -> return 7.toString() + " " + resources.getString(R.string.days)
            13 -> return 14.toString() + " " + resources.getString(R.string.days)
            else -> return "?"
        }
    }
    
}