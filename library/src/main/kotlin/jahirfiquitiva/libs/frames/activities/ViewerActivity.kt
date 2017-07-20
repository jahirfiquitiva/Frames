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

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewCompat
import android.support.v7.widget.Toolbar
import android.view.MenuItem
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import ca.allanwang.kau.utils.buildIsLollipopAndUp
import ca.allanwang.kau.utils.gone
import ca.allanwang.kau.utils.statusBarColor
import ca.allanwang.kau.utils.tint
import ca.allanwang.kau.utils.updateTopMargin
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.GlideBitmapDrawable
import com.bumptech.glide.request.animation.GlideAnimation
import com.bumptech.glide.request.target.SimpleTarget
import com.davemorrissey.labs.subscaleview.ImageSource
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import jahirfiquitiva.libs.frames.R
import jahirfiquitiva.libs.frames.extensions.getStatusBarHeight
import jahirfiquitiva.libs.frames.models.Wallpaper
import jahirfiquitiva.libs.kauextensions.activities.ThemedActivity
import jahirfiquitiva.libs.kauextensions.extensions.getColorFromRes
import jahirfiquitiva.libs.kauextensions.extensions.setupStatusBarStyle

class ViewerActivity:ThemedActivity() {

    override fun lightTheme():Int = R.style.ViewerLightTheme
    override fun darkTheme():Int = R.style.ViewerDarkTheme
    override fun amoledTheme():Int = R.style.ViewerAmoledTheme
    override fun transparentTheme():Int = R.style.ViewerClearTheme

    override fun onCreate(savedInstanceState:Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_viewer)

        val wallpaper:Wallpaper? = intent?.getParcelableExtra("wallpaper")

        setupStatusBarStyle(true, false)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN

        statusBarColor = Color.parseColor("#80000000")

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
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

        val image = findViewById<SubsamplingScaleImageView>(R.id.wallpaper)
        ViewCompat.setTransitionName(image, intent?.getStringExtra("imgTransition") ?: "")

        setupWallpaper(image, wallpaper)
    }

    @SuppressLint("NewApi")
    override fun onOptionsItemSelected(item:MenuItem?):Boolean {
        if (item?.itemId == android.R.id.home) {
            if (buildIsLollipopAndUp) {
                finishAfterTransition()
            } else {
                finish()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    fun setupWallpaper(view:SubsamplingScaleImageView, wallpaper:Wallpaper?) {
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
                view.setImage(ImageSource.cachedBitmap(resource))
            }
        }

        wallpaper?.let {
            val thumbRequest = Glide.with(this).load(it.thumbUrl).asBitmap()
                    .placeholder(d)
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE).priority(Priority.IMMEDIATE)
                    .thumbnail(if (it.url.equals(it.thumbUrl, true)) 0.5F else 1F)

            Glide.with(this).load(it.url).asBitmap()
                    .placeholder(d).thumbnail(thumbRequest)
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE).priority(Priority.HIGH)
                    .into(target)
        }
    }

}