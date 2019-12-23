package dev.jahir.frames.ui.activities

import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.View.*
import android.view.WindowManager
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.palette.graphics.Palette
import com.github.chrisbanes.photoview.PhotoView
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.bottomnavigation.BottomNavigationView
import dev.jahir.frames.R
import dev.jahir.frames.data.models.Wallpaper
import dev.jahir.frames.extensions.*
import dev.jahir.frames.ui.fragments.WallpapersFragment
import dev.jahir.frames.utils.tint

class ViewerActivity : AppCompatActivity() {

    private val appbar: AppBarLayout? by findView(R.id.appbar)
    private val toolbar: Toolbar? by findView(R.id.toolbar)
    private val bottomNavigation: BottomNavigationView? by findView(R.id.bottom_bar)
    private val image: AppCompatImageView? by findView(R.id.wallpaper)

    private var transitioned: Boolean = false
    private var visibleSystemUI: Boolean = true
    private var closing: Boolean = false
    private var currentWallPosition: Int = 0

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
                R.id.favorites -> isInFavorites
                else -> false
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(CLOSING_KEY, closing)
        outState.putBoolean(TRANSITIONED_KEY, transitioned)
        outState.putBoolean(VISIBLE_SYSTEM_UI_KEY, visibleSystemUI)
        outState.putInt(CURRENT_WALL_POSITION, currentWallPosition)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        setSystemUIVisibility(savedInstanceState.getBoolean(VISIBLE_SYSTEM_UI_KEY, true))
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

    private fun generatePalette(drawable: Drawable?) {
        supportStartPostponedEnterTransition()
        (image as? PhotoView)?.scale = 1.0F
        drawable?.asBitmap()?.let { bitmap ->
            Palette.from(bitmap)
                .generate { setBackgroundColor(it?.bestSwatch?.rgb ?: 0) }
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

    private fun toggleSystemUI() {
        setSystemUIVisibility(!visibleSystemUI)
    }

    private fun setSystemUIVisibility(visible: Boolean, withSystemBars: Boolean = true) {
        Handler().post {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && withSystemBars) {
                window.decorView.systemUiVisibility = if (visible)
                    SYSTEM_UI_FLAG_LAYOUT_STABLE or
                            SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                            SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                else
                    SYSTEM_UI_FLAG_LAYOUT_STABLE or
                            SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                            SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                            SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                            SYSTEM_UI_FLAG_FULLSCREEN or
                            SYSTEM_UI_FLAG_IMMERSIVE or
                            SYSTEM_UI_FLAG_IMMERSIVE_STICKY
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
        val extra = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.decorView.rootWindowInsets.systemWindowInsetTop
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) 25.dpToPx
            else 0
        }
        appbar?.setMarginTop(if (show) extra else 0)
        val transY = (if (show) 0 else -(appbar?.height ?: 0 * 3)).toFloat()
        appbar?.animate()?.translationY(transY)
            ?.setInterpolator(AccelerateDecelerateInterpolator())
            ?.withStartAction { if (show) appbar?.visible() }
            ?.withEndAction { if (!show) appbar?.gone() }
            ?.start()
    }

    private fun changeBottomBarVisibility(show: Boolean) {
        val extra = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.decorView.rootWindowInsets.systemWindowInsetBottom
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) 48.dpToPx
            else 0
        }
        val transY = (if (show) 0 else ((bottomNavigation?.height ?: 0 * 2) + extra)).toFloat()
        bottomNavigation?.animate()?.translationY(transY)
            ?.setInterpolator(AccelerateDecelerateInterpolator())
            ?.start()
    }

    companion object {
        private const val CLOSING_KEY = "closing"
        private const val TRANSITIONED_KEY = "transitioned"
        private const val VISIBLE_SYSTEM_UI_KEY = "visible_system_ui"
        internal const val CURRENT_WALL_POSITION = "curr_wall_pos"
    }
}