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

import android.Manifest
import android.annotation.SuppressLint
import android.app.WallpaperManager
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
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
import ca.allanwang.kau.utils.gone
import ca.allanwang.kau.utils.isColorDark
import ca.allanwang.kau.utils.statusBarColor
import ca.allanwang.kau.utils.tint
import ca.allanwang.kau.utils.updateLeftMargin
import ca.allanwang.kau.utils.updateRightMargin
import ca.allanwang.kau.utils.updateTopMargin
import com.afollestad.materialdialogs.MaterialDialog
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.GlideBitmapDrawable
import com.bumptech.glide.request.animation.GlideAnimation
import com.bumptech.glide.request.target.SimpleTarget
import com.davemorrissey.labs.subscaleview.ImageSource
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import com.github.rubensousa.floatingtoolbar.FloatingToolbar
import com.github.rubensousa.floatingtoolbar.FloatingToolbarItem
import jahirfiquitiva.libs.frames.R
import jahirfiquitiva.libs.frames.extensions.*
import jahirfiquitiva.libs.frames.models.Wallpaper
import jahirfiquitiva.libs.frames.utils.GlidePictureDownloader
import jahirfiquitiva.libs.kauextensions.activities.ThemedActivity
import jahirfiquitiva.libs.kauextensions.extensions.*
import org.jetbrains.anko.contentView
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class ViewerActivity:ThemedActivity() {

    override fun lightTheme():Int = R.style.ViewerLightTheme
    override fun darkTheme():Int = R.style.ViewerDarkTheme
    override fun amoledTheme():Int = R.style.ViewerAmoledTheme
    override fun transparentTheme():Int = R.style.ViewerClearTheme

    private var wallpaper:Wallpaper? = null
    private var actionDialog:MaterialDialog? = null

    private lateinit var toolbar:Toolbar
    private lateinit var fab:FloatingActionButton
    private lateinit var floatingToolbar:FloatingToolbar

    private var isInFavorites = false
    private var hasModifiedFavs = false

    private val DOWNLOAD_ITEM_ID = 1
    private val APPLY_ITEM_ID = 2
    private val FAVORITE_ITEM_ID = 3

    @ColorInt
    private var toolbarColor:Int = 0

    override fun onCreate(savedInstanceState:Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_viewer)

        wallpaper = intent?.getParcelableExtra("wallpaper")

        isInFavorites = intent?.getBooleanExtra("inFavorites", false) ?: false

        setupStatusBarStyle(true, false)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN

        statusBarColor = Color.parseColor("#80000000")

        toolbar = findViewById<Toolbar>(R.id.toolbar)
        toolbar.updateTopMargin(getStatusBarHeight(true))

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
            override fun onItemLongClick(item:FloatingToolbarItem?) {
                // Do nothing
            }

            override fun onItemClick(item:FloatingToolbarItem?) {
                item?.let { doItemClick(item) }
            }
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
            if (currentRotation == 270) toolbar.updateLeftMargin(navigationBarHeight)
            else if (currentRotation == 90) toolbar.updateRightMargin(navigationBarHeight)
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

    override fun onBackPressed() {
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
        actionDialog?.dismiss()
        try {
            ActivityCompat.finishAfterTransition(this)
        } catch(ignored:Exception) {
            finish()
        }
    }

    private fun setupWallpaper(view:SubsamplingScaleImageView, wallpaper:Wallpaper?) {
        var bmp:Bitmap? = null
        val filename = intent?.getStringExtra("image") ?: ""
        if (filename.isNotEmpty() && filename.isNotBlank()) {
            try {
                val stream = openFileInput(filename)
                bmp = BitmapFactory.decodeStream(stream)
                stream.close()
            } catch (ignored:Exception) {
            }
        }

        val d:Drawable
        if (bmp != null) {
            d = GlideBitmapDrawable(resources, bmp)
        } else {
            d = ColorDrawable(ContextCompat.getColor(this, android.R.color.transparent))
        }

        val target = object:SimpleTarget<Bitmap>() {
            override fun onResourceReady(resource:Bitmap?,
                                         anim:GlideAnimation<in Bitmap>?) {
                findViewById<ProgressBar>(R.id.loading).gone()
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
                    }
                }
            }
        }

        wallpaper?.let {
            val thumbRequest = Glide.with(this).load(it.thumbUrl).asBitmap()
                    .placeholder(d).thumbnail(if (it.url.equals(it.thumbUrl, true)) 0.5F else 1F)
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE).priority(Priority.IMMEDIATE)

            Glide.with(this).load(it.url).asBitmap()
                    .placeholder(d).thumbnail(thumbRequest)
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE).priority(Priority.HIGH)
                    .into(target)
        }
    }

    private fun setupFabToolbarIcons() {
        fab.setImageDrawable(
                "ic_plus".getDrawable(this).tint(getActiveIconsColorFor(toolbarColor, 0.6F)))
        val list = ArrayList<FloatingToolbarItem>()

        val downloadIcon = "ic_download".getDrawable(this).tint(
                getActiveIconsColorFor(toolbarColor, 0.6F))
        val downloadItem = FloatingToolbarItem(this, DOWNLOAD_ITEM_ID, R.string.download,
                                               downloadIcon)
        wallpaper?.let { if (it.downloadable) list.add(downloadItem) }

        val applyIcon = "ic_apply_wallpaper".getDrawable(this).tint(
                getActiveIconsColorFor(toolbarColor, 0.6F))
        val applyItem = FloatingToolbarItem(this, APPLY_ITEM_ID, R.string.apply, applyIcon)
        list.add(applyItem)

        val favIcon = (if (isInFavorites) "ic_heart" else "ic_heart_outline").getDrawable(
                this).tint(getActiveIconsColorFor(toolbarColor, 0.6F))
        val favItem = FloatingToolbarItem(this, FAVORITE_ITEM_ID, R.string.favorite, favIcon)
        list.add(favItem)

        floatingToolbar.setItems(list)
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

    private fun doItemClick(item:FloatingToolbarItem) {
        when (item.id) {
            DOWNLOAD_ITEM_ID -> downloadWallpaper()
            APPLY_ITEM_ID -> showWallpaperApplyOptions()
            FAVORITE_ITEM_ID -> toggleFavorite()
        }
    }

    @SuppressLint("NewApi")
    private fun downloadWallpaper() {
        checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        object:PermissionRequestListener {
                            override fun onPermissionRequest(permission:String) {
                                requestPermissions(permission)
                            }

                            override fun showPermissionInformation(permission:String) {
                                showSnackbar(getString(R.string.permission_request, getAppName()), {
                                    setAction(R.string.allow, { dismiss() })
                                    addCallback(object:Snackbar.Callback() {
                                        override fun onDismissed(transientBottomBar:Snackbar?,
                                                                 event:Int) {
                                            super.onDismissed(transientBottomBar, event)
                                            onPermissionRequest(permission)
                                        }
                                    })
                                })
                            }

                            override fun onPermissionCompletelyDenied() {
                                showSnackbar(R.string.permission_denied_completely)
                            }

                            override fun onPermissionGranted() {
                                checkIfFileExists()
                            }
                        })
    }

    private fun checkIfFileExists() {
        wallpaper?.let {
            actionDialog?.dismiss()
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
            actionDialog?.dismiss()
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
                                               actionDialog?.dismiss()
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
        val strDate = sdfDate.format(now)
        return strDate
    }

    private fun getWallpaperExtension(currenExt:String):String {
        val validExtensions = arrayOf(".jpg", ".jpeg", ".png")
        validExtensions.forEach {
            if (currenExt.contains(it, true)) return it
        }
        return ".png"
    }

    private fun showWallpaperApplyOptions() {
        actionDialog?.dismiss()
        actionDialog = buildMaterialDialog {
            title(R.string.apply_to)
            items(getString(R.string.home_screen), getString(R.string.lock_screen),
                  getString(R.string.home_lock_screen))
            itemsCallback { _, _, position, _ ->
                applyWallpaper(position == 0, position == 1, position == 2)
            }
        }
        actionDialog?.show()
    }

    private fun applyWallpaper(toHomescreen:Boolean, toLockscreen:Boolean, toBoth:Boolean) {
        wallpaper?.let {
            floatingToolbar.hide()
            actionDialog?.dismiss()
            actionDialog = buildMaterialDialog {
                content(getString(R.string.applying_wallpaper, it.name))
                progress(true, 0)
                cancelable(false)
            }
            actionDialog?.show()
            val applyTarget = object:SimpleTarget<Bitmap>() {
                override fun onResourceReady(resource:Bitmap?,
                                             glideAnimation:GlideAnimation<in Bitmap>?) {
                    resource?.let {
                        val wm = WallpaperManager.getInstance(this@ViewerActivity)
                        val wallpaper = it.adjustToDeviceScreen(this@ViewerActivity)
                        try {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                if (toBoth) {
                                    wm.setBitmap(wallpaper, null, true)
                                } else {
                                    if (toHomescreen) {
                                        wm.setBitmap(wallpaper, null, true,
                                                     WallpaperManager.FLAG_SYSTEM)
                                    } else if (toLockscreen) {
                                        wm.setBitmap(wallpaper, null, true,
                                                     WallpaperManager.FLAG_LOCK)
                                    } else {
                                        printError("The unexpected case has happened :O")
                                    }
                                }
                            } else {
                                wm.setBitmap(wallpaper)
                            }
                            actionDialog?.dismiss()
                            showSnackbar(getString(R.string.apply_successful,
                                                   getString(
                                                           if (toBoth) R.string.home_lock_screen else
                                                               if (toHomescreen) R.string.home_screen else
                                                                   if (toLockscreen) R.string.lock_screen else R.string.empty
                                                            )))
                        } catch (e:Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }
            runOnUiThread {
                Glide.with(this).load(it.url).asBitmap().dontAnimate().diskCacheStrategy(
                        DiskCacheStrategy.SOURCE).into(applyTarget)
            }
        }
    }

    private fun toggleFavorite() {
        if (isInFavorites) {
            isInFavorites = false
            hasModifiedFavs = true
            floatingToolbar.updateItemIcon(FAVORITE_ITEM_ID,
                                           "ic_heart_outline".getDrawable(this).tint(
                                                   getActiveIconsColorFor(toolbarColor, 0.6F)))
        } else {
            isInFavorites = true
            hasModifiedFavs = true
            floatingToolbar.updateItemIcon(FAVORITE_ITEM_ID, "ic_heart".getDrawable(this).tint(
                    getActiveIconsColorFor(toolbarColor, 0.6F)))
        }
    }

    private fun showSnackbar(@StringRes text:Int, settings:Snackbar.() -> Unit = {}) {
        showSnackbar(resources.getString(text), settings)
    }

    private fun showSnackbar(text:String, settings:Snackbar.() -> Unit = {}) {
        if (floatingToolbar.isShowing) {
            floatingToolbar.removeMorphListeners()

            val morphListener = object:FloatingToolbar.MorphListener {
                override fun onMorphEnd() {
                    // Do nothing
                }

                override fun onMorphStart() {
                    // Do nothing
                }

                override fun onUnmorphStart() {
                    // Do nothing
                }

                override fun onUnmorphEnd() {
                    justShowSnackbar(text, settings)
                }
            }

            floatingToolbar.addMorphListener(morphListener)
            floatingToolbar.hide()
        } else {
            justShowSnackbar(text, settings)
        }
    }

    private fun justShowSnackbar(text:String, settings:Snackbar.() -> Unit) {
        contentView?.let {
            fab.hide()

            val snack = it.buildSnackbar(text, builder = settings)

            snack.addCallback(object:Snackbar.Callback() {
                override fun onDismissed(transientBottomBar:Snackbar?,
                                         event:Int) {
                    super.onDismissed(transientBottomBar, event)
                    fab.setNavBarMargins()
                    floatingToolbar.setNavBarMargins()
                    fab.show()
                    floatingToolbar.removeMorphListeners()
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

            snack.view.findViewById<TextView>(R.id.snackbar_text)?.setTextColor(Color.WHITE)
            snack.view.setPadding(snack.view.paddingLeft + extraLeft, snack.view.paddingTop,
                                  snack.view.paddingRight + extraRight,
                                  snack.view.paddingBottom + bottomNavBar)
            val backColor = snack.view.solidColor
            if (backColor.isColorDark) {
                snack.setActionTextColor(
                        if (toolbarColor.isColorLight) toolbarColor else accentColor)
            } else {
                snack.setActionTextColor(
                        if (toolbarColor.isColorDark) toolbarColor else accentColor)
            }
            snack.show()
        }
    }


}