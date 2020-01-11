package dev.jahir.frames.ui.activities.base

import android.Manifest
import androidx.appcompat.app.AlertDialog
import com.fondesa.kpermissions.extension.permissionsBuilder
import com.fondesa.kpermissions.request.PermissionRequest
import com.fondesa.kpermissions.request.runtime.nonce.PermissionNonce
import com.google.android.material.snackbar.Snackbar
import dev.jahir.frames.R
import dev.jahir.frames.extensions.showSnackbar

abstract class BaseStoragePermissionRequestActivity : ThemedActivity(),
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
        // TODO: Show rationale properly
        AlertDialog.Builder(this)
            .setTitle("Request again the permissions")
            .setPositiveButton(android.R.string.ok) { _, _ ->
                // Use the nonce when the user presses on the positive button.
                // By default, the [permissions] are requested again.
                nonce.use()
            }
            .show()
    }

}