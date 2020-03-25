package dev.jahir.frames.ui.activities.base

import android.Manifest
import com.fondesa.kpermissions.PermissionStatus
import com.fondesa.kpermissions.extension.permissionsBuilder
import com.google.android.material.snackbar.Snackbar
import dev.jahir.frames.R
import dev.jahir.frames.data.listeners.BasePermissionRequestListener
import dev.jahir.frames.extensions.getAppName
import dev.jahir.frames.extensions.showSnackbar
import dev.jahir.frames.utils.Prefs

abstract class BaseStoragePermissionRequestActivity<out P : Prefs> : BaseThemedActivity<P>() {

    private val permissionRequestListener: BasePermissionRequestListener by lazy {
        object : BasePermissionRequestListener {
            override fun onPermissionsGranted(result: List<PermissionStatus>) {
                super.onPermissionsGranted(result)
                internalOnPermissionsGranted(result)
            }

            override fun onPermissionsDenied(result: List<PermissionStatus>) {
                super.onPermissionsDenied(result)
                showSnackbar(R.string.permission_denied, Snackbar.LENGTH_INDEFINITE)
            }

            override fun onPermissionsPermanentlyDenied(result: List<PermissionStatus>) {
                super.onPermissionsPermanentlyDenied(result)
                showSnackbar(R.string.permission_permanently_denied, Snackbar.LENGTH_INDEFINITE)
            }

            override fun onPermissionsShouldShowRationale(result: List<PermissionStatus>) {
                super.onPermissionsShouldShowRationale(result)
                showPermissionRationale()
            }
        }
    }

    private val permissionRequest by lazy {
        permissionsBuilder(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            .build()
            .apply { addListener(permissionRequestListener) }
    }

    fun requestStoragePermission() {
        permissionRequest.send()
    }

    private fun showPermissionRationale() {
        showSnackbar(getPermissionRationaleMessage(), Snackbar.LENGTH_INDEFINITE) {
            setAction(android.R.string.ok) { requestStoragePermission() }
        }
    }

    open fun internalOnPermissionsGranted(result: List<PermissionStatus>) {}

    open fun getPermissionRationaleMessage(): String =
        getString(R.string.permission_request, getAppName())

    override fun onDestroy() {
        super.onDestroy()
        try {
            permissionRequest.removeAllListeners()
        } catch (e: Exception) {
        }
    }
}