@file:Suppress("DEPRECATION")

package dev.jahir.frames.ui.activities

import android.Manifest
import android.app.ActivityManager
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.GestureDetector
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
import android.view.View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
import android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE
import android.view.Window
import android.view.WindowManager
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatImageButton
import androidx.appcompat.widget.Toolbar
import androidx.core.graphics.drawable.toDrawable
import androidx.core.view.ViewCompat
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import androidx.palette.graphics.Palette
import coil3.asDrawable
import coil3.dispose
import coil3.executeBlocking
import coil3.imageLoader
import coil3.memory.MemoryCache
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import coil3.size.Scale
import com.google.android.material.navigation.NavigationBarView
import com.google.android.material.transition.platform.MaterialContainerTransform
import com.google.android.material.transition.platform.MaterialContainerTransformSharedElementCallback
import com.ortiz.touchview.TouchImageView
import dev.jahir.frames.R
import dev.jahir.frames.data.Preferences
import dev.jahir.frames.data.models.Wallpaper
import dev.jahir.frames.extensions.context.boolean
import dev.jahir.frames.extensions.context.color
import dev.jahir.frames.extensions.context.compliesWithMinTime
import dev.jahir.frames.extensions.context.findView
import dev.jahir.frames.extensions.context.firstInstallTime
import dev.jahir.frames.extensions.context.isNetworkAvailable
import dev.jahir.frames.extensions.context.isActiveNetworkMetered
import dev.jahir.frames.extensions.context.isWifiConnected
import dev.jahir.frames.extensions.context.navigationBarLight
import dev.jahir.frames.extensions.context.resolveColor
import dev.jahir.frames.extensions.context.statusBarLight
import dev.jahir.frames.extensions.context.string
import dev.jahir.frames.extensions.fragments.mdDialog
import dev.jahir.frames.extensions.fragments.message
import dev.jahir.frames.extensions.fragments.positiveButton
import dev.jahir.frames.extensions.fragments.title
import dev.jahir.frames.extensions.resources.asBitmap
import dev.jahir.frames.extensions.resources.hasContent
import dev.jahir.frames.extensions.resources.toReadableTime
import dev.jahir.frames.extensions.utils.MAX_FRAMES_PALETTE_COLORS
import dev.jahir.frames.extensions.utils.bestSwatch
import dev.jahir.frames.extensions.views.gone
import dev.jahir.frames.extensions.views.loadFramesPic
import dev.jahir.frames.extensions.views.setPaddingBottom
import dev.jahir.frames.extensions.views.setPaddingLeft
import dev.jahir.frames.extensions.views.setPaddingRight
import dev.jahir.frames.extensions.views.setPaddingTop
import dev.jahir.frames.extensions.views.tint
import dev.jahir.frames.extensions.utils.filteredBy
import dev.jahir.frames.extensions.views.visible
import dev.jahir.frames.extensions.views.visibleIf
import dev.jahir.frames.ui.activities.base.BaseWallpaperApplierActivity
import dev.jahir.frames.ui.fragments.WallpapersFragment.Companion.WALLPAPER_EXTRA
import dev.jahir.frames.ui.fragments.viewer.DetailsFragment
import dev.jahir.frames.ui.fragments.viewer.SetAsOptionsDialog
import kotlinx.coroutines.launch

open class ViewerActivity : BaseWallpaperApplierActivity<Preferences>() {

    override val preferences: Preferences by lazy { Preferences(this) }

    private val toolbar: Toolbar? by findView(R.id.toolbar)
    private val imageView: TouchImageView? by findView(R.id.wallpaper)

    private var firstImageLoad: Boolean = true
    private var transitioned: Boolean = false
    private var closing: Boolean = false
    private var favoritesModified: Boolean = false
    private var isInFavorites: Boolean = false
        set(value) {
            field = value
            bottomNavigation?.setSelectedItemId(
                if (value) R.id.favorites else R.id.details,
                false
            )
        }
    private var collectionName: String? = null
    private var isForFavs: Boolean = false
    private var currentWallpaper: Wallpaper? = null
    private var preloadedNeighborsOf: String? = null
    private var searchQuery: String? = null
    private var navigableWallpapers: List<Wallpaper> = emptyList()

    private val detailsFragment: DetailsFragment by lazy {
        DetailsFragment.create(shouldShowPaletteDetails = shouldShowWallpapersPalette())
    }

