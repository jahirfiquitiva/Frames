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
package jahirfiquitiva.libs.frames.extensions

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.support.v4.app.ActivityCompat
import ca.allanwang.kau.utils.buildIsMarshmallowAndUp

const val PERMISSION_REQUEST_CODE = 42

fun Context.shouldRequestPermission(which:String):Boolean {
    if (buildIsMarshmallowAndUp) {
        val permissionResult = ActivityCompat.checkSelfPermission(this, which)
        return permissionResult != PackageManager.PERMISSION_GRANTED
    }
    return false
}

fun Activity.requestPermissions(vararg which:String) =
        ActivityCompat.requestPermissions(this, which, PERMISSION_REQUEST_CODE)

@SuppressLint("NewApi")
fun Activity.checkPermission(permission:String, listener:PermissionRequestListener) =
        if (shouldRequestPermission(permission)) {
            // Permission has not been granted
            if (shouldShowRequestPermissionRationale(permission)) {
                // Permission needs some explanation
                listener.showPermissionInformation(permission)
            } else {
                if (!framesKonfigs.storagePermissionRequested) {
                    // Request permission
                    listener.onPermissionRequest(permission)
                } else {
                    // Handle the feature without permission or ask user to manually allow it
                    listener.onPermissionCompletelyDenied()
                }
            }
            framesKonfigs.storagePermissionRequested = true
        } else {
            listener.onPermissionGranted()
        }

interface PermissionRequestListener {
    fun onPermissionRequest(permission:String)
    fun showPermissionInformation(permission:String)
    fun onPermissionCompletelyDenied()
    fun onPermissionGranted()
}