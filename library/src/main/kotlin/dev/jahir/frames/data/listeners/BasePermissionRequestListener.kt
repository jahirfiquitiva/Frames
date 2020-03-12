package dev.jahir.frames.data.listeners

import com.fondesa.kpermissions.PermissionStatus
import com.fondesa.kpermissions.allGranted
import com.fondesa.kpermissions.anyDenied
import com.fondesa.kpermissions.anyPermanentlyDenied
import com.fondesa.kpermissions.anyShouldShowRationale
import com.fondesa.kpermissions.request.PermissionRequest

interface BasePermissionRequestListener : PermissionRequest.Listener {
    override fun onPermissionsResult(result: List<PermissionStatus>) {
        when {
            result.anyDenied() -> onPermissionsDenied(result)
            result.anyPermanentlyDenied() -> onPermissionsPermanentlyDenied(result)
            result.anyShouldShowRationale() -> onPermissionsShouldShowRationale(result)
            result.allGranted() -> onPermissionsGranted(result)
        }
    }

    fun onPermissionsDenied(result: List<PermissionStatus>) {}
    fun onPermissionsPermanentlyDenied(result: List<PermissionStatus>) {}
    fun onPermissionsShouldShowRationale(result: List<PermissionStatus>) {}
    fun onPermissionsGranted(result: List<PermissionStatus>) {}
}