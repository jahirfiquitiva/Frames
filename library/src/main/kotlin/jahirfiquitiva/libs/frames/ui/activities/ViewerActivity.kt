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
import android.app.WallpaperManager
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.support.annotation.ColorInt
import android.support.annotation.StringRes
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewCompat
import android.support.v7.widget.Toolbar
import android.view.MenuItem
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import ca.allanwang.kau.utils.dpToPx
import ca.allanwang.kau.utils.gone
import ca.allanwang.kau.utils.isColorDark
import ca.allanwang.kau.utils.postDelayed
import ca.allanwang.kau.utils.setMarginBottom
import ca.allanwang.kau.utils.setMarginLeft
import ca.allanwang.kau.utils.setMarginRight
import ca.allanwang.kau.utils.setMarginTop
import ca.allanwang.kau.utils.statusBarColor
import ca.allanwang.kau.utils.tint
import ca.allanwang.kau.utils.toBitmap
import ca.allanwang.kau.utils.visible
import com.afollestad.materialdialogs.MaterialDialog
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.Transition
import com.davemorrissey.labs.subscaleview.ImageSource
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import com.github.rubensousa.floatingtoolbar.FloatingToolbar
import com.github.rubensousa.floatingtoolbar.FloatingToolbarMenuBuilder
import jahirfiquitiva.libs.frames.R
import jahirfiquitiva.libs.frames.data.models.Wallpaper
import jahirfiquitiva.libs.frames.helpers.configs.bestBitmapConfig
import jahirfiquitiva.libs.frames.helpers.extensions.PERMISSION_REQUEST_CODE
import jahirfiquitiva.libs.frames.helpers.extensions.PermissionRequestListener
import jahirfiquitiva.libs.frames.helpers.extensions.adjustToDeviceScreen
import jahirfiquitiva.libs.frames.helpers.extensions.buildMaterialDialog
import jahirfiquitiva.libs.frames.helpers.extensions.buildSnackbar
import jahirfiquitiva.libs.frames.helpers.extensions.checkPermission
import jahirfiquitiva.libs.frames.helpers.extensions.framesKonfigs
import jahirfiquitiva.libs.frames.helpers.extensions.getStatusBarHeight
import jahirfiquitiva.libs.frames.helpers.extensions.navigationBarHeight
import jahirfiquitiva.libs.frames.helpers.extensions.requestPermissions
import jahirfiquitiva.libs.frames.helpers.extensions.setNavBarMargins
import jahirfiquitiva.libs.frames.helpers.utils.GlidePictureDownloader
import jahirfiquitiva.libs.kauextensions.activities.ThemedActivity
import jahirfiquitiva.libs.kauextensions.extensions.accentColor
import jahirfiquitiva.libs.kauextensions.extensions.applyColorFilter
import jahirfiquitiva.libs.kauextensions.extensions.bestSwatch
import jahirfiquitiva.libs.kauextensions.extensions.currentRotation
import jahirfiquitiva.libs.kauextensions.extensions.formatCorrectly
import jahirfiquitiva.libs.kauextensions.extensions.getActiveIconsColorFor
import jahirfiquitiva.libs.kauextensions.extensions.getAppName
import jahirfiquitiva.libs.kauextensions.extensions.getColorFromRes
import jahirfiquitiva.libs.kauextensions.extensions.getDrawable
import jahirfiquitiva.libs.kauextensions.extensions.getRippleColorFor
import jahirfiquitiva.libs.kauextensions.extensions.getUri
import jahirfiquitiva.libs.kauextensions.extensions.hasContent
import jahirfiquitiva.libs.kauextensions.extensions.isColorLight
import jahirfiquitiva.libs.kauextensions.extensions.isInPortraitMode
import jahirfiquitiva.libs.kauextensions.extensions.printError
import jahirfiquitiva.libs.kauextensions.extensions.setOptionIcon
import jahirfiquitiva.libs.kauextensions.extensions.setupStatusBarStyle
import org.jetbrains.anko.contentView
import java.io.File
import java.io.FileOutputStream
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
    
    private lateinit var toolbar:Toolbar
    private lateinit var fab:FloatingActionButton
    private lateinit var floatingToolbar:FloatingToolbar
    
    private var isInFavorites = false
    private var hasModifiedFavs = false
    private var showFavoritesButton = false
    
    private val DOWNLOAD_ITEM_ID = 1
    private val APPLY_ITEM_ID = 2
    private val FAVORITE_ITEM_ID = 3
    
    @ColorInt
    private var toolbarColor:Int = 0
    
    override fun onCreate(savedInstanceState:Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_viewer)
        
        wallpaper = intent?.getParcelableExtra("wallpaper")
        isInFavorites = intent?.getBooleanExtra("inFavorites", false) == true
        showFavoritesButton = intent?.getBooleanExtra("showFavoritesButton", false) == true
        
        setupStatusBarStyle(true, false)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        
        statusBarColor = Color.parseColor("#80000000")
        
        toolbar = findViewById(R.id.toolbar)
        toolbar.setMarginTop(getStatusBarHeight(true))
        
        setSupportActionBar(toolbar)
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        
        toolbar.tint(getColorFromRes(android.R.color.white), false)
        
        ActivityCompat.postponeEnterTransition(this)
        
        val toolbarTitle = findViewById<TextView>(R.id.toolbar_title)
        val toolbarSubtitle = findViewById<TextView>(R.id.toolbar_subtitle)
        ViewCompat.setTransitionName(toolbarTitle, intent?.getStringExtra("nameTransition") ?: "")
        ViewCompat.setTransitionName(toolbarSubtitle,
                                     intent?.getStringExtra("authorTransition") ?: "")
        toolbarTitle.text = wallpaper?.name ?: ""
        toolbarSubtitle.text = wallpaper?.author ?: ""
        
        fab = findViewById(R.id.fab)
        floatingToolbar = findViewById(R.id.floatingToolbar)
        floatingToolbar.enableAutoHide(false)
        
        toolbarColor = accentColor
        setupFabToolbarIcons()
        
        floatingToolbar.attachFab(fab)
        
        val image = findViewById<SubsamplingScaleImageView>(R.id.wallpaper)
        ViewCompat.setTransitionName(image, intent?.getStringExtra("imgTransition") ?: "")
        
        setupWallpaper(image, wallpaper)
        
        floatingToolbar.setClickListener(object:FloatingToolbar.ItemClickListener {
            override fun onItemClick(item:MenuItem?) {
                item?.let { doItemClick(it) }
            }
            
            override fun onItemLongClick(item:MenuItem?) {}
        })
        
        image.setOnClickListener { if (floatingToolbar.isShowing) floatingToolbar.hide() }
        image.setOnTouchListener { _, motionEvent ->
            if (floatingToolbar.isShowing) floatingToolbar.hide()
            return@setOnTouchListener image.onTouchEvent(motionEvent)
        }
    }
    
    override fun onResume() {
        super.onResume()
        try {
            if (currentRotation == 270) toolbar.setMarginLeft(navigationBarHeight)
            else if (currentRotation == 90) toolbar.setMarginRight(navigationBarHeight)
            fab.setNavBarMargins()
            floatingToolbar.setNavBarMargins()
        } catch (ignored:Exception) {
        }
    }
    
    override fun onOptionsItemSelected(item:MenuItem?):Boolean {
        if (item?.itemId == android.R.id.home) {
            doFinish()
        }
        return super.onOptionsItemSelected(item)
    }
    
    override fun onBackPressed() = if (floatingToolbar.isShowing) {
        floatingToolbar.removeMorphListeners()
        floatingToolbar.hide()
    } else {
        doFinish()
    }
    
    private fun doFinish() {
        val intent = Intent()
        intent.putExtra("modified", hasModifiedFavs)
        if (hasModifiedFavs) {
            intent.putExtra("item", wallpaper)
            intent.putExtra("inFavorites", isInFavorites)
        }
        setResult(10, intent)
        properlyCancelDialog()
        ActivityCompat.finishAfterTransition(this)
        overridePendingTransition(0, 0)
    }
    
    private fun setupWallpaper(view:SubsamplingScaleImageView, wallpaper:Wallpaper?) {
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
        
        val d:Drawable
        d = if (bmp != null) {
            BitmapDrawable(resources, bmp)
        } else {
            ColorDrawable(ContextCompat.getColor(this, android.R.color.transparent))
        }
        
        val target = object:SimpleTarget<Bitmap>() {
            override fun onResourceReady(resource:Bitmap?, transition:Transition<in Bitmap>?) {
                setImageInto(resource, view, false)
            }
            
            override fun onLoadStarted(placeholder:Drawable?) {
                super.onLoadStarted(placeholder)
                setImageInto(placeholder?.toBitmap(config = bestBitmapConfig), view, true)
            }
            
            override fun onLoadCleared(placeholder:Drawable?) {
                super.onLoadCleared(placeholder)
                setImageInto(placeholder?.toBitmap(config = bestBitmapConfig), view, false)
            }
            
            override fun onLoadFailed(errorDrawable:Drawable?) {
                super.onLoadFailed(errorDrawable)
                setImageInto(errorDrawable?.toBitmap(config = bestBitmapConfig), view, false)
            }
        }
        
        wallpaper?.let {
            val thumbRequest = Glide.with(this).asBitmap().load(it.thumbUrl)
                    .thumbnail(if (it.url.equals(it.thumbUrl, true)) 0.5F else 1F)
                    .apply(RequestOptions().diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                                   .priority(Priority.IMMEDIATE).placeholder(d)
                                   .dontTransform())
                    .listener(object:RequestListener<Bitmap> {
                        override fun onResourceReady(resource:Bitmap?, model:Any?,
                                                     target:Target<Bitmap>?, dataSource:DataSource?,
                                                     isFirstResource:Boolean):Boolean {
                            setImageInto(resource, view, true)
                            return true
                        }
                        
                        override fun onLoadFailed(e:GlideException?, model:Any?,
                                                  target:Target<Bitmap>?,
                                                  isFirstResource:Boolean):Boolean {
                            setImageInto(d.toBitmap(config = bestBitmapConfig), view, false)
                            return true
                        }
                    })
            
            Glide.with(this).asBitmap().load(it.url).thumbnail(thumbRequest)
                    .apply(RequestOptions().diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                                   .priority(Priority.HIGH).placeholder(d)
                                   .dontTransform())
                    .into(target)
        }
    }
    
    private fun setImageInto(resource:Bitmap?, view:SubsamplingScaleImageView,
                             isThumbnail:Boolean) {
        val progressBar = findViewById<ProgressBar>(R.id.loading)
        if (!isThumbnail) progressBar.gone()
        view.setMinimumScaleType(SubsamplingScaleImageView.SCALE_TYPE_CENTER_CROP)
        resource?.let {
            view.setImage(ImageSource.cachedBitmap(it))
            it.bestSwatch?.let {
                val color = it.rgb
                fab.backgroundTintList = ColorStateList.valueOf(color)
                fab.rippleColor = getRippleColorFor(color)
                floatingToolbar.setBackgroundColor(color)
                floatingToolbar.background = ColorDrawable(color)
                toolbarColor = color
                setupFabToolbarIcons()
                try {
                    if (isThumbnail) {
                        progressBar.indeterminateDrawable.applyColorFilter(color)
                        progressBar.visible()
                    }
                } catch (ignored:Exception) {
                }
            }
        }
        ActivityCompat.startPostponedEnterTransition(this@ViewerActivity)
    }
    
    private fun setupFabToolbarIcons() {
        fab.setImageDrawable(
                "ic_plus".getDrawable(this).tint(getActiveIconsColorFor(toolbarColor, 0.6F)))
        
        val menuBuilder = FloatingToolbarMenuBuilder(this)
        
        val downloadIcon = "ic_download".getDrawable(this).tint(
                getActiveIconsColorFor(toolbarColor, 0.6F))
        wallpaper?.let {
            if (it.downloadable) menuBuilder.addItem(DOWNLOAD_ITEM_ID, downloadIcon,
                                                     R.string.download)
        }
        
        val applyIcon = "ic_apply_wallpaper".getDrawable(this).tint(
                getActiveIconsColorFor(toolbarColor, 0.6F))
        applyIcon.toBitmap(config = bestBitmapConfig)
        menuBuilder.addItem(APPLY_ITEM_ID, applyIcon, R.string.apply)
        
        if (showFavoritesButton) {
            val favIcon = (if (isInFavorites) "ic_heart" else "ic_heart_outline").getDrawable(
                    this).tint(getActiveIconsColorFor(toolbarColor, 0.6F))
            menuBuilder.addItem(FAVORITE_ITEM_ID, favIcon, R.string.favorite)
        }
        
        floatingToolbar.menu = menuBuilder.build()
    }
    
    override fun onRequestPermissionsResult(requestCode:Int, permissions:Array<out String>,
                                            grantResults:IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                checkIfFileExists()
            } else {
                showSnackbar(R.string.permission_denied)
            }
        }
    }
    
    private fun doItemClick(item:MenuItem) {
        when (item.itemId) {
            DOWNLOAD_ITEM_ID -> downloadWallpaper()
            APPLY_ITEM_ID -> showWallpaperApplyOptions()
            FAVORITE_ITEM_ID -> toggleFavorite()
        }
    }
    
    @SuppressLint("NewApi")
    private fun downloadWallpaper() =
            checkPermission(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    object:PermissionRequestListener {
                        override fun onPermissionRequest(permission:String) =
                                requestPermissions(permission)
                        
                        override fun showPermissionInformation(permission:String) =
                                showSnackbar(getString(R.string.permission_request, getAppName()), {
                                    setAction(R.string.allow, { dismiss() })
                                    addCallback(
                                            object:Snackbar.Callback() {
                                                override fun onDismissed(
                                                        transientBottomBar:Snackbar?, event:Int) {
                                                    super.onDismissed(transientBottomBar, event)
                                                    onPermissionRequest(permission)
                                                }
                                            })
                                })
                        
                        override fun onPermissionCompletelyDenied() =
                                showSnackbar(R.string.permission_denied_completely)
                        
                        override fun onPermissionGranted() = checkIfFileExists()
                    })
    
    private fun checkIfFileExists() {
        wallpaper?.let {
            properlyCancelDialog()
            val folder = File(framesKonfigs.downloadsFolder)
            folder.mkdirs()
            val extension = it.url.substring(it.url.lastIndexOf("."))
            val fileName = it.name.formatCorrectly()
            val correctExtension = getWallpaperExtension(extension)
            val dest = File(folder, fileName + correctExtension)
            if (dest.exists()) {
                actionDialog = buildMaterialDialog {
                    content(R.string.file_exists)
                    negativeText(R.string.file_replace)
                    positiveText(R.string.file_create_new)
                    onPositive { _, _ ->
                        val time = getCurrentTimeStamp().replace(" ", "_")
                        startDownload(File(folder, fileName + "_" + time + correctExtension))
                    }
                    onNegative { _, _ ->
                        startDownload(dest)
                    }
                }
                actionDialog?.show()
            } else {
                startDownload(dest)
            }
        }
    }
    
    private fun startDownload(dest:File) {
        wallpaper?.let {
            properlyCancelDialog()
            actionDialog = buildMaterialDialog {
                content(getString(R.string.downloading_wallpaper, it.name))
                progress(true, 0)
                cancelable(false)
            }
            actionDialog?.show()
            GlidePictureDownloader(Glide.with(this),
                                   { result ->
                                       result.success.forEach { _, file ->
                                           val output = FileOutputStream(dest)
                                           output.write(file.readBytes())
                                           output.close()
                                           sendBroadcast(Intent(
                                                   Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                                                   Uri.fromFile(dest)))
                                           runOnUiThread {
                                               properlyCancelDialog()
                                               showSnackbar(
                                                       getString(R.string.download_successful,
                                                                 dest.toString()), {
                                                           setAction(R.string.open, {
                                                               dest.getUri(
                                                                       this@ViewerActivity)?.let {
                                                                   openWallpaper(it)
                                                               }
                                                           })
                                                       })
                                           }
                                       }
                                   }
                                  ).execute(it.url)
        }
    }
    
    private fun openWallpaper(uri:Uri) {
        val intent = Intent()
        intent.action = Intent.ACTION_VIEW
        intent.setDataAndType(uri, "image/*")
        intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        startActivity(intent)
    }
    
    private fun getCurrentTimeStamp():String {
        val sdfDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val now = Date()
        return sdfDate.format(now)
    }
    
    private fun getWallpaperExtension(currenExt:String):String {
        val validExtensions = arrayOf(".jpg", ".jpeg", ".png")
        validExtensions.forEach {
            if (currenExt.contains(it, true)) return it
        }
        return ".png"
    }
    
    private fun showWallpaperApplyOptions() {
        properlyCancelDialog()
        actionDialog = buildMaterialDialog {
            title(R.string.apply_to)
            items(getString(R.string.home_screen), getString(R.string.lock_screen),
                  getString(R.string.home_lock_screen))
            itemsCallback { _, _, position, _ ->
                applyWallpaper(position == 0, position == 1, position == 2)
            }
        }
        actionDialog?.setOnDismissListener { floatingToolbar.hide() }
        actionDialog?.show()
    }
    
    private fun applyWallpaper(toHomescreen:Boolean, toLockscreen:Boolean, toBoth:Boolean) {
        wallpaper?.let {
            postDelayed(10, {
                runOnUiThread {
                    floatingToolbar.hide()
                    properlyCancelDialog()
                    actionDialog = buildMaterialDialog {
                        content(getString(R.string.applying_wallpaper, it.name))
                        progress(true, 0)
                        cancelable(false)
                    }
                    actionDialog?.show()
                }
            })
            val applyTarget = object:SimpleTarget<Bitmap>() {
                override fun onResourceReady(resource:Bitmap?,
                                             glideAnimation:Transition<in Bitmap>?) {
                    resource?.let {
                        val wm = WallpaperManager.getInstance(this@ViewerActivity)
                        val wallpaper = it.adjustToDeviceScreen(this@ViewerActivity)
                        try {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                if (toBoth) {
                                    wm.setBitmap(wallpaper, null, true)
                                } else {
                                    when {
                                        toHomescreen -> wm.setBitmap(wallpaper, null, true,
                                                                     WallpaperManager.FLAG_SYSTEM)
                                        toLockscreen -> wm.setBitmap(wallpaper, null, true,
                                                                     WallpaperManager.FLAG_LOCK)
                                        else -> printError("The unexpected case has happened :O")
                                    }
                                }
                            } else {
                                wm.setBitmap(wallpaper)
                            }
                            properlyCancelDialog()
                            showSnackbar(getString(R.string.apply_successful,
                                                   getString(when {
                                                                 toBoth -> R.string.home_lock_screen
                                                                 toHomescreen -> R.string.home_screen
                                                                 toLockscreen -> R.string.lock_screen
                                                                 else -> R.string.empty
                                                             }).toLowerCase()))
                        } catch (e:Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }
            postDelayed(100, {
                runOnUiThread {
                    Glide.with(this).asBitmap().load(it.url).apply(
                            RequestOptions().diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                                    .priority(Priority.HIGH).dontAnimate().dontTransform())
                            .into(applyTarget)
                }
            })
        }
    }
    
    private fun toggleFavorite() {
        val ftMenu = floatingToolbar.menu
        ftMenu?.setOptionIcon(FAVORITE_ITEM_ID,
                              (if (isInFavorites) "ic_heart_outline" else "ic_heart").getDrawable(
                                      this).tint(getActiveIconsColorFor(toolbarColor, 0.6F)))
        floatingToolbar.menu = ftMenu
        wallpaper?.let {
            showSnackbar(getString(
                    (if (isInFavorites) R.string.removed_from_favorites else R.string.added_to_favorites),
                    it.name))
        }
        hasModifiedFavs = true
        isInFavorites = !isInFavorites
    }
    
    private fun showSnackbar(@StringRes text:Int, settings:Snackbar.() -> Unit = {}) =
            showSnackbar(resources.getString(text), settings)
    
    private fun showSnackbar(text:String, settings:Snackbar.() -> Unit = {}) =
            if (floatingToolbar.isShowing) {
                floatingToolbar.removeMorphListeners()
                
                val morphListener = object:FloatingToolbar.MorphListener {
                    override fun onMorphEnd() {}
                    override fun onMorphStart() {}
                    override fun onUnmorphStart() {}
                    override fun onUnmorphEnd() = justShowSnackbar(text, settings)
                }
                
                floatingToolbar.addMorphListener(morphListener)
                floatingToolbar.hide()
            } else {
                justShowSnackbar(text, settings)
            }
    
    private fun justShowSnackbar(text:String, settings:Snackbar.() -> Unit) {
        contentView?.let {
            val snack = it.buildSnackbar(text, builder = settings)
            
            snack.addCallback(object:Snackbar.Callback() {
                override fun onDismissed(transientBottomBar:Snackbar?, event:Int) {
                    super.onDismissed(transientBottomBar, event)
                    fab.setNavBarMargins()
                    floatingToolbar.removeMorphListeners()
                }
                
                override fun onShown(sb:Snackbar?) {
                    super.onShown(sb)
                    fab.setMarginBottom(16.dpToPx)
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
            
            snack.view.findViewById<TextView>(R.id.snackbar_text)?.setTextColor(Color.WHITE)
            
            val backColor = snack.view.solidColor
            if (backColor.isColorDark) {
                snack.setActionTextColor(
                        if (toolbarColor.isColorLight) toolbarColor else accentColor)
            } else {
                snack.setActionTextColor(
                        if (toolbarColor.isColorDark) toolbarColor else accentColor)
            }
            floatingToolbar.showSnackBar(snack)
        }
    }
    
    private fun properlyCancelDialog() {
        actionDialog?.dismiss()
        actionDialog = null
    }
}