package dev.jahir.frames.muzei

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.RemoteActionCompat
import androidx.core.graphics.drawable.IconCompat
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

    private fun createShareAction(artwork: Artwork) = context?.run {
        val title = getString(R.string.share)
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plain"
        intent.putExtra(
            Intent.EXTRA_TEXT,
            getString(
                R.string.share_text,
                artwork.title.orEmpty(),
                artwork.byline.orEmpty(),
                getAppName(),
                PLAY_STORE_LINK_PREFIX + packageName.orEmpty()
            )
        )
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        RemoteActionCompat(
            IconCompat.createWithResource(this, R.drawable.ic_share),
            title, title,
            PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        )
    }

    override fun getCommandActions(artwork: Artwork): List<RemoteActionCompat> {
        context ?: return super.getCommandActions(artwork)
        return listOfNotNull(createShareAction(artwork))
    }

    override fun onLowMemory() {
        super.onLowMemory()
        worker.destroy()
    }
}
