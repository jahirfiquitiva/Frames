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
import android.arch.lifecycle.LifecycleRegistry
import android.arch.lifecycle.LifecycleRegistryOwner
import android.arch.lifecycle.Observer
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.support.v4.app.NotificationCompat
import android.util.Log
import jahirfiquitiva.libs.frames.R
import jahirfiquitiva.libs.frames.helpers.extensions.framesKonfigs
import jahirfiquitiva.libs.frames.providers.viewmodels.WallpapersViewModel
import jahirfiquitiva.libs.kauextensions.extensions.accentColor
import jahirfiquitiva.libs.kauextensions.extensions.getAppName
import jahirfiquitiva.libs.kauextensions.extensions.hasContent
import jahirfiquitiva.libs.kauextensions.extensions.printInfo
import org.json.JSONArray

abstract class FramesNotificationReceiver:BroadcastReceiver(), LifecycleRegistryOwner {
    
    override fun getLifecycle():LifecycleRegistry = LifecycleRegistry(this)
    
    private val wallsVM = WallpapersViewModel()
    
    companion object {
        const val REQUEST_CODE = 123
    }
    
    override fun onReceive(context:Context?, intent:Intent?) {
        Log.d("FramesReceiver", "Started receiving notifications")
        context?.let {
            it.printInfo("Context was not null")
            val prevResponse = it.framesKonfigs.backupJson
            val prevWallsList = if (prevResponse.hasContent()) {
                wallsVM.buildWallpapersListFromJson(JSONArray(prevResponse))
            } else {
                ArrayList()
            }
            val fContext = it
            wallsVM.items.observe(this, Observer { data ->
                data?.let {
                    fContext.printInfo(
                            "Received new wallpapers:\nNew amount: ${it.size} - Previous amount: ${prevWallsList.size}")
                    // TODO: Change >= to >
                    if (it.size >= prevWallsList.size) {
                        val newWallpapers = it.size - prevWallsList.size
                        
                        val notificationBuilder = NotificationCompat.Builder(fContext,
                                                                             notificationChannel())
                                .setSmallIcon(R.drawable.ic_notifications)
                                .setContentTitle(fContext.getAppName())
                                .setContentText(
                                        fContext.getString(R.string.new_wallpapers_available,
                                                           newWallpapers))
                                .setAutoCancel(true)
                                .setOngoing(false)
                                .setColor(fContext.accentColor)
                        
                        val nIntent = Intent(fContext, mainActivity())
                        nIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        
                        val pendingIntent = PendingIntent.getActivity(fContext,
                                                                      REQUEST_CODE,
                                                                      nIntent,
                                                                      PendingIntent.FLAG_ONE_SHOT)
                        notificationBuilder.setContentIntent(pendingIntent)
                        
                        val ringtoneUri = RingtoneManager.getDefaultUri(
                                RingtoneManager.TYPE_NOTIFICATION)
                        
                        notificationBuilder.setSound(ringtoneUri)
                        notificationBuilder.setVibrate(longArrayOf(500, 500))
                        
                        val notificationManager = fContext.getSystemService(
                                Context.NOTIFICATION_SERVICE) as NotificationManager
                        
                        notificationManager.notify(REQUEST_CODE, notificationBuilder.build())
                    }
                }
            })
            it.printInfo("Started loading data")
            wallsVM.loadData(it, true)
        }
    }
    
    abstract fun mainActivity():Class<*>
    abstract fun notificationChannel():String
}