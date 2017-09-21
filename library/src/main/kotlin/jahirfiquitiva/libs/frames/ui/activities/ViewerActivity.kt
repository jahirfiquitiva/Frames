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

import android.annotation.SuppressLint
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.support.design.widget.AppBarLayout
import android.support.design.widget.Snackbar
import android.support.v4.app.DialogFragment
import android.support.v4.view.ViewCompat
import android.support.v7.graphics.Palette
import android.view.MenuItem
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.ScaleAnimation
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import ca.allanwang.kau.utils.gone
import ca.allanwang.kau.utils.navigationBarColor
import ca.allanwang.kau.utils.postDelayed
import ca.allanwang.kau.utils.setMarginTop
import ca.allanwang.kau.utils.tint
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import jahirfiquitiva.libs.frames.R
import jahirfiquitiva.libs.frames.data.models.Wallpaper
import jahirfiquitiva.libs.frames.helpers.extensions.buildSnackbar
import jahirfiquitiva.libs.frames.helpers.extensions.getStatusBarHeight
import jahirfiquitiva.libs.frames.helpers.extensions.navigationBarHeight
import jahirfiquitiva.libs.frames.helpers.extensions.setNavBarMargins
import jahirfiquitiva.libs.frames.helpers.extensions.toReadableByteCount
import jahirfiquitiva.libs.frames.helpers.extensions.urlOptions
import jahirfiquitiva.libs.frames.helpers.utils.GlideRequestListener
import jahirfiquitiva.libs.frames.providers.viewmodels.WallpaperInfo
import jahirfiquitiva.libs.frames.providers.viewmodels.WallpaperInfoViewModel
import jahirfiquitiva.libs.frames.ui.activities.base.WallpaperActionsActivity
import jahirfiquitiva.libs.frames.ui.adapters.viewholders.WallpaperDetail
import jahirfiquitiva.libs.frames.ui.fragments.dialogs.InfoBottomSheet
import jahirfiquitiva.libs.frames.ui.fragments.dialogs.InfoDialog
import jahirfiquitiva.libs.frames.ui.fragments.dialogs.WallpaperActionsFragment
import jahirfiquitiva.libs.frames.ui.widgets.CustomToolbar
import jahirfiquitiva.libs.kauextensions.extensions.SimpleAnimationListener
import jahirfiquitiva.libs.kauextensions.extensions.activeIconsColor
import jahirfiquitiva.libs.kauextensions.extensions.applyColorFilter
import jahirfiquitiva.libs.kauextensions.extensions.bind
import jahirfiquitiva.libs.kauextensions.extensions.currentRotation
import jahirfiquitiva.libs.kauextensions.extensions.enableTranslucentStatusBar
import jahirfiquitiva.libs.kauextensions.extensions.generatePalette
import jahirfiquitiva.libs.kauextensions.extensions.getBoolean
import jahirfiquitiva.libs.kauextensions.extensions.getColorFromRes
import jahirfiquitiva.libs.kauextensions.extensions.getDrawable
import jahirfiquitiva.libs.kauextensions.extensions.hasContent
import jahirfiquitiva.libs.kauextensions.extensions.isInPortraitMode
import jahirfiquitiva.libs.ziv.ZoomableImageView
import org.jetbrains.anko.contentView
import org.jetbrains.anko.doAsync
import java.io.FileInputStream
import java.util.*

open class ViewerActivity:WallpaperActionsActivity() {
    
    override var wallpaper:Wallpaper? = null
    override val allowBitmapApply:Boolean = true
    
    override fun lightTheme():Int = R.style.ViewerLightTheme
    override fun darkTheme():Int = R.style.ViewerDarkTheme
    override fun amoledTheme():Int = R.style.ViewerAmoledTheme
    override fun transparentTheme():Int = R.style.ViewerTransparentTheme
    override fun autoStatusBarTint():Boolean = false
    
    private val FAVORITE_ACTION_ID = 3
    
    private val appbar:AppBarLayout by bind(R.id.appbar)
    private val toolbar:CustomToolbar by bind(R.id.toolbar)
    private val bottomBar:View by bind(R.id.bottom_bar)
    private val img:ZoomableImageView by bind(R.id.wallpaper)
    private val loading:ProgressBar by bind(R.id.loading)
    
    private var isInFavorites = false
    private var hasModifiedFavs = false
    private var showFavoritesButton = false
    private var closing = false
    
