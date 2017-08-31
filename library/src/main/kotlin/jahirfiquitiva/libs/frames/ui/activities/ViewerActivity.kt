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

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.support.annotation.StringRes
import android.support.design.widget.AppBarLayout
import android.support.design.widget.Snackbar
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewCompat
import android.support.v7.widget.Toolbar
import android.view.MenuItem
import android.view.View
import android.view.ViewTreeObserver
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.ScaleAnimation
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import ca.allanwang.kau.utils.gone
import ca.allanwang.kau.utils.isNetworkAvailable
import ca.allanwang.kau.utils.postDelayed
import ca.allanwang.kau.utils.setMarginTop
import ca.allanwang.kau.utils.tint
import ca.allanwang.kau.utils.toBitmap
import ca.allanwang.kau.utils.visible
import ca.allanwang.kau.utils.visibleIf
import com.afollestad.materialdialogs.MaterialDialog
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.GlideDrawable
import jahirfiquitiva.libs.frames.R
import jahirfiquitiva.libs.frames.data.models.Wallpaper
import jahirfiquitiva.libs.frames.helpers.configs.bestBitmapConfig
import jahirfiquitiva.libs.frames.helpers.extensions.PermissionRequestListener
import jahirfiquitiva.libs.frames.helpers.extensions.buildMaterialDialog
import jahirfiquitiva.libs.frames.helpers.extensions.buildSnackbar
import jahirfiquitiva.libs.frames.helpers.extensions.checkPermission
import jahirfiquitiva.libs.frames.helpers.extensions.framesKonfigs
import jahirfiquitiva.libs.frames.helpers.extensions.getStatusBarHeight
import jahirfiquitiva.libs.frames.helpers.extensions.navigationBarHeight
import jahirfiquitiva.libs.frames.helpers.extensions.openWallpaper
import jahirfiquitiva.libs.frames.helpers.extensions.requestPermissions
import jahirfiquitiva.libs.frames.helpers.extensions.setNavBarMargins
import jahirfiquitiva.libs.frames.helpers.utils.GlideRequestListener
import jahirfiquitiva.libs.frames.ui.fragments.dialogs.WallpaperActionsFragment
import jahirfiquitiva.libs.frames.ui.widgets.SimpleAnimationListener
import jahirfiquitiva.libs.kauextensions.activities.ThemedActivity
import jahirfiquitiva.libs.kauextensions.extensions.applyColorFilter
import jahirfiquitiva.libs.kauextensions.extensions.bestSwatch
import jahirfiquitiva.libs.kauextensions.extensions.cardBackgroundColor
import jahirfiquitiva.libs.kauextensions.extensions.currentRotation
import jahirfiquitiva.libs.kauextensions.extensions.enableTranslucentStatusBar
import jahirfiquitiva.libs.kauextensions.extensions.formatCorrectly
import jahirfiquitiva.libs.kauextensions.extensions.getActiveIconsColorFor
import jahirfiquitiva.libs.kauextensions.extensions.getAppName
import jahirfiquitiva.libs.kauextensions.extensions.getColorFromRes
import jahirfiquitiva.libs.kauextensions.extensions.getDrawable
import jahirfiquitiva.libs.kauextensions.extensions.getUri
import jahirfiquitiva.libs.kauextensions.extensions.hasContent
import jahirfiquitiva.libs.kauextensions.extensions.isInPortraitMode
import jahirfiquitiva.libs.ziv.ZoomableImageView
import org.jetbrains.anko.contentView
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

open class ViewerActivity:ThemedActivity() {
    
    override fun lightTheme():Int = R.style.ViewerLightTheme
    override fun darkTheme():Int = R.style.ViewerDarkTheme
    override fun amoledTheme():Int = R.style.ViewerAmoledTheme
    override fun transparentTheme():Int = R.style.ViewerTransparentTheme
    override fun autoStatusBarTint():Boolean = false
    
    private var wallpaper:Wallpaper? = null
    private var actionDialog:MaterialDialog? = null
    private var wallActions:WallpaperActionsFragment? = null
    
