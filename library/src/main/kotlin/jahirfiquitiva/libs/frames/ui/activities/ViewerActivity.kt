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
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.support.design.widget.AppBarLayout
import android.support.design.widget.Snackbar
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
import ca.allanwang.kau.utils.contentView
import ca.allanwang.kau.utils.dpToPx
import ca.allanwang.kau.utils.gone
import ca.allanwang.kau.utils.isNetworkAvailable
import ca.allanwang.kau.utils.navigationBarColor
import ca.allanwang.kau.utils.postDelayed
import ca.allanwang.kau.utils.setMarginBottom
import ca.allanwang.kau.utils.setMarginTop
import ca.allanwang.kau.utils.setPaddingBottom
import ca.allanwang.kau.utils.tint
import ca.allanwang.kau.utils.toast
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import com.bumptech.glide.request.RequestOptions
import jahirfiquitiva.libs.archhelpers.extensions.lazyViewModel
import jahirfiquitiva.libs.frames.R
import jahirfiquitiva.libs.frames.data.models.Wallpaper
import jahirfiquitiva.libs.frames.data.models.WallpaperInfo
import jahirfiquitiva.libs.frames.helpers.extensions.framesPostponeEnterTransition
import jahirfiquitiva.libs.frames.helpers.extensions.setNavBarMargins
import jahirfiquitiva.libs.frames.helpers.extensions.toReadableByteCount
import jahirfiquitiva.libs.frames.helpers.utils.FL
import jahirfiquitiva.libs.frames.helpers.utils.FramesKonfigs
import jahirfiquitiva.libs.frames.helpers.utils.GlideRequestCallback
import jahirfiquitiva.libs.frames.helpers.utils.MIN_TIME
import jahirfiquitiva.libs.frames.ui.activities.base.BaseWallpaperActionsActivity
import jahirfiquitiva.libs.frames.ui.adapters.viewholders.WallpaperDetail
import jahirfiquitiva.libs.frames.ui.fragments.dialogs.InfoBottomSheet
import jahirfiquitiva.libs.frames.ui.fragments.dialogs.WallpaperActionsDialog
import jahirfiquitiva.libs.frames.ui.widgets.CustomToolbar
import jahirfiquitiva.libs.frames.viewmodels.WallpaperInfoViewModel
import jahirfiquitiva.libs.kext.extensions.SimpleAnimationListener
import jahirfiquitiva.libs.kext.extensions.activeIconsColor
import jahirfiquitiva.libs.kext.extensions.applyColorFilter
import jahirfiquitiva.libs.kext.extensions.bind
import jahirfiquitiva.libs.kext.extensions.buildSnackbar
import jahirfiquitiva.libs.kext.extensions.color
import jahirfiquitiva.libs.kext.extensions.compliesWithMinTime
import jahirfiquitiva.libs.kext.extensions.currentRotation
import jahirfiquitiva.libs.kext.extensions.drawable
import jahirfiquitiva.libs.kext.extensions.enableTranslucentStatusBar
import jahirfiquitiva.libs.kext.extensions.generatePalette
import jahirfiquitiva.libs.kext.extensions.getStatusBarHeight
import jahirfiquitiva.libs.kext.extensions.hasContent
import jahirfiquitiva.libs.kext.extensions.isInPortraitMode
import jahirfiquitiva.libs.kext.extensions.isLowRamDevice
import jahirfiquitiva.libs.kext.extensions.navigationBarHeight
import jahirfiquitiva.libs.kext.extensions.toBitmap
import jahirfiquitiva.libs.ziv.ZoomableImageView
import java.io.FileInputStream
import java.util.ArrayList

open class ViewerActivity : BaseWallpaperActionsActivity<FramesKonfigs>() {
    
    companion object {
        private const val FAVORITE_ACTION_ID = 3
        private const val ANIMATION_DURATION = 150L
        private const val CLOSING_KEY = "closing"
        private const val TRANSITIONED_KEY = "transitioned"
        private const val VISIBLE_SYSTEM_UI_KEY = "visible_system_ui"
    }
    
    override val configs: FramesKonfigs by lazy { FramesKonfigs(this) }
    
    override var wallpaper: Wallpaper? = null
    override val allowBitmapApply: Boolean = true
    
