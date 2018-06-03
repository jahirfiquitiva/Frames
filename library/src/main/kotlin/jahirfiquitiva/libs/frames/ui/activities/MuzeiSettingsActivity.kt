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
import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.OnLifecycleEvent
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.support.v7.widget.AppCompatCheckBox
import android.support.v7.widget.AppCompatSeekBar
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import ca.allanwang.kau.utils.gone
import ca.allanwang.kau.utils.isNetworkAvailable
import com.afollestad.materialdialogs.MaterialDialog
import jahirfiquitiva.libs.frames.R
import jahirfiquitiva.libs.frames.data.models.Collection
import jahirfiquitiva.libs.frames.data.services.FramesArtSource
import jahirfiquitiva.libs.frames.helpers.extensions.mdDialog
import jahirfiquitiva.libs.frames.helpers.utils.FramesKonfigs
import jahirfiquitiva.libs.frames.ui.widgets.CustomToolbar
import jahirfiquitiva.libs.frames.viewmodels.CollectionsViewModel
import jahirfiquitiva.libs.frames.viewmodels.WallpapersViewModel
import jahirfiquitiva.libs.kext.extensions.bind
import jahirfiquitiva.libs.kext.extensions.boolean
import jahirfiquitiva.libs.kext.extensions.dividerColor
import jahirfiquitiva.libs.kext.extensions.getActiveIconsColorFor
import jahirfiquitiva.libs.kext.extensions.getPrimaryTextColorFor
import jahirfiquitiva.libs.kext.extensions.getSecondaryTextColorFor
import jahirfiquitiva.libs.kext.extensions.primaryColor
import jahirfiquitiva.libs.kext.extensions.primaryTextColor
import jahirfiquitiva.libs.kext.extensions.secondaryTextColor
import jahirfiquitiva.libs.kext.extensions.tint
import jahirfiquitiva.libs.kext.ui.activities.ThemedActivity
import java.util.Locale

class MuzeiSettingsActivity : ThemedActivity<FramesKonfigs>() {
    companion object {
        private const val SEEKBAR_STEPS = 1
        private const val SEEKBAR_MAX_VALUE = 13
        private const val SEEKBAR_MIN_VALUE = 0
    }
    
    override val configs: FramesKonfigs by lazy { FramesKonfigs(this) }
    override fun lightTheme(): Int = R.style.Frames_LightTheme
    override fun darkTheme(): Int = R.style.Frames_DarkTheme
    override fun amoledTheme(): Int = R.style.Frames_AmoledTheme
    override fun transparentTheme(): Int = R.style.Frames_TransparentTheme
    
    override fun autoTintStatusBar(): Boolean = true
    override fun autoTintNavigationBar(): Boolean = true
    
    private var selectedCollections = ""
    private var dialog: MaterialDialog? = null
    
    private val collsSummaryText: TextView? by bind(R.id.choose_collections_summary)
    private val seekBar: AppCompatSeekBar? by bind(R.id.every_seekbar)
    private val checkBox: AppCompatCheckBox? by bind(R.id.wifi_checkbox)
    
    private val wallsVM: WallpapersViewModel by lazy {
        ViewModelProviders.of(this).get(WallpapersViewModel::class.java)
    }
    private val collsVM: CollectionsViewModel by lazy {
        ViewModelProviders.of(this).get(CollectionsViewModel::class.java)
    }
    