    private val VISIBLE_SYSTEM_UI_KEY = "visible_system_ui"
    private var visibleSystemUI = true
    private var visibleBottomBar = true
    
    private var infoDialog:DialogFragment? = null
    private val details = ArrayList<WallpaperDetail>()
    private var detailsVM:WallpaperInfoViewModel? = null
    private var palette:Palette? = null
    private var info:WallpaperInfo? = null
    
    override fun onCreate(savedInstanceState:Bundle?) {
        super.onCreate(savedInstanceState)
        
        enableTranslucentStatusBar()
        navigationBarColor = Color.parseColor("#66000000")
        
        setContentView(R.layout.activity_viewer)
        supportPostponeEnterTransition()
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val decor = window.decorView
            val statusBar:View by decor.bind(android.R.id.statusBarBackground)
            val navBar:View by decor.bind(android.R.id.navigationBarBackground)
            val actionBar:View by decor.bind(R.id.action_bar_container)
            
            val viewsToExclude = arrayOf(statusBar, navBar, actionBar)
            val extraViewsToExclude = arrayOf(R.id.appbar, R.id.toolbar, R.id.tabs)
            
            viewsToExclude.forEach { window.sharedElementEnterTransition?.excludeTarget(it, true) }
            extraViewsToExclude.forEach {
                window.sharedElementEnterTransition?.excludeTarget(it, true)
            }
        }
        
        wallpaper = intent?.getParcelableExtra("wallpaper")
        loadWallpaperDetails()
        
        isInFavorites = intent?.getBooleanExtra("inFavorites", false) == true
        showFavoritesButton = intent?.getBooleanExtra("showFavoritesButton", false) == true
        
        toolbar.setMarginTop(getStatusBarHeight(true))
        
        toolbar.bindToActivity(this)
        
        toolbar.tint(getColorFromRes(android.R.color.white), false)
        
        val toolbarTitle:TextView by bind(R.id.toolbar_title)
        val toolbarSubtitle:TextView by bind(R.id.toolbar_subtitle)
        ViewCompat.setTransitionName(toolbarTitle, intent?.getStringExtra("nameTransition") ?: "")
        ViewCompat.setTransitionName(toolbarSubtitle,
                                     intent?.getStringExtra("authorTransition") ?: "")
        toolbarTitle.text = wallpaper?.name ?: ""
        toolbarSubtitle.text = wallpaper?.author ?: ""
        
        findViewById<View>(R.id.bottom_bar_container).setNavBarMargins()
        
        setupProgressBarColors()
        ViewCompat.setTransitionName(img, intent?.getStringExtra("imgTransition") ?: "")
        supportStartPostponedEnterTransition()
        
        val info:View by bind(R.id.info_container)
        info.setOnClickListener {
            showInfoDialog()
        }
        
        val downloadable = wallpaper?.downloadable ?: false
        if (downloadable) {
            findViewById<RelativeLayout>(R.id.download_container).setOnClickListener {
                doItemClick(DOWNLOAD_ACTION_ID)
            }
        } else {
            findViewById<RelativeLayout>(R.id.download_container).gone()
        }
        
        findViewById<RelativeLayout>(R.id.apply_container).setOnClickListener {
            doItemClick(APPLY_ACTION_ID)
        }
        
        if (showFavoritesButton) {
            val favIcon = (if (isInFavorites) "ic_heart" else "ic_heart_outline").getDrawable(this)
            val favImageView:ImageView by bind(R.id.fav_button)
            ViewCompat.setTransitionName(favImageView,
                                         intent?.getStringExtra("favTransition") ?: "")
            favImageView.setImageDrawable(favIcon)
            findViewById<RelativeLayout>(R.id.fav_container).setOnClickListener {
                doItemClick(FAVORITE_ACTION_ID)
            }
        } else {
            findViewById<RelativeLayout>(R.id.fav_container).gone()
        }
        
        img.setOnSingleTapListener { toggleSystemUI(); true }
        