    private lateinit var appbar:AppBarLayout
    private lateinit var toolbar:Toolbar
    private lateinit var bottomBar:View
    private lateinit var img:ZoomableImageView
    
    private var isInFavorites = false
    private var hasModifiedFavs = false
    private var showFavoritesButton = false
    private var transitioned = false
    private var closing = false
    
    private val VISIBLE_PROGRESS_BAR_KEY = "visible_progress_bar"
    private var visibleProgressBar = true
    private val VISIBLE_SYSTEM_UI_KEY = "visible_system_ui"
    private var visibleSystemUI = true
    private var visibleBottomBar = true
    
    private val DOWNLOAD_ACTION_ID = 1
    private val APPLY_ACTION_ID = 2
    private val FAVORITE_ACTION_ID = 3
    
    override fun onCreate(savedInstanceState:Bundle?) {
        super.onCreate(savedInstanceState)
        enableTranslucentStatusBar()
        
        setContentView(R.layout.activity_viewer)
        supportPostponeEnterTransition()
        
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
        }
        
        wallpaper = intent?.getParcelableExtra("wallpaper")
        isInFavorites = intent?.getBooleanExtra("inFavorites", false) == true
        showFavoritesButton = intent?.getBooleanExtra("showFavoritesButton", false) == true
        
        appbar = findViewById(R.id.appbar)
        toolbar = findViewById(R.id.toolbar)
        toolbar.setMarginTop(getStatusBarHeight(true))
        
        setSupportActionBar(toolbar)
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        
        toolbar.tint(getColorFromRes(android.R.color.white), false)
        
        val toolbarTitle = findViewById<TextView>(R.id.toolbar_title)
        val toolbarSubtitle = findViewById<TextView>(R.id.toolbar_subtitle)
        ViewCompat.setTransitionName(toolbarTitle, intent?.getStringExtra("nameTransition") ?: "")
        ViewCompat.setTransitionName(toolbarSubtitle,
                                     intent?.getStringExtra("authorTransition") ?: "")
        toolbarTitle.text = wallpaper?.name ?: ""
        toolbarSubtitle.text = wallpaper?.author ?: ""
        
        bottomBar = findViewById(R.id.bottom_bar)
        findViewById<View>(R.id.bottom_bar_container).setNavBarMargins()
        
        val downloadable = wallpaper?.downloadable ?: false
        if (downloadable) {
            findViewById<ImageView>(R.id.download_button).setOnClickListener {
                doItemClick(DOWNLOAD_ACTION_ID)
            }
        } else {
            findViewById<ImageView>(R.id.download_container).gone()
        }
        
        findViewById<ImageView>(R.id.apply_button).setOnClickListener {
            doItemClick(APPLY_ACTION_ID)
        }
        
        if (showFavoritesButton) {
            val favIcon = (if (isInFavorites) "ic_heart" else "ic_heart_outline").getDrawable(this)
            val favImageView = findViewById<ImageView>(R.id.fav_button)
            ViewCompat.setTransitionName(favImageView,
                                         intent?.getStringExtra("favTransition") ?: "")
            favImageView.setImageDrawable(favIcon)
            favImageView.setOnClickListener { doItemClick(FAVORITE_ACTION_ID) }
        } else {
            findViewById<RelativeLayout>(R.id.fav_container).gone()
        }
        
        img = findViewById(R.id.wallpaper)
        ViewCompat.setTransitionName(img, intent?.getStringExtra("imgTransition") ?: "")
        
        setupWallpaper(wallpaper)
        
