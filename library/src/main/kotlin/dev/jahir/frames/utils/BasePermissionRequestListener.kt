package dev.jahir.frames.utils

import com.fondesa.kpermissions.request.PermissionRequest
import com.fondesa.kpermissions.request.runtime.nonce.PermissionNonce

interface BasePermissionRequestListener : PermissionRequest.AcceptedListener,
    PermissionRequest.DeniedListener,
    PermissionRequest.PermanentlyDeniedListener,
    PermissionRequest.RationaleListener {

    override fun onPermissionsAccepted(permissions: Array<out String>) {
        // Do nothing
    }

    override fun onPermissionsDenied(permissions: Array<out String>) {
        // Do nothing
    }

    override fun onPermissionsPermanentlyDenied(permissions: Array<out String>) {
        // Do nothing
    }

    override fun onPermissionsShouldShowRationale(
        permissions: Array<out String>,
        nonce: PermissionNonce
    ) {
        // Do nothing
    }
}