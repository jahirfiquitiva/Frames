package dev.jahir.frames.ui.activities

import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.view.View.*
import android.view.ViewGroup
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.updateLayoutParams
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.bottomnavigation.BottomNavigationView
import dev.jahir.frames.R
import dev.jahir.frames.extensions.findView
import dev.jahir.frames.extensions.loadFramesPic
import dev.jahir.frames.ui.fragments.WallpapersFragment
import dev.jahir.frames.utils.tint

class ViewerActivity : AppCompatActivity() {

    private val appbar: AppBarLayout? by findView(R.id.appbar)
    private val toolbar: Toolbar? by findView(R.id.toolbar)
    private val bottomNavigation: BottomNavigationView? by findView(R.id.bottom_bar)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_viewer)
        setSupportActionBar(toolbar)
        supportActionBar?.let {
            it.setHomeButtonEnabled(true)
            it.setDisplayHomeAsUpEnabled(true)
            it.setDisplayShowHomeEnabled(true)
        }
        initWindow()

        val wallpaperName =
            intent?.extras?.getString(WallpapersFragment.WALLPAPER_NAME_EXTRA, "") ?: ""
        val wallpaperAuthor =
            intent?.extras?.getString(WallpapersFragment.WALLPAPER_AUTHOR_EXTRA, "") ?: ""
        val wallpaperUrl =
            intent?.extras?.getString(WallpapersFragment.WALLPAPER_URL_EXTRA, "") ?: ""
        val wallpaperThumb =
            intent?.extras?.getString(WallpapersFragment.WALLPAPER_THUMB_EXTRA, "") ?: ""

        supportActionBar?.title = wallpaperName
        supportActionBar?.subtitle = wallpaperAuthor
        toolbar?.tint(ContextCompat.getColor(this, R.color.white))

        val image: AppCompatImageView? by findView(R.id.wallpaper)
        image?.loadFramesPic(wallpaperUrl, wallpaperThumb) { crossfade(250) }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) finish()
        return super.onOptionsItemSelected(item)
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