        img.setOnSingleTapListener {
            toggleSystemUI()
            true
        }
    }
    
    override fun onResume() {
        super.onResume()
        findViewById<View>(R.id.bottom_bar_container).setNavBarMargins()
        val dummy:Bitmap? = null
        setupProgressBarColors(dummy)
    }
    
    override fun onMultiWindowModeChanged(isInMultiWindowMode:Boolean, newConfig:Configuration?) {
        super.onMultiWindowModeChanged(isInMultiWindowMode, newConfig)
        findViewById<View>(R.id.bottom_bar_container).setNavBarMargins()
    }
    
    override fun onSaveInstanceState(outState:Bundle?) {
        super.onSaveInstanceState(outState)
        outState?.putBoolean(VISIBLE_PROGRESS_BAR_KEY, visibleProgressBar)
        outState?.putBoolean(VISIBLE_SYSTEM_UI_KEY, visibleSystemUI)
    }
    
    override fun onRestoreInstanceState(savedInstanceState:Bundle?) {
        super.onRestoreInstanceState(savedInstanceState)
        try {
            val visibleProgress = savedInstanceState?.getBoolean(VISIBLE_PROGRESS_BAR_KEY,
                                                                 true) ?: true
            findViewById<ProgressBar>(R.id.loading).visibleIf(visibleProgress)
        } catch (ignored:Exception) {
        }
        try {
            val visibleUI = savedInstanceState?.getBoolean(VISIBLE_SYSTEM_UI_KEY, true) ?: true
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
        super.onBackPressed()
        doFinish()
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
    
    private fun setupWallpaper(wallpaper:Wallpaper?) {
        var bmp:Bitmap? = null
        val filename = intent?.getStringExtra("image") ?: ""
        if (filename.hasContent()) {
            try {
                val stream = openFileInput(filename)
                bmp = BitmapFactory.decodeStream(stream)
                stream.close()
            } catch (ignored:Exception) {
            }
        }
        
        setupProgressBarColors(bmp)
        
        val d:Drawable
        d = if (bmp != null) {
            findViewById<ProgressBar>(R.id.loading).visible()
            BitmapDrawable(resources, bmp)
        } else {
            ColorDrawable(ContextCompat.getColor(this, android.R.color.transparent))
        }
        
        wallpaper?.let {
            val listener = object:GlideRequestListener<GlideDrawable>() {
                override fun onLoadSucceed(resource:GlideDrawable):Boolean {
                    findViewById<ProgressBar>(R.id.loading).gone()
                    visibleProgressBar = false
                    doEnterTransition()
                    return false
                }
                
                override fun onLoadFailed():Boolean {
                    findViewById<ProgressBar>(R.id.loading).gone()
                    visibleProgressBar = false
                    doEnterTransition()
                    return super.onLoadFailed()
                }
            }
            
            if (it.thumbUrl.equals(it.url, true)) {
                Glide.with(this).load(it.url)
                        .placeholder(d)
                        .error(d)
                        .dontTransform()
                        .dontAnimate()
                        .fitCenter()
                        .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                        .priority(Priority.HIGH)
                        .thumbnail(0.5F)
                        .listener(listener)
                        .into(img)
            } else {
                val thumbnailRequest = Glide.with(this).load(it.thumbUrl)
                        .placeholder(d)
                        .error(d)
                        .dontTransform()
                        .dontAnimate()
                        .fitCenter()
                        .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                        .priority(Priority.IMMEDIATE)
                        .thumbnail(0.5F)
                        .listener(object:GlideRequestListener<GlideDrawable>() {
                            override fun onLoadSucceed(resource:GlideDrawable):Boolean {
                                setupProgressBarColors(resource)
                                findViewById<ProgressBar>(R.id.loading).visible()
                                visibleProgressBar = true
                                doEnterTransition()
                                return false
                            }
                            
                            override fun onLoadFailed():Boolean {
                                findViewById<ProgressBar>(R.id.loading).gone()
                                visibleProgressBar = false
                                doEnterTransition()
                                return super.onLoadFailed()
                            }
                        })
                
                Glide.with(this).load(it.url)
                        .placeholder(d)
                        .error(d)
                        .dontTransform()
                        .dontAnimate()
                        .fitCenter()
                        .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                        .priority(Priority.HIGH)
                        .thumbnail(thumbnailRequest)
                        .listener(listener)
                        .into(img)
            }
        }
    }
    
    private fun doEnterTransition() {
        img.viewTreeObserver.addOnPreDrawListener(object:ViewTreeObserver.OnPreDrawListener {
            override fun onPreDraw():Boolean {
                img.viewTreeObserver.removeOnPreDrawListener(this)
                if (!transitioned) {
                    supportStartPostponedEnterTransition()
                    transitioned = true
                }
                return true
            }
        })
    }
    
    private fun setupProgressBarColors(drw:Drawable?) {
        setupProgressBarColors(drw?.toBitmap())
    }
    
    private fun setupProgressBarColors(bmp:Bitmap?) {
        val color = getActiveIconsColorFor(bmp?.bestSwatch?.rgb ?: cardBackgroundColor)
        findViewById<ProgressBar>(R.id.loading).indeterminateDrawable.applyColorFilter(color)
    }
    
    override fun onRequestPermissionsResult(requestCode:Int, permissions:Array<out String>,
                                            grantResults:IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 41 || requestCode == 42) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                checkIfFileExists(requestCode == 41)
            } else {
                showSnackbar(R.string.permission_denied)
            }
        }
    }
    
    private fun doItemClick(actionId:Int) {
        when (actionId) {
            DOWNLOAD_ACTION_ID -> downloadWallpaper(false)
            APPLY_ACTION_ID -> downloadWallpaper(true)
            FAVORITE_ACTION_ID -> toggleFavorite()
        }
    }
    
    @SuppressLint("NewApi")
    private fun downloadWallpaper(toApply:Boolean) {
        if (isNetworkAvailable) {
            checkPermission(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    object:PermissionRequestListener {
                        override fun onPermissionRequest(permission:String) =
                                requestPermissions(if (toApply) 41 else 42, permission)
                        
                        override fun showPermissionInformation(permission:String) =
                                showPermissionInformation(toApply)
                        
                        override fun onPermissionCompletelyDenied() =
                                showSnackbar(R.string.permission_denied_completely)
                        
                        override fun onPermissionGranted() = checkIfFileExists(toApply)
                    })
        } else {
            if (toApply) showWallpaperApplyOptions(null)
            else showNotConnectedDialog()
        }
    }
    
    private fun showPermissionInformation(toApply:Boolean) {
        showSnackbar(getString(R.string.permission_request, getAppName()), {
            setAction(R.string.allow, {
                dismiss()
                downloadWallpaper(toApply)
            })
        })
    }
    
    private fun checkIfFileExists(toApply:Boolean) {
        wallpaper?.let {
            properlyCancelDialog()
            val folder = File(framesKonfigs.downloadsFolder)
            folder.mkdirs()
            val extension = it.url.substring(it.url.lastIndexOf("."))
            val fileName = it.name.formatCorrectly()
            var correctExtension = getWallpaperExtension(extension)
            if (toApply) correctExtension = ".temp" + correctExtension
            val dest = File(folder, fileName + correctExtension)
            if (dest.exists()) {
                actionDialog = buildMaterialDialog {
                    content(R.string.file_exists)
                    negativeText(R.string.file_replace)
                    positiveText(R.string.file_create_new)
                    onPositive { _, _ ->
                        val time = getCurrentTimeStamp().formatCorrectly().replace(" ", "_")
                        val newDest = File(folder, fileName + "_" + time + correctExtension)
                        if (toApply) showWallpaperApplyOptions(newDest)
                        else startDownload(newDest)
                    }
                    onNegative { _, _ ->
                        if (toApply) showWallpaperApplyOptions(dest)
                        else startDownload(dest)
                    }
                }
                actionDialog?.show()
            } else {
                if (toApply) showWallpaperApplyOptions(dest)
                else startDownload(dest)
            }
        }
    }
    
    private fun startDownload(dest:File) {
        wallpaper?.let {
            properlyCancelDialog()
            wallActions = WallpaperActionsFragment()
            wallActions?.show(this, it, dest)
        }
    }
    
    fun showWallpaperDownloadedSnackbar(dest:File) {
        sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(dest)))
        runOnUiThread {
            properlyCancelDialog()
            showSnackbar(getString(R.string.download_successful, dest.toString()), {
                setAction(R.string.open, {
                    dest.getUri(this@ViewerActivity)?.let {
                        openWallpaper(it)
                    }
                })
            })
        }
    }
    
    @SuppressLint("SimpleDateFormat")
    private fun getCurrentTimeStamp():String {
        val sdfDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        return sdfDate.format(Date())
    }
    
    private fun getWallpaperExtension(currenExt:String):String {
        val validExtensions = arrayOf(".jpg", ".jpeg", ".png")
        validExtensions.forEach {
            if (currenExt.contains(it, true)) return it
        }
        return ".png"
    }
    
    private fun showWallpaperApplyOptions(dest:File?) {
        properlyCancelDialog()
        actionDialog = buildMaterialDialog {
            title(R.string.apply_to)
            items(getString(R.string.home_screen), getString(R.string.lock_screen),
                  getString(R.string.home_lock_screen))
            itemsCallback { _, _, position, _ ->
                if (dest != null) {
                    applyWallpaper(dest, position == 0, position == 1, position == 2)
                } else {
                    applyBitmapWallpaper(position == 0, position == 1, position == 2)
                }
            }
        }
        actionDialog?.show()
    }
    
    private fun applyBitmapWallpaper(toHomeScreen:Boolean, toLockScreen:Boolean, toBoth:Boolean) {
        wallpaper?.let {
            properlyCancelDialog()
            wallActions = WallpaperActionsFragment()
            wallActions?.show(this, it, img.drawable.toBitmap(config = bestBitmapConfig),
                              toHomeScreen, toLockScreen, toBoth)
        }
    }
    
    private fun applyWallpaper(dest:File, toHomeScreen:Boolean, toLockScreen:Boolean,
                               toBoth:Boolean) {
        wallpaper?.let {
            properlyCancelDialog()
            wallActions = WallpaperActionsFragment()
            wallActions?.show(this, it, dest, toHomeScreen, toLockScreen, toBoth)
        }
    }
    
    fun showWallpaperAppliedSnackbar(toHomeScreen:Boolean, toLockScreen:Boolean, toBoth:Boolean) {
        properlyCancelDialog()
        showSnackbar(getString(R.string.apply_successful,
                               getString(when {
                                             toBoth -> R.string.home_lock_screen
                                             toHomeScreen -> R.string.home_screen
                                             toLockScreen -> R.string.lock_screen
                                             else -> R.string.empty
                                         }).toLowerCase()))
    }
    
    private val ANIMATION_DURATION:Long = 150
    private fun toggleFavorite() = runOnUiThread {
        val favImageView = findViewById<ImageView>(R.id.fav_button)
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
    
    private fun showSnackbar(@StringRes text:Int, settings:Snackbar.() -> Unit = {}) =
            showSnackbar(resources.getString(text), settings)
    
    private fun showSnackbar(text:String, settings:Snackbar.() -> Unit = {}) {
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
            
            snack.view.findViewById<TextView>(R.id.snackbar_text).setTextColor(Color.WHITE)
            
            if (visibleBottomBar) changeBottomBarVisibility(false)
            snack.show()
        }
    }
    
    private fun toggleSystemUI() {
        setSystemUIVisibility(!visibleSystemUI)
    }
    
    private fun setSystemUIVisibility(visible:Boolean) {
        visibleSystemUI = visible
        Handler().post({
                           if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                               window.decorView.systemUiVisibility = if (visibleSystemUI)
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
                           changeBarsVisibility(visibleSystemUI)
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
    
    private fun showNotConnectedDialog() {
        properlyCancelDialog()
        actionDialog = buildMaterialDialog {
            title(R.string.muzei_not_connected_title)
            content(R.string.not_connected_content)
            positiveText(android.R.string.ok)
        }
        actionDialog?.show()
    }
    
    private fun properlyCancelDialog() {
        wallActions?.stopActions()
        wallActions?.dismiss(this)
        wallActions = null
        actionDialog?.dismiss()
        actionDialog = null
    }
}