        setupWallpaper(wallpaper)
        loadWallpaperDetails()
        loadExpensiveWallpaperDetails()
    }
    
    override fun onResume() {
        super.onResume()
        dismissInfoDialog()
        findViewById<View>(R.id.bottom_bar_container).setNavBarMargins()
        setupProgressBarColors()
        loadWallpaperDetails()
        loadExpensiveWallpaperDetails()
    }
    
    override fun doItemClick(actionId:Int) {
        when (actionId) {
            FAVORITE_ACTION_ID -> toggleFavorite()
            else -> super.doItemClick(actionId)
        }
    }
    
    private fun showInfoDialog() {
        dismissInfoDialog()
        val showBottomSheet = getBoolean(R.bool.show_bottom_sheet)
        infoDialog = if (showBottomSheet) InfoBottomSheet.build(details, palette)
        else InfoDialog.build(details, palette)
        
        loadExpensiveWallpaperDetails()
        
        if (showBottomSheet) (infoDialog as? InfoBottomSheet)?.show(this)
        else (infoDialog as? InfoDialog)?.show(this)
    }
    
    private fun dismissInfoDialog() {
        infoDialog?.let {
            if (it is InfoBottomSheet) it.animateHide()
            else it.dismiss()
        }
    }
    
    override fun onMultiWindowModeChanged(isInMultiWindowMode:Boolean, newConfig:Configuration?) {
        super.onMultiWindowModeChanged(isInMultiWindowMode, newConfig)
        findViewById<View>(R.id.bottom_bar_container).setNavBarMargins()
    }
    
    override fun onSaveInstanceState(outState:Bundle?) {
        super.onSaveInstanceState(outState)
        outState?.putBoolean(VISIBLE_SYSTEM_UI_KEY, visibleSystemUI)
    }
    
    override fun onRestoreInstanceState(savedInstanceState:Bundle?) {
        super.onRestoreInstanceState(savedInstanceState)
        try {
            val visibleUI = savedInstanceState?.getBoolean(VISIBLE_SYSTEM_UI_KEY, false) ?: false
            setSystemUIVisibility(visibleUI)
        } catch (ignored:Exception) {
        }
    }
    
    override fun onOptionsItemSelected(item:MenuItem?):Boolean {
        if (item?.itemId == android.R.id.home) {
            doFinish()
        }
        return super.onOptionsItemSelected(item)
    }
    
    override fun onBackPressed() {
        val infoVisible = infoDialog?.isVisible ?: false
        if (infoVisible) dismissInfoDialog()
        else {
            super.onBackPressed()
            doFinish()
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        properlyCancelDialog()
    }
    
    private fun doFinish() {
        if (!closing) {
            closing = true
            properlyCancelDialog()
            try {
                img.setZoom(1F)
            } catch (ignored:Exception) {
            }
            detailsVM?.stopTask(true)
            detailsVM?.info?.removeObservers(this)
            detailsVM = null
            postDelayed(100, {
                val intent = Intent()
                intent.putExtra("modified", hasModifiedFavs)
                if (hasModifiedFavs) {
                    intent.putExtra("item", wallpaper)
                    intent.putExtra("inFavorites", isInFavorites)
                }
                setResult(10, intent)
                supportFinishAfterTransition()
                overridePendingTransition(0, 0)
            })
        }
    }
    
    @SuppressLint("NewApi")
    private val isBeingDestroyed:Boolean =
            closing || (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 && isDestroyed)
    
    private fun setupWallpaper(wallpaper:Wallpaper?) {
        if (isBeingDestroyed) return
        
        var bmp:Bitmap? = null
        val filename = intent?.getStringExtra("image") ?: ""
        if (filename.hasContent()) {
            var stream:FileInputStream? = null
            try {
                stream = openFileInput(filename)
                bmp = BitmapFactory.decodeStream(stream)
            } catch (ignored:Exception) {
            } finally {
                img.setImageBitmap(bmp)
                supportStartPostponedEnterTransition()
                stream?.close()
            }
        }
        
        postPalette(bmp)
        
        val d = if (bmp != null) {
            BitmapDrawable(resources, bmp)
        } else {
            ColorDrawable(Color.TRANSPARENT)
        }
        
        wallpaper?.let {
            val listener = object:GlideRequestListener<Bitmap>() {
                override fun onLoadSucceed(resource:Bitmap):Boolean {
                    img.setImageBitmap(resource)
                    postPalette(resource)
                    loadExpensiveWallpaperDetails()
                    return true
                }
                
                override fun onLoadFailed():Boolean = super.onLoadFailed()
            }
            
            val options = urlOptions
                    .placeholder(d).error(d)
                    .dontTransform().dontAnimate()
                    .fitCenter()
            
            if (it.thumbUrl.equals(it.url, true)) {
                Glide.with(this)
                        .asBitmap()
                        .load(it.url)
                        .apply(options.priority(Priority.HIGH))
                        .thumbnail(0.5F)
                        .listener(listener)
                        .into(img)
            } else {
                val thumbnailRequest = Glide.with(this).asBitmap()
                        .load(it.thumbUrl)
                        .apply(options.priority(Priority.IMMEDIATE))
                        .thumbnail(0.5F)
                        .listener(object:GlideRequestListener<Bitmap>() {
                            override fun onLoadSucceed(resource:Bitmap):Boolean {
                                img.setImageBitmap(resource)
                                postPalette(resource)
                                return true
                            }
                            
                            override fun onLoadFailed():Boolean = super.onLoadFailed()
                        })
                
                Glide.with(this).asBitmap()
                        .load(it.url)
                        .apply(options.priority(Priority.HIGH))
                        .thumbnail(thumbnailRequest)
                        .listener(listener)
                        .into(img)
            }
        }
    }
    
    private fun setupProgressBarColors() {
        loading.indeterminateDrawable.applyColorFilter(activeIconsColor)
    }
    
    private fun postPalette(bmp:Bitmap?) {
        palette = bmp?.generatePalette()
        updateInfo()
    }
    
    private fun updateInfo() {
        infoDialog?.let {
            when (it) {
                is InfoBottomSheet -> it.setDetailsAndPalette(details, palette)
                is InfoDialog -> it.setDetailsAndPalette(details, palette)
            }
        }
    }
    
    private fun addToDetails(detail:WallpaperDetail) {
        val pos = details.indexOf(detail)
        if (pos != -1) {
            details.removeAt(pos)
            details.add(pos, detail)
        } else details.add(detail)
    }
    
    private fun loadWallpaperDetails() {
        wallpaper?.let {
            with(it) {
                addToDetails(WallpaperDetail("ic_all_wallpapers", name))
                if (author.hasContent()) addToDetails(WallpaperDetail("ic_person", author))
                if (size != 0L) addToDetails(WallpaperDetail("ic_size", size.toReadableByteCount()))
                if (dimensions.hasContent())
                    addToDetails(WallpaperDetail("ic_dimensions", dimensions))
                if (copyright.hasContent()) addToDetails(WallpaperDetail("ic_copyright", copyright))
                updateInfo()
            }
        }
    }
    
    private fun loadExpensiveWallpaperDetails() {
        wallpaper?.let {
            with(it) {
                if (size != 0L || dimensions.hasContent()) return
                if (isValidInfo(info)) {
                    postWallpaperInfo(info)
                    return
                }
                setupDetailsViewModel()
                doAsync { detailsVM?.loadData(this@ViewerActivity, this@with) }
            }
        }
    }
    
    private fun setupDetailsViewModel() {
        if (detailsVM == null) {
            detailsVM = ViewModelProviders.of(
                    this@ViewerActivity).get(WallpaperInfoViewModel::class.java)
            detailsVM?.info?.observe(this, Observer<WallpaperInfo> { data ->
                data?.let { postWallpaperInfo(it) }
            })
        }
    }
    
    private fun isValidInfo(info:WallpaperInfo?):Boolean =
            info != null && info.size > 0L && info.dimension.isValid
    
    private fun postWallpaperInfo(it:WallpaperInfo?) {
        if (isValidInfo(it) && (info != it)) {
            this.info = it
            addToDetails(WallpaperDetail("ic_size", it?.size?.toReadableByteCount() ?: ""))
            addToDetails(WallpaperDetail("ic_dimensions", it?.dimension?.toString() ?: ""))
            updateInfo()
        }
    }
    
    override fun applyBitmapWallpaper(toHomeScreen:Boolean, toLockScreen:Boolean, toBoth:Boolean) {
        wallpaper?.let {
            properlyCancelDialog()
            wallActions = WallpaperActionsFragment()
            wallActions?.show(this, it, (img.drawable as BitmapDrawable).bitmap,
                              toHomeScreen, toLockScreen, toBoth)
        }
    }
    
    private val ANIMATION_DURATION:Long = 150
    private fun toggleFavorite() = runOnUiThread {
        val favImageView:ImageView by bind(R.id.fav_button)
        val scale = ScaleAnimation(1F, 0F, 1F, 0F, Animation.RELATIVE_TO_SELF, 0.5f,
                                   Animation.RELATIVE_TO_SELF, 0.5f)
        scale.duration = ANIMATION_DURATION
        scale.interpolator = LinearInterpolator()
        scale.setAnimationListener(object:SimpleAnimationListener() {
            override fun onEnd(animation:Animation) {
                super.onEnd(animation)
                favImageView.setImageDrawable(
                        (if (isInFavorites) "ic_heart_outline" else "ic_heart")
                                .getDrawable(this@ViewerActivity))
                
                val nScale = ScaleAnimation(0F, 1F, 0F, 1F, Animation.RELATIVE_TO_SELF, 0.5f,
                                            Animation.RELATIVE_TO_SELF, 0.5f)
                nScale.duration = ANIMATION_DURATION
                nScale.interpolator = LinearInterpolator()
                nScale.setAnimationListener(object:SimpleAnimationListener() {
                    override fun onEnd(animation:Animation) {
                        super.onEnd(animation)
                        wallpaper?.let {
                            showSnackbar(getString(
                                    (if (isInFavorites) R.string.removed_from_favorites else R.string.added_to_favorites),
                                    it.name))
                        }
                        hasModifiedFavs = true
                        isInFavorites = !isInFavorites
                    }
                })
                favImageView.startAnimation(nScale)
            }
        })
        favImageView.startAnimation(scale)
    }
    
    override fun showSnackbar(text:String, settings:Snackbar.() -> Unit) {
        contentView?.let {
            val snack = it.buildSnackbar(text, builder = settings)
            val bottomBarWasVisible = visibleBottomBar
            
            snack.addCallback(object:Snackbar.Callback() {
                override fun onDismissed(transientBottomBar:Snackbar?, event:Int) {
                    super.onDismissed(transientBottomBar, event)
                    if (bottomBarWasVisible) changeBottomBarVisibility(true)
                }
                
                override fun onShown(sb:Snackbar?) {
                    super.onShown(sb)
                }
            })
            
            var bottomNavBar = 0
            var sideNavBar = 0
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val tabletMode = resources.getBoolean(R.bool.md_is_tablet)
                if (tabletMode || isInPortraitMode) {
                    bottomNavBar = navigationBarHeight
                } else {
                    sideNavBar = navigationBarHeight
                }
            }
            
            var extraLeft = 0
            var extraRight = 0
            if (currentRotation == 90) extraRight = sideNavBar
            else if (currentRotation == 270) extraLeft = sideNavBar
            
            snack.view.setPadding(snack.view.paddingLeft + extraLeft, snack.view.paddingTop,
                                  snack.view.paddingRight + extraRight,
                                  snack.view.paddingBottom + bottomNavBar)
            
            val snackText = snack.view.findViewById<TextView>(R.id.snackbar_text)
            snackText.setTextColor(Color.WHITE)
            snackText.maxLines = 3
            
            if (visibleBottomBar) changeBottomBarVisibility(false)
            snack.show()
        }
    }
    
    private fun toggleSystemUI() {
        setSystemUIVisibility(!visibleSystemUI)
    }
    
    private fun setSystemUIVisibility(visible:Boolean) {
        Handler().post({
                           if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                               window.decorView.systemUiVisibility = if (visible)
                                   View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                                           View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                                           View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                               else
                                   View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                                           View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                                           View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                                           View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                                           View.SYSTEM_UI_FLAG_FULLSCREEN or
                                           View.SYSTEM_UI_FLAG_IMMERSIVE or
                                           View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                           }
                           changeBarsVisibility(visible)
                           visibleSystemUI = visible
                       })
    }
    
    private fun changeBarsVisibility(show:Boolean) {
        changeAppBarVisibility(show)
        changeBottomBarVisibility(show)
    }
    
    private fun changeAppBarVisibility(show:Boolean) {
        val transY = (if (show) 0 else -appbar.height).toFloat()
        appbar.animate().translationY(transY)
                .setInterpolator(AccelerateDecelerateInterpolator())
                .start()
    }
    
    private fun changeBottomBarVisibility(show:Boolean) {
        visibleBottomBar = show
        val transY = (if (show) 0 else ((bottomBar.parent as View).height + navigationBarHeight)).toFloat()
        (bottomBar.parent as View).animate().translationY(transY)
                .setInterpolator(AccelerateDecelerateInterpolator())
                .start()
    }
}