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
import android.os.Build
import android.os.Bundle
import android.support.v4.view.ViewCompat
import android.support.v7.widget.Toolbar
import android.transition.Transition
import android.view.MenuItem
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import jahirfiquitiva.libs.frames.R
import jahirfiquitiva.libs.frames.data.models.Collection
import jahirfiquitiva.libs.frames.ui.activities.base.BaseActivityWithFragments
import jahirfiquitiva.libs.frames.ui.fragments.WallpapersInCollectionFragment
import jahirfiquitiva.libs.kauextensions.extensions.getActiveIconsColorFor
import jahirfiquitiva.libs.kauextensions.extensions.getPrimaryTextColorFor
import jahirfiquitiva.libs.kauextensions.extensions.getSecondaryTextColorFor
import jahirfiquitiva.libs.kauextensions.extensions.primaryColor
import jahirfiquitiva.libs.kauextensions.extensions.tint

open class CollectionActivity:BaseActivityWithFragments() {
    
    override fun lightTheme():Int = R.style.LightTheme
    override fun darkTheme():Int = R.style.DarkTheme
    override fun transparentTheme():Int = R.style.TransparentTheme
    override fun amoledTheme():Int = R.style.AmoledTheme
    override fun hasBottomBar():Boolean = true
    override fun fragmentsContainer():Int = R.id.fragments_container
    
    private var fragmentLoaded = false
    private var closing = false
    private var collection:Collection? = null
    
    private lateinit var frag:WallpapersInCollectionFragment
    
    override fun onCreate(savedInstanceState:Bundle?) {
        super.onCreate(savedInstanceState)
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val decor = window.decorView
            val statusBar = decor.findViewById<View>(android.R.id.statusBarBackground)
            val navBar = decor.findViewById<View>(android.R.id.navigationBarBackground)
            val actionBar = decor.findViewById<View>(R.id.action_bar_container)
            
            val viewsToExclude = arrayOf(statusBar, navBar, actionBar)
            val extraViewsToExclude = arrayOf(R.id.appbar, R.id.toolbar, R.id.tabs)
            
            viewsToExclude.forEach { window.sharedElementEnterTransition?.excludeTarget(it, true) }
            extraViewsToExclude.forEach {
                window.sharedElementEnterTransition?.excludeTarget(it, true)
            }
            
            window.enterTransition?.addListener(object:Transition.TransitionListener {
                override fun onTransitionPause(p0:Transition?) = loadFragment()
                override fun onTransitionCancel(p0:Transition?) = loadFragment()
                override fun onTransitionEnd(p0:Transition?) = loadFragment()
                override fun onTransitionStart(p0:Transition?) {}
                override fun onTransitionResume(p0:Transition?) {}
            })
        }
        
        setContentView(R.layout.activity_collection_settings)
        
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        
        setSupportActionBar(toolbar)
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        
        val container = findViewById<FrameLayout>(fragmentsContainer())
        container?.let {
            it.setPadding(it.paddingLeft, it.paddingTop, it.paddingRight, 0)
        }
        
        collection = intent?.getParcelableExtra("item")
        setupToolbarTitle(toolbar)
        toolbar.tint(getPrimaryTextColorFor(primaryColor, 0.6F),
                     getSecondaryTextColorFor(primaryColor, 0.6F),
                     getActiveIconsColorFor(primaryColor, 0.6F))
    }
    
    private fun loadFragment() {
        collection?.let {
            if (!fragmentLoaded) {
                fragmentLoaded = true
                frag = WallpapersInCollectionFragment.create(it, it.wallpapers)
                changeFragment(frag)
            }
        }
    }
    
    private fun setupToolbarTitle(toolbar:Toolbar) {
        val title:TextView
        try {
            val f = toolbar.javaClass.getDeclaredField("mTitleTextView")
            f.isAccessible = true
            title = f.get(toolbar) as TextView
            title.text = collection?.name ?: ""
            toolbar.title = collection?.name ?: ""
            supportActionBar?.title = collection?.name ?: ""
            ViewCompat.setTransitionName(title, "title")
        } catch (ignored:Exception) {
        }
    }
    
    override fun onOptionsItemSelected(item:MenuItem?):Boolean {
        if (item?.itemId == android.R.id.home) {
            doFinish()
        }
        return super.onOptionsItemSelected(item)
    }
    
    override fun onBackPressed() = doFinish()
    
    private fun doFinish() {
        if (!closing) {
            closing = true
            val intent = Intent()
            try {
                intent.putExtra("nFavs", frag.newFavs)
            } catch (ignored:Exception) {
            }
            setResult(11, intent)
            supportFinishAfterTransition()
        }
    }
}