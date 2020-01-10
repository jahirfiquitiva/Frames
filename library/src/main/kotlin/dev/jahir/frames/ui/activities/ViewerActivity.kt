package dev.jahir.frames.ui.activities

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
import com.github.chrisbanes.photoview.PhotoView
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
import dev.jahir.frames.ui.fragments.SetWallpaperOptionDialog
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
        image?.loadFramesPic(wallpaper.url, wallpaper.thumbnail, true) { generatePalette(it) }

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
        SetWallpaperOptionDialog.show(this, wallpaper)
    }

    companion object {
        private const val CLOSING_KEY = "closing"
        private const val TRANSITIONED_KEY = "transitioned"
        internal const val CURRENT_WALL_POSITION = "curr_wall_pos"
    }
}