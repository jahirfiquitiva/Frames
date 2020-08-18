package dev.jahir.frames.muzei

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import com.google.android.apps.muzei.api.provider.Artwork
import com.google.android.apps.muzei.api.provider.ProviderContract
import dev.jahir.frames.R
import dev.jahir.frames.data.Preferences
import dev.jahir.frames.data.models.Wallpaper
import dev.jahir.frames.data.viewmodels.WallpapersDataViewModel
import dev.jahir.frames.extensions.context.string
import dev.jahir.frames.extensions.resources.hasContent

open class FramesArtWorker : LifecycleOwner {

    private val lcRegistry by lazy { LifecycleRegistry(this) }
    override fun getLifecycle(): LifecycleRegistry = lcRegistry

    open var viewModel: WallpapersDataViewModel? = null

    open fun initViewModel(context: Context?): WallpapersDataViewModel? = try {
        WallpapersDataViewModel((context?.applicationContext as Application))
    } catch (e: Exception) {
        null
    }

    open fun loadWallpapers(context: Context?, prefs: Preferences?) {
        context ?: return
        prefs ?: return

        destroy()
        if (viewModel == null) viewModel = initViewModel(context)
        val selectedCollections = getSelectedCollections(prefs)
        val shouldLoadFavs = selectedCollections.any { it.equals("favorites", true) }

        viewModel?.whenReady = {
            val wallpapers: ArrayList<Wallpaper> =
                ArrayList(if (shouldLoadFavs) viewModel?.favorites.orEmpty() else listOf())
            val collections = if (selectedCollections.isNotEmpty()) {
                viewModel?.collections?.filter {
                    selectedCollections.any { co -> co.equals(it.displayName, true) }
                }
            } else viewModel?.collections.orEmpty()
            collections?.forEach { wallpapers.addAll(it.wallpapers) }
            postWallpapers(context, wallpapers)
        }
        viewModel?.loadData(context.string(R.string.json_url), true, shouldLoadFavs)
    }

    private fun getSelectedCollections(prefs: Preferences): List<String> =
        prefs.muzeiCollections.split(",").map { it.trim() }.distinct()

    private fun postWallpapers(context: Context, wallpapers: ArrayList<Wallpaper>) {
        val client: String by lazy { "${context.packageName}.muzei" }
        val providerClient = ProviderContract.getProviderClient(context, client)
        providerClient.addArtwork(wallpapers.map { wallpaper ->
            Artwork(
                token = wallpaper.url,
                title = wallpaper.name,
                byline = wallpaper.author,
                attribution = if (wallpaper.copyright.hasContent()) wallpaper.copyright else wallpaper.author,
                persistentUri = Uri.parse(wallpaper.url),
                webUri = Uri.parse(wallpaper.url),
                metadata = wallpaper.url,
            )
        })
        destroy()
    }

    internal fun destroy(makeNull: Boolean = false) {
        try {
            viewModel?.destroy(this)
            if (makeNull) viewModel = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
