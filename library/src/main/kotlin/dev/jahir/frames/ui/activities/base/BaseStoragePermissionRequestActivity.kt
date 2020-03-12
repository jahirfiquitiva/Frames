package dev.jahir.frames.ui.activities.base

import android.Manifest
import com.fondesa.kpermissions.extension.permissionsBuilder
import com.fondesa.kpermissions.request.runtime.nonce.PermissionNonce
import com.google.android.material.snackbar.Snackbar
import dev.jahir.frames.R
import dev.jahir.frames.extensions.getAppName
import dev.jahir.frames.extensions.showSnackbar
import dev.jahir.frames.data.listeners.BasePermissionRequestListener
import dev.jahir.frames.utils.Prefs

abstract class BaseStoragePermissionRequestActivity<out P : Prefs> : BaseThemedActivity<P>() {

    private val permissionRequestListener: BasePermissionRequestListener by lazy {
        object : BasePermissionRequestListener {
            override fun onPermissionsAccepted(permissions: Array<out String>) {
                super.onPermissionsAccepted(permissions)
                internalOnPermissionsAccepted(permissions)
            }

            override fun onPermissionsDenied(permissions: Array<out String>) {
                super.onPermissionsDenied(permissions)
                showSnackbar(R.string.permission_denied, Snackbar.LENGTH_LONG)
            }

            override fun onPermissionsPermanentlyDenied(permissions: Array<out String>) {
                super.onPermissionsPermanentlyDenied(permissions)
                showSnackbar(R.string.permission_permanently_denied, Snackbar.LENGTH_LONG)
            }

            override fun onPermissionsShouldShowRationale(
                permissions: Array<out String>,
                nonce: PermissionNonce
            ) {
                super.onPermissionsShouldShowRationale(permissions, nonce)
                showSnackbar(
                    getString(R.string.permission_request, getAppName()),
                    Snackbar.LENGTH_LONG
                ) {
                    setAction(android.R.string.ok) { nonce.use() }
                }
            }
        }
    }

    private val permissionRequest by lazy {
        permissionsBuilder(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            .build()
            .apply {
                acceptedListener(permissionRequestListener)
                deniedListener(permissionRequestListener)
                permanentlyDeniedListener(permissionRequestListener)
                rationaleListener(permissionRequestListener)
            }
    }

    internal fun requestPermission() {
        permissionRequest.send()
    }

    open fun internalOnPermissionsAccepted(permissions: Array<out String>) {}

    override fun onDestroy() {
        super.onDestroy()
        try {
            permissionRequest.detachAllListeners()
        } catch (e: Exception) {
        }
    }
}