    override fun lightTheme(): Int = R.style.ViewerLightTheme
    override fun darkTheme(): Int = R.style.ViewerDarkTheme
    override fun amoledTheme(): Int = R.style.ViewerAmoledTheme
    override fun transparentTheme(): Int = R.style.ViewerTransparentTheme
    
    override fun autoTintStatusBar(): Boolean = false
    override fun autoTintNavigationBar(): Boolean = false
    
    private val appbar: AppBarLayout? by bind(R.id.appbar)
    private val toolbar: CustomToolbar? by bind(R.id.toolbar)
    private val bottomBar: View? by bind(R.id.bottom_bar)
    private val img: ZoomableImageView? by bind(R.id.wallpaper)
    private val loading: ProgressBar? by bind(R.id.loading)
    
    private var isInFavorites = false
    private var hasModifiedFavs = false
    private var showFavoritesButton = false
    
    private var closing = false
    private var transitioned = false
    private var visibleSystemUI = true
    private var visibleBottomBar = true
    
    private val detailsVM: WallpaperInfoViewModel by lazyViewModel()
    private var infoDialog: InfoBottomSheet? = null
    private val details = ArrayList<WallpaperDetail>()
    private var palette: Palette? = null
    private var info: WallpaperInfo? = null
    
    @SuppressLint("MissingSuperCall", "InlinedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        enableTranslucentStatusBar()
        navigationBarColor = Color.parseColor("#66000000")
        
        setContentView(R.layout.activity_viewer)
        framesPostponeEnterTransition()
        
        wallpaper = intent?.getParcelableExtra("wallpaper")
        
        isInFavorites = intent?.getBooleanExtra("inFavorites", false) == true
        showFavoritesButton = intent?.getBooleanExtra("showFavoritesButton", false) == true
        
        toolbar?.setMarginTop(getStatusBarHeight(true))
        toolbar?.bindToActivity(this)
        toolbar?.tint(color(android.R.color.white), false)
        
        val toolbarTitle: TextView? by bind(R.id.toolbar_title)
        val toolbarSubtitle: TextView? by bind(R.id.toolbar_subtitle)
        ViewCompat.setTransitionName(toolbarTitle, intent?.getStringExtra("nameTransition") ?: "")
        ViewCompat.setTransitionName(
            toolbarSubtitle, intent?.getStringExtra("authorTransition") ?: "")
        toolbarTitle?.text = (wallpaper?.name ?: "").trim()
        wallpaper?.author?.let {
            if (it.trim().hasContent()) toolbarSubtitle?.text = it
            else toolbarSubtitle?.gone()
        }
        
        findViewById<View>(R.id.bottom_bar_container).setNavBarMargins().apply {
            if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT && isInPortraitMode)
                setPaddingBottom(50.dpToPx)
        }
        
        setupProgressBarColors()
        
        findViewById<RelativeLayout>(R.id.info_container).setOnClickListener {
            showInfoDialog()
        }
        
        val downloadable = wallpaper?.downloadable == true
        
        val hasChecker = intent?.getBooleanExtra("checker", false) ?: false
        val actuallyComplies = if (hasChecker) compliesWithMinTime(MIN_TIME) else true
        
        if (downloadable && isNetworkAvailable && actuallyComplies) {
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
            val favIcon = drawable(if (isInFavorites) "ic_heart" else "ic_heart_outline")
            val favImageView: ImageView? by bind(R.id.fav_button)
            ViewCompat.setTransitionName(
                favImageView, intent?.getStringExtra("favTransition") ?: "")
            favImageView?.setImageDrawable(favIcon)
            findViewById<RelativeLayout>(R.id.fav_container).setOnClickListener {
                doItemClick(FAVORITE_ACTION_ID)
            }
        } else {
            findViewById<RelativeLayout>(R.id.fav_container).gone()
        }
        
        img?.enableScaleBeyondLimits(false)
        img?.minZoom = 1F
        img?.maxZoom = 2.5F
        img?.setOnSingleTapListener { toggleSystemUI(); true }
        
        ViewCompat.setTransitionName(img, intent?.getStringExtra("imgTransition") ?: "")
        setupWallpaper(wallpaper, true)
        startEnterTransition()
        
