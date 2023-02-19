package dev.jahir.frames.ui.activities.base

import android.Manifest
import android.os.Build
import androidx.annotation.IdRes
import com.fondesa.kpermissions.PermissionStatus
import com.fondesa.kpermissions.extension.permissionsBuilder
import com.google.android.material.snackbar.Snackbar
import dev.jahir.frames.R
import dev.jahir.frames.data.Preferences
import dev.jahir.frames.data.listeners.BasePermissionRequestListener
import dev.jahir.frames.extensions.context.getAppName
import dev.jahir.frames.extensions.context.string
import dev.jahir.frames.extensions.views.snackbar

data class PermissionsResult(
    val storage: Boolean,
    val notifications: Boolean,
)

fun List<PermissionStatus>.getPermissionsResult(): PermissionsResult {
    return PermissionsResult(
        storage = find { it.permission === Manifest.permission.WRITE_EXTERNAL_STORAGE } !== null,
        notifications = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            find {
                it.permission === Manifest.permission.POST_NOTIFICATIONS
            } !== null
        } else {
            true
        },
    )
}

abstract class BasePermissionsRequestActivity<out P : Preferences> : BaseThemedActivity<P>() {

    var currentSnackbar: Snackbar? = null

    private val permissionRequestListener: BasePermissionRequestListener by lazy {
        object : BasePermissionRequestListener {
            override fun onPermissionsGranted(result: List<PermissionStatus>) {
                super.onPermissionsGranted(result)
                internalOnPermissionsGranted(result.firstOrNull()?.permission)
            }

            override fun onPermissionsDenied(result: List<PermissionStatus>) {
                super.onPermissionsDenied(result)
                val permissions = result.getPermissionsResult()
                if (permissions.notifications) return
                // Only show for storage permission
                currentSnackbar = snackbar(
                    if (permissions.storage) R.string.permission_denied
                    else R.string.notifications_permission_denied,
                    Snackbar.LENGTH_LONG,
                    snackbarAnchorId
                )
            }

            override fun onPermissionsPermanentlyDenied(result: List<PermissionStatus>) {
                super.onPermissionsPermanentlyDenied(result)
                val permissions = result.getPermissionsResult()
                currentSnackbar = snackbar(
                    if (permissions.storage) R.string.permission_permanently_denied
                    else R.string.notifications_permission_permanently_denied,
                    Snackbar.LENGTH_LONG,
                    snackbarAnchorId
                )
            }

            override fun onPermissionsShouldShowRationale(result: List<PermissionStatus>) {
                super.onPermissionsShouldShowRationale(result)
                showPermissionRationale(result)
            }
        }
    }

    private val permissionRequest by lazy {
        permissionsBuilder(Manifest.permission.WRITE_EXTERNAL_STORAGE).build()
            .apply { addListener(permissionRequestListener) }
    }

    fun requestStoragePermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) permissionRequest.send()
        else internalOnPermissionsGranted()
    }

    private val notificationsPermissionRequest by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionsBuilder(Manifest.permission.POST_NOTIFICATIONS).build()
                .apply { addListener(permissionRequestListener) }
        } else null
    }

    fun requestNotificationsPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            notificationsPermissionRequest?.send()
    }

    private fun showPermissionRationale(result: List<PermissionStatus>) {
        val permissions = result.getPermissionsResult()
        currentSnackbar = snackbar(
            getPermissionRationaleMessage(permissions), Snackbar.LENGTH_INDEFINITE, snackbarAnchorId
        ) {
            setAction(android.R.string.ok) {
                if (permissions.storage) requestStoragePermission()
                else requestNotificationsPermission()
                dismiss()
            }
        }
    }

    open fun internalOnPermissionsGranted(permission: String? = null) {}

    open fun getPermissionRationaleMessage(permissions: PermissionsResult): String {
        return string(
            if (permissions.storage) R.string.permission_request
            else R.string.notifications_permission_request,
            getAppName()
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            permissionRequest.removeAllListeners()
            notificationsPermissionRequest?.removeAllListeners()
        } catch (_: Exception) {
        }
    }

    @IdRes
    open val snackbarAnchorId: Int = R.id.bottom_navigation
}