    @SuppressLint("WrongViewCast", "MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_muzei_settings)
        
        selectedCollections = configs.muzeiCollections
        
        val toolbar by bind<CustomToolbar>(R.id.toolbar)
        toolbar?.bindToActivity(this)
        supportActionBar?.title = getString(R.string.muzei_settings)
        
        toolbar?.tint(
            getPrimaryTextColorFor(primaryColor, 0.6F),
            getSecondaryTextColorFor(primaryColor, 0.6F),
            getActiveIconsColorFor(primaryColor, 0.6F))
        
        val everyTitle: TextView? by bind(R.id.every_title)
        everyTitle?.setTextColor(primaryTextColor)
        
        val everySummary: TextView? by bind(R.id.every_summary)
        everySummary?.setTextColor(secondaryTextColor)
        everySummary?.text = getString(
            R.string.every_x, textFromProgress(
            configs.muzeiRefreshInterval).toLowerCase(Locale.getDefault()))
        
        seekBar?.progress = configs.muzeiRefreshInterval
        seekBar?.incrementProgressBy(SEEKBAR_STEPS)
        seekBar?.max = (SEEKBAR_MAX_VALUE - SEEKBAR_MIN_VALUE) / SEEKBAR_STEPS
        
        val isFramesApp = boolean(R.bool.isFrames)
        
        findViewById<View>(R.id.divider).background = ColorDrawable(dividerColor)
        if (isFramesApp) {
            findViewById<View>(R.id.other_divider).background = ColorDrawable(dividerColor)
        } else {
            findViewById<View>(R.id.other_divider).gone()
        }
        
        findViewById<TextView>(R.id.wifi_only_title).setTextColor(primaryTextColor)
        findViewById<TextView>(R.id.wifi_only_summary).setTextColor(secondaryTextColor)
        
        checkBox?.isChecked = configs.refreshMuzeiOnWiFiOnly
        
        findViewById<LinearLayout>(R.id.wifi_only).setOnClickListener {
            checkBox?.toggle()
            saveChanges()
        }
        
        findViewById<TextView>(R.id.choose_collections_title).setTextColor(primaryTextColor)
        
        collsSummaryText?.setTextColor(secondaryTextColor)
        collsSummaryText?.text = getString(R.string.choose_collections_summary, selectedCollections)
        
        if (isFramesApp) {
            findViewById<LinearLayout>(R.id.choose_collections).setOnClickListener {
                if (isNetworkAvailable) {
                    showChooseCollectionsDialog()
                } else {
                    showNotConnectedDialog()
                }
            }
        } else {
            findViewById<LinearLayout>(R.id.choose_collections).gone()
        }
        
        seekBar?.setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                                              ) {
                    val value = SEEKBAR_MIN_VALUE + (progress * SEEKBAR_STEPS)
                    everySummary?.text = resources.getString(
                        R.string.every_x,
                        textFromProgress(value).toLowerCase(
                            Locale.getDefault()))
                    saveChanges()
                }
                
                override fun onStartTrackingTouch(p0: SeekBar?) {}
                
                override fun onStopTrackingTouch(p0: SeekBar?) {}
            })
        
    }
    
    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        item?.let {
            if (it.itemId == android.R.id.home) {
                doFinish()
            }
        }
        return super.onOptionsItemSelected(item)
    }
    
    override fun onBackPressed() = doFinish()
    
    private fun saveChanges() {
        configs.muzeiRefreshInterval = seekBar?.progress ?: 10
        configs.refreshMuzeiOnWiFiOnly = checkBox?.isChecked ?: false
        configs.muzeiCollections = selectedCollections
    }
    
    private fun showNotConnectedDialog() {
        destroyDialog()
        dialog = mdDialog {
            title(R.string.muzei_not_connected_title)
            content(R.string.muzei_not_connected_content)
            positiveText(android.R.string.ok)
        }
        dialog?.show()
    }
    
    private fun showChooseCollectionsDialog() {
        destroyDialog()
        dialog = mdDialog {
            content(R.string.loading)
            progress(true, 0)
            cancelable(false)
        }
        
        try {
            wallsVM.destroy(this)
        } catch (ignored: Exception) {
        }
        try {
            collsVM.destroy(this)
        } catch (ignored: Exception) {
        }
        
        wallsVM.observe(
            this, {
            destroyDialog()
            collsVM.observe(
                this, {
                destroyDialog()
                val correct = ArrayList<Collection>()
                correct.addAll(it.distinct())
                correct.add(0, Collection("Favorites"))
                correct.distinct()
                
                val eachSelectedCollection = selectedCollections.split(",")
                var selectedIndexes: Array<Int> = arrayOf()
                correct.forEachIndexed { index, (name) ->
                    eachSelectedCollection.forEach {
                        if (name.equals(it, true))
                            selectedIndexes = selectedIndexes.plus(index)
                    }
                }
                
                destroyDialog()
                dialog = mdDialog {
                    title(R.string.choose_collections_title)
                    items(correct)
                    itemsCallbackMultiChoice(
                        selectedIndexes,
                        { _, _, text ->
                            val sb = StringBuilder()
                            text.forEachIndexed { i, item ->
                                if (i > 0 && i < text.size)
                                    sb.append(",")
                                sb.append(item)
                            }
                            selectedCollections = sb.toString()
                            collsSummaryText?.text = getString(
                                R.string.choose_collections_summary, selectedCollections)
                            saveChanges()
                            true
                        })
                    positiveText(android.R.string.ok)
                    negativeText(android.R.string.cancel)
                }
                dialog?.show()
            })
            collsVM.loadWithContext(this, ArrayList(it))
        })
        wallsVM.loadData(this)
        dialog?.show()
    }
    
    private fun doFinish() {
        destroyDialog()
        saveChanges()
        try {
            wallsVM.destroy(this)
        } catch (ignored: Exception) {
        }
        try {
            collsVM.destroy(this)
        } catch (ignored: Exception) {
        }
        val intent = Intent(this, FramesArtSource::class.java)
        intent.putExtra("restart", true)
        startService(intent)
        try {
            supportFinishAfterTransition()
        } catch (e: Exception) {
            finish()
        }
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
    
    private fun textFromProgress(progress: Int): String {
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