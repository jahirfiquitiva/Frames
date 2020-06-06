package dev.jahir.frames.muzei

import android.content.Context
import android.content.Intent
import com.google.android.apps.muzei.api.UserCommand
import com.google.android.apps.muzei.api.provider.Artwork
import com.google.android.apps.muzei.api.provider.MuzeiArtProvider
import dev.jahir.frames.R
import dev.jahir.frames.data.Preferences
import dev.jahir.frames.extensions.context.getAppName
import dev.jahir.frames.extensions.context.isNetworkAvailable
import dev.jahir.frames.extensions.context.isWifiConnected
import dev.jahir.frames.ui.activities.base.BaseLicenseCheckerActivity.Companion.PLAY_STORE_LINK_PREFIX

open class FramesArtProvider : MuzeiArtProvider() {

    open val worker: FramesArtWorker by lazy { FramesArtWorker() }

    open fun getPreferences(context: Context): Preferences = Preferences(context)

    override fun onLoadRequested(initial: Boolean) {
        val prefs = context?.let { getPreferences(it) }
        prefs ?: return
        if (prefs.functionalDashboard && context?.isNetworkAvailable() == true) {
            if (prefs.refreshMuzeiOnWiFiOnly) {
                if (context?.isWifiConnected == true) worker.loadWallpapers(context, prefs)
            } else {
                worker.loadWallpapers(context, prefs)
            }
        }
    }

    override fun getCommands(artwork: Artwork): MutableList<UserCommand> = context?.let {
        arrayListOf(UserCommand(SHARE_COMMAND_ID, it.getString(R.string.share)))
    } ?: super.getCommands(artwork)

    override fun onCommand(artwork: Artwork, id: Int) {
        val context = context ?: return
        when (id) {
            SHARE_COMMAND_ID -> {
                val intent = Intent(Intent.ACTION_SEND)
                intent.type = "text/plain"
                intent.putExtra(
                    Intent.EXTRA_TEXT,
                    context.getString(
                        R.string.share_text, artwork.title.orEmpty(), artwork.byline.orEmpty(),
                        context.getAppName(), PLAY_STORE_LINK_PREFIX + context.packageName
                    )
                )
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            }
        }
    }

    override fun onLowMemory() {
        super.onLowMemory()
        worker.destroy()
    }

    companion object {
        private const val SHARE_COMMAND_ID = 2
    }
}