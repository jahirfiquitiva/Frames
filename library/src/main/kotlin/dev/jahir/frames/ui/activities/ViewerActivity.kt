package dev.jahir.frames.ui.activities

import android.app.WallpaperManager
import android.content.DialogInterface
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
import android.view.View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
import android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE
import android.view.WindowManager
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.palette.graphics.Palette
import coil.Coil
import coil.api.load
import coil.request.Request
import com.github.chrisbanes.photoview.PhotoView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dev.jahir.frames.R
import dev.jahir.frames.data.models.Wallpaper
import dev.jahir.frames.extensions.MAX_FRAMES_PALETTE_COLORS
import dev.jahir.frames.extensions.asBitmap
import dev.jahir.frames.extensions.bestSwatch
import dev.jahir.frames.extensions.buildAuthorTransitionName
import dev.jahir.frames.extensions.buildImageTransitionName
import dev.jahir.frames.extensions.buildTitleTransitionName
import dev.jahir.frames.extensions.findView
import dev.jahir.frames.extensions.loadFramesPic
import dev.jahir.frames.extensions.setMarginBottom
import dev.jahir.frames.extensions.setMarginTop
import dev.jahir.frames.ui.activities.base.BaseSystemUIVisibilityActivity
import dev.jahir.frames.ui.fragments.WallpaperDetailsFragment
import dev.jahir.frames.ui.fragments.WallpapersFragment
import dev.jahir.frames.utils.tint
import kotlin.math.roundToInt

class ViewerActivity : BaseSystemUIVisibilityActivity() {

    private val toolbar: Toolbar? by findView(R.id.toolbar)
    private val image: AppCompatImageView? by findView(R.id.wallpaper)

    private var transitioned: Boolean = false
    private var closing: Boolean = false
    private var currentWallPosition: Int = 0