    private var downloadBlockedDialog: AlertDialog? = null
    private var applierDialog: DialogFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        window.requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS)
        window.decorView.setBackgroundColor(0)
        findViewById<View>(android.R.id.content).transitionName = TRANSITION_NAME
        setEnterSharedElementCallback(MaterialContainerTransformSharedElementCallback())

        window.sharedElementEnterTransition = MaterialContainerTransform().apply {
            addTarget(android.R.id.content)
            duration = ENTER_TRANSITION_DURATION
        }

        window.sharedElementReturnTransition = MaterialContainerTransform().apply {
            addTarget(android.R.id.content)
            duration = RETURN_TRANSITION_DURATION
        }

        super.onCreate(savedInstanceState)
        statusBarLight = false
        navigationBarLight = false
//        window.setFlags(
//            WindowManager.LayoutParams.FLAG_SECURE,
//            WindowManager.LayoutParams.FLAG_SECURE
//        );
        setContentView(R.layout.activity_viewer)
        bottomNavigation?.labelVisibilityMode = NavigationBarView.LABEL_VISIBILITY_LABELED
        // Set before the first frame, so the bar does not change once the wallpaper loads
        (savedInstanceState?.getParcelable<Wallpaper?>(CURRENT_WALLPAPER_KEY)
            ?: intent?.extras?.getParcelable<Wallpaper?>(WALLPAPER_EXTRA))
            ?.let { updateDownloadVisibility(it) }

        setSupportActionBar(toolbar)
        supportActionBar?.let {
            it.setHomeButtonEnabled(true)
            it.setDisplayHomeAsUpEnabled(true)
            it.setDisplayShowHomeEnabled(true)
        }
        initWindow()
        toolbar?.tint(color(R.color.white))

        imageView?.setOnDoubleTapListener(object : GestureDetector.SimpleOnGestureListener() {
            override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                toggleSystemUI()
                return super.onSingleTapConfirmed(e)
            }
        })

        // WALLPAPER SPECIFIC RELATED SETUP ↓
        collectionName = intent?.extras?.getString(CollectionActivity.COLLECTION_NAME_KEY)
        isForFavs = intent?.extras?.getBoolean(IS_FOR_FAVS, false) ?: false
        searchQuery = intent?.extras?.getString(SEARCH_QUERY)

        wallpapersViewModel.observeFavorites(this) {
            this.isInFavorites = it.any { wall -> wall.url == wallpaperDownloadUrl }
            if (isForFavs) updateNavigableWallpapers(it)
        }
        when {
            isForFavs -> {}
            collectionName != null -> wallpapersViewModel.observeCollections(this) { collections ->
                updateNavigableWallpapers(
                    collections.firstOrNull { it.name == collectionName }?.wallpapers.orEmpty()
                )
            }
            else -> wallpapersViewModel.observeWallpapers(this) { updateNavigableWallpapers(it) }
        }

        findViewById<AppCompatImageButton>(R.id.go_previous)?.setOnClickListener {
            configureUIForWallpaper(getAdjacentWallpaper(next = false))
        }
        findViewById<AppCompatImageButton>(R.id.go_next)?.setOnClickListener {
            configureUIForWallpaper(getAdjacentWallpaper(next = true))
        }

        val lastWallpaper = savedInstanceState?.getString(WALLPAPER_URL_KEY)
        val wallpaperFromIntent = intent?.extras?.getParcelable<Wallpaper?>(WALLPAPER_EXTRA)?.url

        loadWallpapersData()
        lifecycleScope.launch {
            configureUIForWallpaper(
                wallpapersViewModel.findWallpaper(
                    lastWallpaper ?: wallpaperFromIntent
                )
            )
        }
    }

    /**
     * Mirrors the list the viewer was opened from: same source, same search filter, same order
     */
    private fun updateNavigableWallpapers(source: List<Wallpaper>) {
        navigableWallpapers = source.filteredBy(searchQuery)
        updateNavigationArrows()
        // The list can arrive after the current image loaded
        if (!firstImageLoad) preloadAdjacentWallpapers()
    }

    private fun currentIndex(): Int =
        navigableWallpapers.indexOfFirst { it.url == wallpaperDownloadUrl }

    private fun updateNavigationArrows() {
        val canNavigate = navigableWallpapers.size > 1 && currentIndex() >= 0
        findViewById<AppCompatImageButton>(R.id.go_previous)?.visibleIf(canNavigate)
        findViewById<AppCompatImageButton>(R.id.go_next)?.visibleIf(canNavigate)
    }

    private fun getAdjacentWallpaper(next: Boolean): Wallpaper? {
        val index = currentIndex()
        if (index < 0) return null
        val size = navigableWallpapers.size
        return navigableWallpapers[(index + (if (next) 1 else -1) + size) % size]
    }

    private fun updateDownloadVisibility(wallpaper: Wallpaper) {
        bottomNavigation?.setItemVisible(
            R.id.download,
            wallpaper.downloadable != false && shouldShowDownloadOption()
        )
    }

    private fun configureUIForWallpaper(wallpaper: Wallpaper?) {
        if (wallpaper == null) {
            finish()
            return
        }

        currentWallpaper = wallpaper
        // Set now, not only in finish(): the return transition reads the result when it starts
        setViewerResult()
        updateDownloadVisibility(wallpaper)

        findViewById<View?>(R.id.toolbar_title)?.let {
            (it as? TextView)?.text = wallpaper.name
        }
        findViewById<View?>(R.id.toolbar_subtitle)?.let {
            (it as? TextView)?.text = wallpaper.author
            it.visibleIf(wallpaper.author.hasContent())
        }

        initWallpaperFetcher(wallpaper)
        detailsFragment.wallpaper = wallpaper
        loadWallpaper(wallpaper)

        // Wallpapers read from the database never have isInFavorites set
        isInFavorites = wallpapersViewModel.favorites.any { it.url == wallpaper.url }

        bottomNavigation?.setOnNavigationItemSelectedListener {
            handleNavigationItemSelected(it.itemId, wallpaper)
        }

        updateNavigationArrows()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(CLOSING_KEY, closing)
        outState.putBoolean(TRANSITIONED_KEY, transitioned)
        outState.putBoolean(IS_IN_FAVORITES_KEY, isInFavorites)
        outState.putBoolean(FAVORITES_MODIFIED, favoritesModified)
        outState.putParcelable(CURRENT_WALLPAPER_KEY, currentWallpaper)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        this.closing = savedInstanceState.getBoolean(CLOSING_KEY, false)
        this.transitioned = savedInstanceState.getBoolean(TRANSITIONED_KEY, false)
        this.isInFavorites = savedInstanceState.getBoolean(IS_IN_FAVORITES_KEY, false)
        this.favoritesModified = savedInstanceState.getBoolean(FAVORITES_MODIFIED, false)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) supportFinishAfterTransition()
        return super.onOptionsItemSelected(item)
    }

    private fun setViewerResult() {
        setResult(
            if (favoritesModified) FAVORITES_MODIFIED_RESULT
            else FAVORITES_NOT_MODIFIED_RESULT,
            Intent().apply {
                putExtra(FAVORITES_MODIFIED, favoritesModified)
                putExtra(CURRENT_WALLPAPER_URL_EXTRA, currentWallpaper?.url)
            }
        )
    }

    override fun finish() {
        imageView?.setZoom(1F)
        setViewerResult()
        super.finish()
    }

    private fun dismissApplierDialog() {
        try {
            applierDialog?.dismiss()
        } catch (_: Exception) {
        }
        applierDialog = null
    }

    private fun dismissDownloadBlockedDialog() {
        try {
            downloadBlockedDialog?.dismiss()
        } catch (_: Exception) {
        }
        downloadBlockedDialog = null
    }

    override fun onDestroy() {
        super.onDestroy()
        dismissApplierDialog()
        dismissDownloadBlockedDialog()
    }

    private fun generatePalette(drawable: Drawable? = null) {
        findViewById<View?>(R.id.loading)?.gone()
        if (!shouldShowWallpapersPalette()) {
            setBackgroundColor()
            return
        }
        (drawable ?: imageView?.drawable)?.asBitmap()?.let { bitmap ->
            Palette.from(bitmap)
                .maximumColorCount(MAX_FRAMES_PALETTE_COLORS * 2)
                .generate {
                    setBackgroundColor(it?.bestSwatch?.rgb ?: 0)
                    detailsFragment.palette = it
                }
        } ?: run {
            setBackgroundColor()
        }
    }

    private fun setBackgroundColor(@ColorInt color: Int? = null) {
        findViewById<View?>(R.id.activity_root_view)?.setBackgroundColor(
            color ?: resolveColor(android.R.attr.colorBackground)
        )
    }

    private fun loadWallpaper(wallpaper: Wallpaper) {
        findViewById<View?>(R.id.loading)?.visible()
        var placeholder: Drawable? = null
        val wallpaperFromIntent = intent?.extras?.getParcelable<Wallpaper?>(WALLPAPER_EXTRA)?.url
        try {
            if (wallpaperFromIntent == wallpaper.url) {
                placeholder = cachedCardImage(wallpaper)
            } else {
                imageView?.dispose()
                placeholder = Color.TRANSPARENT.toDrawable()
                firstImageLoad = true
                setBackgroundColor()
            }
        } catch (_: Exception) {
        }
        imageView?.loadFramesPic(
            wallpaper.url,
            wallpaper.thumbnail,
            placeholder,
            forceLoadFullRes = true,
            cropAsCircle = false,
            saturate = false
        ) { w ->
            if (firstImageLoad) {
                firstImageLoad = false
                imageView?.resetZoomAnimated()
            }
            generatePalette(w)
            preloadAdjacentWallpapers()
        }
    }

    /**
     * The image the grid card was showing, so the transition starts with it. Comes from the
     * memory cache when possible, else from the disk cache, and never from the network.
     */
    private fun cachedCardImage(wallpaper: Wallpaper): Drawable? {
        val keys = listOfNotNull(wallpaper.thumbnail?.takeIf { it.hasContent() }, wallpaper.url)
            .distinct()
        keys.firstNotNullOfOrNull { imageLoader.memoryCache?.get(MemoryCache.Key(it))?.image }
            ?.let { return it.asDrawable(resources) }
        // Only reached without a memory cache hit, e.g. after process death
        val metrics = resources.displayMetrics
        return keys.firstNotNullOfOrNull { key ->
            val request = ImageRequest.Builder(this)
                .data(key)
                .networkCachePolicy(CachePolicy.DISABLED)
                .size(metrics.widthPixels, metrics.heightPixels)
                .build()
            (imageLoader.executeBlocking(request) as? SuccessResult)?.image
        }?.asDrawable(resources)
    }

    /**
     * Loads the previous and next wallpapers at the viewer's size, so an arrow tap shows them
     * from the memory cache. Skipped on metered networks, as they might never be viewed, and on
     * low-RAM devices, as each one can take tens of MB of memory.
     */
    private fun preloadAdjacentWallpapers() {
        val current = currentWallpaper ?: return
        if (preloadedNeighborsOf == current.url || isActiveNetworkMetered()) return
        if (getSystemService(ActivityManager::class.java)?.isLowRamDevice == true) return
        val width = imageView?.width ?: 0
        val height = imageView?.height ?: 0
        if (width <= 0 || height <= 0) return
        val neighbors =
            listOfNotNull(getAdjacentWallpaper(next = true), getAdjacentWallpaper(next = false))
                .filter { it.url != current.url }
                .distinctBy { it.url }
        if (neighbors.isEmpty()) return
        preloadedNeighborsOf = current.url
        neighbors.forEach {
            imageLoader.enqueue(
                ImageRequest.Builder(this)
                    .data(it.url)
                    .size(width, height)
                    // The viewer's TouchImageView loads with FILL. A FIT decode is smaller, and
                    // the memory cache would not use it for the viewer's request
                    .scale(Scale.FILL)
                    .build()
            )
        }
    }

    private fun initWindow() {
        window.decorView.systemUiVisibility =
            SYSTEM_UI_FLAG_LAYOUT_STABLE or SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                    SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION

        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)

        val params: WindowManager.LayoutParams = window.attributes
        params.flags = params.flags and WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS.inv()
        window.attributes = params

        appbar?.let { appbar ->
            ViewCompat.setOnApplyWindowInsetsListener(appbar) { _, insets ->
                appbar.setPaddingTop(insets.systemWindowInsetTop)
                appbar.setPaddingLeft(
                    if (boolean(R.bool.is_landscape)) insets.systemWindowInsetLeft
                    else 0
                )
                appbar.setPaddingRight(
                    if (boolean(R.bool.is_landscape)) insets.systemWindowInsetRight
                    else 0
                )
                insets
            }
        }

        bottomNavigation?.let { bottomNavigation ->
            ViewCompat.setOnApplyWindowInsetsListener(bottomNavigation) { _, insets ->
                bottomNavigation.setPaddingBottom(insets.systemWindowInsetBottom)
                insets
            }
        }

        window.statusBarColor = color(R.color.viewer_bars_colors)
        window.navigationBarColor = color(R.color.viewer_bars_colors)
    }

    open fun handleNavigationItemSelected(itemId: Int, wallpaper: Wallpaper?): Boolean {
        wallpaper ?: return false
        when (itemId) {
            R.id.details -> detailsFragment.show(this, "DETAILS_FRAG")
            R.id.download -> checkForDownload()
            R.id.apply -> applyWallpaper(wallpaper)
            R.id.favorites -> {
                if (canModifyFavorites()) {
                    this.favoritesModified = true
                    if (isInFavorites) removeFromFavorites(wallpaper)
                    else addToFavorites(wallpaper)
                } else onFavoritesLocked()
            }
        }
        return false
    }

    private fun hasValidNetworkAvailable(): Boolean {
        val downloadUsingWiFiOnly = preferences.shouldDownloadOnWiFiOnly
        val isConnected = isNetworkAvailable()
        val usingMobileData = (downloadUsingWiFiOnly && !isWifiConnected) && isConnected
        val shouldShowNetworkDialog = !isConnected || usingMobileData
        if (shouldShowNetworkDialog) {
            dismissDownloadBlockedDialog()
            downloadBlockedDialog = mdDialog {
                title(R.string.error)
                message(
                    if (usingMobileData) R.string.data_error_network_wifi_only
                    else R.string.data_error_network
                )
                positiveButton(android.R.string.ok) { it.dismiss() }
            }
            downloadBlockedDialog?.show()
            return false
        }
        return true
    }

    private fun checkForDownload() {
        if (!shouldShowDownloadOption()) return
        val actuallyComplies =
            if (intent?.getBooleanExtra(LICENSE_CHECK_ENABLED, false) == true)
                compliesWithMinTime(MIN_TIME) || boolean(R.bool.allow_immediate_downloads)
            else true
        if (actuallyComplies) {
            if (!hasValidNetworkAvailable()) return
            requestStoragePermission()
        } else {
            val elapsedTime = System.currentTimeMillis() - firstInstallTime
            val timeLeft = MIN_TIME - elapsedTime
            val timeLeftText = timeLeft.toReadableTime()

            dismissDownloadBlockedDialog()
            downloadBlockedDialog = mdDialog {
                title(R.string.prevent_download_title)
                message(string(R.string.prevent_download_content, timeLeftText))
                positiveButton(android.R.string.ok) { it.dismiss() }
            }
            downloadBlockedDialog?.show()
        }
    }

    override fun internalOnPermissionsGranted(permission: String) {
        super.internalOnPermissionsGranted(permission)
        if (permission == Manifest.permission.WRITE_EXTERNAL_STORAGE)
            startDownload()
    }

    private fun applyWallpaper(wallpaper: Wallpaper?) {
        wallpaper ?: return
        dismissApplierDialog()
        applierDialog = SetAsOptionsDialog()
        applierDialog?.show(supportFragmentManager, SetAsOptionsDialog.TAG)
    }

    private fun shouldShowWallpapersPalette(): Boolean =
        boolean(R.bool.show_wallpaper_palette_details, true)

    open fun shouldShowDownloadOption() = true
    override fun shouldLoadCollections(): Boolean = collectionName != null
    override val shouldChangeStatusBarLightStatus: Boolean = false
    override val shouldChangeNavigationBarLightStatus: Boolean = false

    override fun canToggleSystemUIVisibility(): Boolean =
        intent?.getBooleanExtra(CAN_TOGGLE_SYSTEMUI_VISIBILITY_KEY, true) ?: true

    companion object {
        internal const val MIN_TIME: Long = 3L * 60L * 60000L
        internal const val SEARCH_QUERY = "search_query"
        internal const val CURRENT_WALLPAPER_URL_EXTRA = "current_wallpaper_url"
        internal const val FAVORITES_MODIFIED = "favorites_modified"
        internal const val FAVORITES_MODIFIED_RESULT = 1
        // Not RESULT_CANCELED (0): Android skips onActivityReenter for it, and the list needs that
        // callback to return the transition to the wallpaper the viewer ended on
        internal const val FAVORITES_NOT_MODIFIED_RESULT = RESULT_OK
        internal const val LICENSE_CHECK_ENABLED = "license_check_enabled"
        internal const val CAN_TOGGLE_SYSTEMUI_VISIBILITY_KEY = "can_toggle_visibility"
        internal const val TRANSITION_NAME = "wallpaper_transition_container"
        internal const val IS_FOR_FAVS = "viewer_is_for_favs"
        private const val ENTER_TRANSITION_DURATION = 300L
        private const val RETURN_TRANSITION_DURATION = 250L
        private const val CLOSING_KEY = "closing"
        private const val TRANSITIONED_KEY = "transitioned"
        private const val IS_IN_FAVORITES_KEY = "is_in_favorites"
        private const val CURRENT_WALLPAPER_KEY = "current_wallpaper"
    }
}
