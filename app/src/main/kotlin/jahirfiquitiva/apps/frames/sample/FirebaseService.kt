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
package jahirfiquitiva.apps.frames.sample

/* TODO: Remove comment marks to enable
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import jahirfiquitiva.libs.frames.data.services.FramesNotificationPublisher
*/

// TODO: Remove comment marks to enable
class FirebaseService /*: FirebaseMessagingService() {
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        remoteMessage.notification?.let {
            FramesNotificationPublisher.publish {
                id = 0 // Put any number here
                from = this@FirebaseService
                launch = MainActivity::class.java
                channel = "MyNotificationChannel" // Put any channel name here
                content = it.body ?: ""
                data = remoteMessage.data
            }
        }
    }
}
*/