    private val detailsFragment: WallpaperDetailsFragment by lazy { WallpaperDetailsFragment.create() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setBackgroundDrawable(null)
        setContentView(R.layout.activity_viewer)

        supportPostponeEnterTransition()

        currentWallPosition = intent?.extras?.getInt(CURRENT_WALL_POSITION, 0) ?: 0

        val wallpaper =
            intent?.extras?.getParcelable<Wallpaper?>(WallpapersFragment.WALLPAPER_EXTRA)

        if (wallpaper == null) {
            finish()
            return
        }

        initFetch(wallpaper)
        detailsFragment.wallpaper = wallpaper

        findViewById<View?>(R.id.toolbar_title)?.let {
            (it as? TextView)?.text = wallpaper.name
            ViewCompat.setTransitionName(
                it,
                wallpaper.buildTitleTransitionName(currentWallPosition)
            )
        }
        findViewById<View?>(R.id.toolbar_subtitle)?.let {
            (it as? TextView)?.text = wallpaper.author
            ViewCompat.setTransitionName(
                it,
                wallpaper.buildAuthorTransitionName(currentWallPosition)
            )
        }
        image?.let {
            ViewCompat.setTransitionName(
                it,
                wallpaper.buildImageTransitionName(currentWallPosition)
            )
        }
        (image as? PhotoView)?.scale = 1.0F
        image?.loadFramesPic(wallpaper.url, wallpaper.thumbnail) { generatePalette(it) }

        setSupportActionBar(toolbar)
        supportActionBar?.let {
            it.setHomeButtonEnabled(true)
            it.setDisplayHomeAsUpEnabled(true)
            it.setDisplayShowHomeEnabled(true)
        }
        initWindow()
        toolbar?.tint(ContextCompat.getColor(this, R.color.white))
        supportStartPostponedEnterTransition()

        image?.setOnClickListener { toggleSystemUI() }

        val isInFavorites =
            intent?.extras?.getBoolean(WallpapersFragment.WALLPAPER_IN_FAVS_EXTRA, false)
                ?: wallpaper.isInFavorites ?: false

        try {
            bottomNavigation?.menu?.findItem(R.id.favorites)?.isChecked = isInFavorites
            bottomNavigation?.selectedItemId = if (isInFavorites) R.id.favorites else R.id.details
        } catch (e: Exception) {
            e.printStackTrace()
        }

        bottomNavigation?.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.details -> {
                    detailsFragment.show(this, "DETAILS_FRAG")
                    false
                }
                R.id.download -> {
                    requestPermission()
                    false
                }
                R.id.apply -> {
                    applyWallpaper(wallpaper)
                    false
                }
                R.id.favorites -> isInFavorites
                else -> false
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(CLOSING_KEY, closing)
        outState.putBoolean(TRANSITIONED_KEY, transitioned)
        outState.putInt(CURRENT_WALL_POSITION, currentWallPosition)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        this.closing = savedInstanceState.getBoolean(CLOSING_KEY, false)
        this.transitioned = savedInstanceState.getBoolean(TRANSITIONED_KEY, false)
        this.currentWallPosition = savedInstanceState.getInt(CURRENT_WALL_POSITION, 0)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            supportFinishAfterTransition()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        supportFinishAfterTransition()
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            val bmp = (image?.drawable as? BitmapDrawable?)?.bitmap
            if (bmp?.isRecycled == false) bmp.recycle()
        } catch (e: Exception) {
        }
        try {
            image?.setImageBitmap(null)
            image?.setImageDrawable(null)
        } catch (e: Exception) {
        }
    }

    private fun generatePalette(drawable: Drawable?) {
        supportStartPostponedEnterTransition()
        (image as? PhotoView)?.scale = 1.0F
        drawable?.asBitmap()?.let { bitmap ->
            Palette.from(bitmap)
                .maximumColorCount((MAX_FRAMES_PALETTE_COLORS * 1.5).roundToInt())
                .generate {
                    setBackgroundColor(it?.bestSwatch?.rgb ?: 0)
                    detailsFragment.palette = it
                }
        } ?: { setBackgroundColor(0) }()
    }

    private fun setBackgroundColor(@ColorInt color: Int = 0) {
        findViewById<View?>(R.id.viewer_root_layout)?.setBackgroundColor(color)
        window.setBackgroundDrawable(ColorDrawable(color))
    }

    private fun initWindow() {
        if (Build.VERSION.SDK_INT >= 21) {
            window.decorView.systemUiVisibility =
                SYSTEM_UI_FLAG_LAYOUT_STABLE or SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                        SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION

            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)

            val params: WindowManager.LayoutParams = window.attributes
            params.flags = params.flags and WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS.inv()
            window.attributes = params

            appbar?.let { appbar ->
                ViewCompat.setOnApplyWindowInsetsListener(appbar) { _, insets ->
                    appbar.setMarginTop(insets.systemWindowInsetTop)
                    insets
                }
            }

            bottomNavigation?.let { bottomNavigation ->
                ViewCompat.setOnApplyWindowInsetsListener(bottomNavigation) { _, insets ->
                    bottomNavigation.setMarginBottom(insets.systemWindowInsetBottom)
                    insets
                }
            }

            window.statusBarColor = ContextCompat.getColor(this, R.color.viewer_bars_colors)
            window.navigationBarColor = ContextCompat.getColor(this, R.color.viewer_bars_colors)
        }
    }

    override fun onPermissionsAccepted(permissions: Array<out String>) {
        startDownload()
    }

    private fun applyWallpaper(wallpaper: Wallpaper?) {
        wallpaper ?: return
        val dialog = MaterialAlertDialogBuilder(this)
            .setMessage("Loading wallpaper…")
            .setCancelable(false)
            .create()
        dialog.setOnShowListener {
            Coil.load(this, wallpaper.url) {
                target {
                    var bmp = try {
                        it.asBitmap()
                    } catch (e: Exception) {
                        null
                    }
                    bmp ?: return@target
                    val wm = WallpaperManager.getInstance(this@ViewerActivity)
                    val result: Int = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        wm.setBitmap(bmp, null, true)
                    } else {
                        wm.setBitmap(bmp)
                        -1
                    }
                    val success = result != 0
                    if (!bmp.isRecycled) bmp.recycle()
                    @Suppress("UNUSED_VALUE")
                    bmp = null
                    dialog.setMessage(if (success) "Success!" else "Error!")
                    dialog.setCancelable(true)
                    dialog.setButton(DialogInterface.BUTTON_POSITIVE, "Ok") { _, _ ->
                        dialog.dismiss()
                    }
                }
                listener(object : Request.Listener {
                    override fun onCancel(data: Any) {
                        super.onCancel(data)
                        dialog.setMessage("Cancel")
                        dialog.setCancelable(true)
                    }

                    override fun onError(data: Any, throwable: Throwable) {
                        super.onError(data, throwable)
                        dialog.setMessage("Error")
                        dialog.setCancelable(true)
                    }

                    override fun onStart(data: Any) {
                        super.onStart(data)
                        dialog.setMessage("Start")
                        dialog.setCancelable(false)
                    }
                })
            }
        }
        dialog.show()
    }

    companion object {
        private const val CLOSING_KEY = "closing"
        private const val TRANSITIONED_KEY = "transitioned"
        internal const val CURRENT_WALL_POSITION = "curr_wall_pos"
    }
}