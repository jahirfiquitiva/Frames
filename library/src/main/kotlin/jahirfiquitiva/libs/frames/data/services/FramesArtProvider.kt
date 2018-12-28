/*
 * Copyright (c) 2018. Jahir Fiquitiva
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
package jahirfiquitiva.libs.frames.data.services

import android.annotation.SuppressLint
import android.content.Intent
import ca.allanwang.kau.utils.isNetworkAvailable
import ca.allanwang.kau.utils.isWifiConnected
import com.google.android.apps.muzei.api.UserCommand
import com.google.android.apps.muzei.api.provider.Artwork
import com.google.android.apps.muzei.api.provider.MuzeiArtProvider
import jahirfiquitiva.libs.frames.R
import jahirfiquitiva.libs.frames.helpers.utils.FramesKonfigs
import jahirfiquitiva.libs.frames.helpers.utils.PLAY_STORE_LINK_PREFIX
import jahirfiquitiva.libs.kext.extensions.getAppName

@SuppressLint("NewApi")
open class FramesArtProvider : MuzeiArtProvider() {
    
    companion object {
        private const val SHARE_COMMAND_ID = 2
    }
    
    private val worker: FramesArtWorker by lazy { FramesArtWorker() }
    
    override fun onLoadRequested(initial: Boolean) {
        val configs = context?.let { FramesKonfigs(it) }
        if (configs?.functionalDashboard == true && context?.isNetworkAvailable == true) {
            if (configs.refreshMuzeiOnWiFiOnly) {
                if (context?.isWifiConnected == true)
                    worker.loadWallpapers(context)
            } else {
                worker.loadWallpapers(context)
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
                        context.getAppName(), PLAY_STORE_LINK_PREFIX + context.packageName))
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            }
        }
    }
    
    override fun onLowMemory() {
        super.onLowMemory()
        worker.destroy()
    }
}