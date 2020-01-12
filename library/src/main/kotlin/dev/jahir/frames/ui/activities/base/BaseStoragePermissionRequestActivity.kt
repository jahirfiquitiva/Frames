package dev.jahir.frames.ui.activities.base

import android.Manifest
import com.fondesa.kpermissions.extension.permissionsBuilder
import com.fondesa.kpermissions.request.PermissionRequest
import com.fondesa.kpermissions.request.runtime.nonce.PermissionNonce
import com.google.android.material.snackbar.Snackbar
import dev.jahir.frames.R
import dev.jahir.frames.extensions.getAppName
import dev.jahir.frames.extensions.showSnackbar
import dev.jahir.frames.utils.Prefs

abstract class BaseStoragePermissionRequestActivity<out P : Prefs> : BaseThemedActivity<P>(),
    PermissionRequest.AcceptedListener,
    PermissionRequest.DeniedListener,
    PermissionRequest.PermanentlyDeniedListener,
    PermissionRequest.RationaleListener {

    private val permissionRequest by lazy {
        permissionsBuilder(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            .build()
            .apply {
                acceptedListener(this@BaseStoragePermissionRequestActivity)
                deniedListener(this@BaseStoragePermissionRequestActivity)
                permanentlyDeniedListener(this@BaseStoragePermissionRequestActivity)
                rationaleListener(this@BaseStoragePermissionRequestActivity)
            }
    }

    internal fun requestPermission() {
        permissionRequest.send()
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            permissionRequest.detachAllListeners()
        } catch (e: Exception) {
        }
    }

    override fun onPermissionsDenied(permissions: Array<out String>) {
        showSnackbar(R.string.permission_denied, Snackbar.LENGTH_LONG)
    }

    override fun onPermissionsPermanentlyDenied(permissions: Array<out String>) {
        showSnackbar(R.string.permission_permanently_denied, Snackbar.LENGTH_LONG)
    }

    override fun onPermissionsShouldShowRationale(
        permissions: Array<out String>,
        nonce: PermissionNonce
    ) {
        showSnackbar(getString(R.string.permission_request, getAppName()), Snackbar.LENGTH_LONG) {
            setAction(android.R.string.ok) { nonce.use() }
        }
    }
}