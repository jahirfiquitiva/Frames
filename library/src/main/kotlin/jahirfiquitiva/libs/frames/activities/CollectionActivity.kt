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
import android.support.v4.app.ActivityCompat
import android.support.v4.view.ViewCompat
import android.support.v7.widget.Toolbar
import android.view.MenuItem
import android.widget.FrameLayout
import android.widget.TextView
import jahirfiquitiva.libs.frames.R
import jahirfiquitiva.libs.frames.activities.base.BaseActivityWithFragments
import jahirfiquitiva.libs.frames.fragments.WallpapersInCollectionFragment
import jahirfiquitiva.libs.frames.models.Collection
import jahirfiquitiva.libs.kauextensions.extensions.getActiveIconsColorFor
import jahirfiquitiva.libs.kauextensions.extensions.getPrimaryTextColorFor
import jahirfiquitiva.libs.kauextensions.extensions.getSecondaryTextColorFor
import jahirfiquitiva.libs.kauextensions.extensions.primaryColor
import jahirfiquitiva.libs.kauextensions.extensions.setupStatusBarStyle
import jahirfiquitiva.libs.kauextensions.extensions.tint

open class CollectionActivity:BaseActivityWithFragments() {
    
    override fun lightTheme():Int = R.style.LightTheme
    override fun darkTheme():Int = R.style.DarkTheme
    override fun transparentTheme():Int = R.style.TransparentTheme
    override fun amoledTheme():Int = R.style.AmoledTheme
    override fun hasBottomBar():Boolean = true
    override fun fragmentsContainer():Int = R.id.fragments_container
    
    private var collection:Collection? = null
    private lateinit var frag:WallpapersInCollectionFragment
    
    override fun onCreate(savedInstanceState:Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_with_fragments)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        val toolbarTitle = findViewById<TextView>(R.id.toolbar_title)
        
        setSupportActionBar(toolbar)
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        
        toolbar.tint(getPrimaryTextColorFor(primaryColor, 0.6F),
                     getSecondaryTextColorFor(primaryColor, 0.6F),
                     getActiveIconsColorFor(primaryColor, 0.6F))
        toolbarTitle.setTextColor(getPrimaryTextColorFor(primaryColor, 0.6F))
        setupStatusBarStyle(false)
        
        val container = findViewById<FrameLayout>(fragmentsContainer())
        container?.let {
            it.setPadding(it.paddingLeft, it.paddingTop, it.paddingRight, 0)
        }
        
        ViewCompat.setTransitionName(toolbarTitle, "title")
        
        collection = intent?.getParcelableExtra<Collection>("item")
        toolbarTitle.text = collection?.name ?: ""
        
        val bundle = Bundle()
        bundle.putParcelable("collection", collection)
        frag = WallpapersInCollectionFragment.newInstance(bundle)
        changeFragment(frag)
    }
    
    override fun onOptionsItemSelected(item:MenuItem?):Boolean {
        if (item?.itemId == android.R.id.home) {
            doFinish()
        }
        return super.onOptionsItemSelected(item)
    }
    
    override fun onBackPressed() {
        doFinish()
    }
    
    private fun doFinish() {
        val intent = Intent()
        intent.putExtra("favs", frag.currentFavorites)
        setResult(11, intent)
        ActivityCompat.finishAfterTransition(this)
    }
    
}