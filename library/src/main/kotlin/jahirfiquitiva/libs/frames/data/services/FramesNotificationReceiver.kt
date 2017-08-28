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
package jahirfiquitiva.libs.frames.data.services

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.support.v4.app.NotificationCompat
import jahirfiquitiva.libs.frames.R
import jahirfiquitiva.libs.frames.helpers.extensions.framesKonfigs
import jahirfiquitiva.libs.frames.providers.viewmodels.WallpapersViewModel
import jahirfiquitiva.libs.kauextensions.extensions.accentColor
import jahirfiquitiva.libs.kauextensions.extensions.getAppName
import jahirfiquitiva.libs.kauextensions.extensions.hasContent
import org.json.JSONArray

abstract class FramesNotificationReceiver:BroadcastReceiver() {
    companion object {
        const val REQUEST_CODE = 123
    }
    
    override fun onReceive(context:Context?, intent:Intent?) {
        context?.let {
            val wallsVM = WallpapersViewModel()
            val prevResponse = it.framesKonfigs.backupJson
            val prevWallsList = if (prevResponse.hasContent()) {
                wallsVM.buildWallpapersListFromJson(JSONArray(prevResponse))
            } else {
                ArrayList()
            }
            wallsVM.items.observeForever({ data ->
                                             postNotification(it, prevWallsList.size,
                                                              data?.size ?: 0)
                                         })
            wallsVM.loadData(it, true)
        }
    }
    
    private fun postNotification(context:Context, prevSize:Int, newSize:Int) {
        if (newSize == 0) return
        if (newSize > prevSize) {
            val newWallpapers = newSize - prevSize
            
            val notificationBuilder = NotificationCompat.Builder(context, notificationChannel())
                    .setSmallIcon(R.drawable.ic_notifications)
                    .setContentTitle(context.getAppName())
                    .setContentText(
                            context.getString(R.string.new_wallpapers_available, newWallpapers))
                    .setAutoCancel(true)
                    .setOngoing(false)
                    .setColor(context.accentColor)
            
            val nIntent = Intent(context, mainActivity())
            nIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            
            val pendingIntent = PendingIntent.getActivity(context, REQUEST_CODE, nIntent,
                                                          PendingIntent.FLAG_ONE_SHOT)
            notificationBuilder.setContentIntent(pendingIntent)
            
            val ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            
            notificationBuilder.setSound(ringtoneUri)
            notificationBuilder.setVibrate(longArrayOf(500, 500))
            
            val notificationManager = context.getSystemService(
                    Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(REQUEST_CODE)
            notificationManager.notify(REQUEST_CODE, notificationBuilder.build())
        }
    }
    
    abstract fun mainActivity():Class<*>
    abstract fun notificationChannel():String
}