package dev.jahir.frames.ui.activities

import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.View.*
import android.view.ViewGroup
import android.view.WindowManager
import androidx.annotation.ColorInt
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.updateLayoutParams
import androidx.palette.graphics.Palette
import com.github.chrisbanes.photoview.PhotoView
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.bottomnavigation.BottomNavigationView
import dev.jahir.frames.R
import dev.jahir.frames.extensions.*
import dev.jahir.frames.ui.fragments.WallpapersFragment
import dev.jahir.frames.utils.tint

class ViewerActivity : AppCompatActivity() {

    private val appbar: AppBarLayout? by findView(R.id.appbar)
    private val toolbar: Toolbar? by findView(R.id.toolbar)
    private val bottomNavigation: BottomNavigationView? by findView(R.id.bottom_bar)
    private val image: AppCompatImageView? by findView(R.id.wallpaper)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setBackgroundDrawable(null)
        setContentView(R.layout.activity_viewer)

        supportPostponeEnterTransition()

        val wallpaperName =
            intent?.extras?.getString(WallpapersFragment.WALLPAPER_NAME_EXTRA, "") ?: ""
        val wallpaperAuthor =
            intent?.extras?.getString(WallpapersFragment.WALLPAPER_AUTHOR_EXTRA, "") ?: ""
        val wallpaperUrl =
            intent?.extras?.getString(WallpapersFragment.WALLPAPER_URL_EXTRA, "") ?: ""
        val wallpaperThumb =
            intent?.extras?.getString(WallpapersFragment.WALLPAPER_THUMB_EXTRA, "") ?: ""

        if (!wallpaperUrl.hasContent()) finish()

        image?.let {
            ViewCompat.setTransitionName(
                it,
                intent?.extras?.getString(WallpapersFragment.WALLPAPER_TRANSITION_EXTRA, "") ?: ""
            )
        }
        (image as? PhotoView)?.scale = 1.0F
        image?.loadFramesPic(wallpaperUrl, wallpaperThumb) { generatePalette(it) }

        supportStartPostponedEnterTransition()

        setSupportActionBar(toolbar)
        supportActionBar?.let {
            it.setHomeButtonEnabled(true)
            it.setDisplayHomeAsUpEnabled(true)
            it.setDisplayShowHomeEnabled(true)
        }
        initWindow()

        supportActionBar?.title = wallpaperName
        supportActionBar?.subtitle = wallpaperAuthor
        toolbar?.tint(ContextCompat.getColor(this, R.color.white))
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            supportFinishAfterTransition()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun generatePalette(drawable: Drawable?) {
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
                    appbar.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                        topMargin = insets.systemWindowInsetTop
                    }
                    insets
                }
            }

            bottomNavigation?.let { bottomNavigation ->
                ViewCompat.setOnApplyWindowInsetsListener(bottomNavigation) { _, insets ->
                    bottomNavigation.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                        bottomMargin = insets.systemWindowInsetBottom
                    }
                    insets
                }
            }

            window.statusBarColor = ContextCompat.getColor(this, R.color.viewer_bars_colors)
            window.navigationBarColor = ContextCompat.getColor(this, R.color.viewer_bars_colors)
        }
    }
}