        detailsVM.observe(this) { postWallpaperInfo(it) }
        loadWallpaperDetails()
    }
    
    private fun startEnterTransition() {
        if (!transitioned) {
            supportStartPostponedEnterTransition()
            transitioned = true
            setupWallpaper(wallpaper)
        }
    }
    
    @SuppressLint("MissingSuperCall")
    override fun onResume() {
        super.onResume()
        dismissInfoDialog()
        findViewById<View>(R.id.bottom_bar_container).setNavBarMargins()
        setupProgressBarColors()
        loadWallpaperDetails()
    }
    
    override fun doItemClick(actionId: Int) {
        when (actionId) {
            FAVORITE_ACTION_ID -> toggleFavorite()
            else -> super.doItemClick(actionId)
        }
    }
    
    private fun showInfoDialog() {
        dismissInfoDialog()
        infoDialog = InfoBottomSheet.build(details, palette)
        loadExpensiveWallpaperDetails()
        (infoDialog as? InfoBottomSheet)?.show(this)
    }
    
    private fun dismissInfoDialog() {
        infoDialog?.let {
            if (it is InfoBottomSheet) it.animateHide()
            else it.dismiss()
        }
    }
    
    override fun onMultiWindowModeChanged(isInMultiWindowMode: Boolean, newConfig: Configuration?) {
        super.onMultiWindowModeChanged(isInMultiWindowMode, newConfig)
        findViewById<View>(R.id.bottom_bar_container).setNavBarMargins()
    }
    
    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        outState?.putBoolean(CLOSING_KEY, closing)
        outState?.putBoolean(TRANSITIONED_KEY, transitioned)
        outState?.putBoolean(VISIBLE_SYSTEM_UI_KEY, visibleSystemUI)
    }
    
    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        super.onRestoreInstanceState(savedInstanceState)
        setSystemUIVisibility(savedInstanceState?.getBoolean(VISIBLE_SYSTEM_UI_KEY, true) != false)
        this.closing = savedInstanceState?.getBoolean(CLOSING_KEY, false) == true
        this.transitioned = savedInstanceState?.getBoolean(TRANSITIONED_KEY, false) == true
    }
    
    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item?.itemId == android.R.id.home) {
            doFinish()
        }
        return super.onOptionsItemSelected(item)
    }
    
    override fun onBackPressed() {
        val infoVisible = infoDialog?.isVisible == true
        if (infoVisible) dismissInfoDialog()
        else doFinish()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        properlyCancelDialog()
        transitioned = false
    }
    
    private fun doFinish() {
        if (!closing) {
            closing = true
            properlyCancelDialog()
            try {
                img?.setZoom(1F)
            } catch (ignored: Exception) {
            }
            detailsVM.destroy(this)
            
            postDelayed(100) {
                val intent = Intent()
                intent.putExtra("modified", hasModifiedFavs)
                if (hasModifiedFavs) {
                    intent.putExtra("item", wallpaper)
                    intent.putExtra("inFavorites", isInFavorites)
                }
                setResult(10, intent)
                try {
                    supportFinishAfterTransition()
                } catch (e: Exception) {
                    finish()
                }
                overridePendingTransition(0, 0)
            }
        }
    }
    
    private fun setupWallpaper(wallpaper: Wallpaper?, justStart: Boolean = false) {
        var bmp: Bitmap? = null
        val filename = intent?.getStringExtra("image") ?: ""
        if (filename.hasContent()) {
            var stream: FileInputStream? = null
            try {
                stream = openFileInput(filename)
                bmp = BitmapFactory.decodeStream(stream)
            } catch (ignored: Exception) {
            } finally {
                stream?.close()
            }
        }
        
        val drawable = if (bmp != null) {
            BitmapDrawable(resources, bmp)
        } else {
            ColorDrawable(Color.TRANSPARENT)
        }
        postPalette(drawable)
        
        if (justStart) {
            img?.setImageDrawable(drawable)
            return
        }
        
        wallpaper?.let {
            val listener = object : GlideRequestCallback<Drawable>() {
                override fun onLoadSucceed(resource: Drawable): Boolean {
                    img?.setImageDrawable(resource)
                    startEnterTransition()
                    postPalette(resource)
                    return true
                }
                
                override fun onLoadFailed(): Boolean {
                    startEnterTransition()
                    return super.onLoadFailed()
                }
            }
            
            val options = RequestOptions()
                .format(
                    if (isLowRamDevice) DecodeFormat.PREFER_RGB_565
                    else DecodeFormat.PREFER_ARGB_8888)
                .disallowHardwareConfig()
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                .timeout(10000)
                .placeholder(drawable).error(drawable)
                .fitCenter()
            
            img?.let { img ->
                if (it.thumbUrl.equals(it.url, true)) {
                    Glide.with(this)
                        .load(it.url)
                        .apply(options.priority(Priority.HIGH))
                        .transition(withCrossFade(500))
                        .listener(listener)
                        .into(img)
                } else {
                    val thumbnailRequest = Glide.with(this)
                        .load(it.thumbUrl)
                        .apply(options.priority(Priority.IMMEDIATE))
                        .listener(listener)
                    
                    Glide.with(this)
                        .load(it.url)
                        .apply(options.priority(Priority.HIGH))
                        .thumbnail(thumbnailRequest)
                        .transition(withCrossFade(500))
                        .listener(listener)
                        .into(img)
                }
            }
        }
    }
    
    private fun setupProgressBarColors() {
        loading?.indeterminateDrawable?.applyColorFilter(activeIconsColor)
    }
    
    private fun postPalette(drw: Drawable?) {
        try {
            palette = drw?.toBitmap()?.generatePalette()
            updateInfo()
        } catch (e: Exception) {
            FL.e(e.message)
        }
    }
    
    private fun updateInfo() {
        infoDialog?.setDetailsAndPalette(details, palette)
    }
    
    private fun addToDetails(detail: WallpaperDetail) {
        if (!detail.value.hasContent()) return
        val pos = details.indexOf(detail)
        if (pos != -1) {
            details.removeAt(pos)
            details.add(pos, detail)
        } else details.add(detail)
        updateInfo()
    }
    
    private fun loadWallpaperDetails() {
        wallpaper?.let {
            with(it) {
                addToDetails(WallpaperDetail("ic_all_wallpapers", name))
                if (author.hasContent()) addToDetails(WallpaperDetail("ic_person", author))
                if (copyright.hasContent()) addToDetails(WallpaperDetail("ic_copyright", copyright))
                loadExpensiveWallpaperDetails()
            }
        }
    }
    
    private fun loadExpensiveWallpaperDetails() {
        wallpaper?.let {
            if (it.size != 0L) {
                val sizeText = it.size.toReadableByteCount()
                if (sizeText != "-0") addToDetails(WallpaperDetail("ic_size", sizeText))
            }
            if (it.dimensions.hasContent()) {
                addToDetails(WallpaperDetail("ic_dimensions", it.dimensions))
            }
            val isValidInfo = info?.isValid == true
            if (isValidInfo) {
                postWallpaperInfo(info)
                return
            }
            detailsVM.loadData(it)
        }
    }
    
    private fun postWallpaperInfo(it: WallpaperInfo?) {
        val isValid = it?.isValid == true
        
        if (isValid && info != it) {
            val prevSize = wallpaper?.size ?: 0L
            val size = it?.size ?: 0L
            val bytes = size.toReadableByteCount()
            
            if (prevSize <= 0L && bytes != "-0") {
                addToDetails(WallpaperDetail("ic_size", bytes))
                wallpaper?.size = size
            } else {
                addToDetails(WallpaperDetail("ic_size", prevSize.toReadableByteCount()))
            }
            
            val prevDimension = wallpaper?.dimensions ?: ""
            val dimension = it?.dimension?.toString() ?: ""
            
            if (!prevDimension.hasContent() && dimension.hasContent()) {
                addToDetails(WallpaperDetail("ic_dimensions", dimension))
                wallpaper?.dimensions = dimension
            } else {
                addToDetails(WallpaperDetail("ic_dimensions", prevDimension))
            }
            this.info = it
        }
    }
    
    override fun applyBitmapWallpaper(
        toHomeScreen: Boolean, toLockScreen: Boolean, toBoth: Boolean,
        toOtherApp: Boolean
                                     ) {
        wallpaper?.let {
            properlyCancelDialog()
            wallActions = WallpaperActionsDialog.create(
                this, it, img?.drawable?.toBitmap(),
                arrayOf(toHomeScreen, toLockScreen, toBoth, toOtherApp))
            wallActions?.show(this)
        }
    }
    
    private fun toggleFavorite() = runOnUiThread {
        val favImageView: ImageView? by bind(R.id.fav_button)
        val scale = ScaleAnimation(
            1F, 0F, 1F, 0F, Animation.RELATIVE_TO_SELF, 0.5f,
            Animation.RELATIVE_TO_SELF, 0.5f)
        scale.duration = ANIMATION_DURATION
        scale.interpolator = LinearInterpolator()
        scale.setAnimationListener(
            object : SimpleAnimationListener() {
                override fun onEnd(animation: Animation) {
                    super.onEnd(animation)
                    favImageView?.setImageDrawable(
                        this@ViewerActivity.drawable(
                            if (isInFavorites) "ic_heart_outline" else "ic_heart"))
                    val nScale = ScaleAnimation(
                        0F, 1F, 0F, 1F, Animation.RELATIVE_TO_SELF, 0.5f,
                        Animation.RELATIVE_TO_SELF, 0.5f)
                    nScale.duration = ANIMATION_DURATION
                    nScale.interpolator = LinearInterpolator()
                    nScale.setAnimationListener(
                        object : SimpleAnimationListener() {
                            override fun onEnd(animation: Animation) {
                                super.onEnd(animation)
                                onToggleEnd()
                            }
                        })
                    favImageView?.startAnimation(nScale)
                }
            })
        favImageView?.startAnimation(scale)
    }
    
    private fun onToggleEnd() {
        wallpaper?.let {
            showSnackbar(
                getString(
                    if (isInFavorites) R.string.removed_from_favorites
                    else R.string.added_to_favorites,
                    it.name), Snackbar.LENGTH_SHORT)
        }
        hasModifiedFavs = true
        isInFavorites = !isInFavorites
    }
    
    override fun showSnackbar(
        text: String,
        duration: Int,
        defaultToToast: Boolean,
        settings: Snackbar.() -> Unit
                             ) {
        contentView?.let {
            val snack = it.buildSnackbar(text, duration, settings)
            val bottomBarWasVisible = visibleBottomBar
            
            snack.addCallback(
                object : Snackbar.Callback() {
                    override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                        super.onDismissed(transientBottomBar, event)
                        if (bottomBarWasVisible) changeBottomBarVisibility(true)
                    }
                    
                })
            
            var bottomNavBar = 0
            var sideNavBar = 0
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val tabletMode = resources.getBoolean(R.bool.isTablet)
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
            
            snack.view.setPadding(
                snack.view.paddingLeft + extraLeft, snack.view.paddingTop,
                snack.view.paddingRight + extraRight,
                snack.view.paddingBottom + bottomNavBar)
            
            if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT && isInPortraitMode)
                snack.view.setMarginBottom(50.dpToPx)
            
            val snackText = snack.view.findViewById<TextView>(R.id.snackbar_text)
            snackText.setTextColor(Color.WHITE)
            snackText.maxLines = 3
            
            if (visibleBottomBar) changeBottomBarVisibility(false)
            snack.show()
        } ?: { if (defaultToToast) toast(text) }()
    }
    
    private fun toggleSystemUI() {
        setSystemUIVisibility(!visibleSystemUI)
    }
    
    private fun setSystemUIVisibility(visible: Boolean) {
        Handler().post {
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
        }
    }
    
    private fun changeBarsVisibility(show: Boolean) {
        changeAppBarVisibility(show)
        changeBottomBarVisibility(show)
    }
    
    private fun changeAppBarVisibility(show: Boolean) {
        val transY = (if (show) 0 else -(appbar?.height ?: 0)).toFloat()
        appbar?.animate()?.translationY(transY)
            ?.setInterpolator(AccelerateDecelerateInterpolator())
            ?.start()
    }
    
    private fun changeBottomBarVisibility(show: Boolean) {
        val bottomBarParent = bottomBar?.parent as? View ?: return
        visibleBottomBar = show
        val transY = (if (show) 0 else bottomBarParent.height + navigationBarHeight).toFloat()
        bottomBarParent.animate().translationY(transY)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .start()
    }
}