package com.wkq.base.activity

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.wkq.base.R
import com.wkq.base.dialog.DialogKit

/**
 * Base Activity for app permission requests.
 */
open class PermissionsActivity : AppCompatActivity() {

    private var permissionType = -1
    private var permissionList = mutableListOf<String>()

    private val requestPermissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val grantedPermissions = mutableListOf<String>()
        val deniedPermissions = mutableListOf<String>()
        val permanentlyDeniedPermissions = mutableListOf<String>()

        permissions.entries.forEach { entry ->
            if (entry.value) {
                grantedPermissions.add(entry.key)
            } else {
                deniedPermissions.add(entry.key)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && isPermissionPermanentlyDenied(entry.key)) {
                    permanentlyDeniedPermissions.add(entry.key)
                }
            }
        }

        when {
            deniedPermissions.isEmpty() -> authorized(permissionType, grantedPermissions)
            permanentlyDeniedPermissions.isNotEmpty() -> showAppSettingsPermissionDialog()
            else -> Toast.makeText(
                this,
                getString(R.string.partial_permission_denied, deniedPermissions.joinToString()),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private val openSettingsLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            val allPermissionsGranted = permissionList.all { isGrantedOne(it) }
            val message = if (allPermissionsGranted) {
                getString(R.string.permissions_granted)
            } else {
                getString(R.string.permissions_not_granted)
            }
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }

    fun requestAppPermissions(type: Int, permissions: List<String>) {
        permissionType = type
        permissionList = permissions.toMutableList()
        requestPermissionsLauncher.launch(permissions.toTypedArray())
    }

    fun isGranted(permissions: List<String>?): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return true
        return permissions?.all { isGrantedOne(it) } ?: false
    }

    private fun isGrantedOne(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
    }

    private fun isPermissionPermanentlyDenied(permission: String): Boolean {
        return !shouldShowRequestPermissionRationale(permission) &&
            ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED
    }

    fun getMediaPermissions(): MutableList<String> {
        return when {
            Build.VERSION.SDK_INT >= 34 -> mutableListOf(
                android.Manifest.permission.READ_MEDIA_IMAGES,
                android.Manifest.permission.READ_MEDIA_VIDEO,
                "android.permission.READ_MEDIA_VISUAL_USER_SELECTED"
            )

            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> mutableListOf(
                android.Manifest.permission.READ_MEDIA_IMAGES,
                android.Manifest.permission.READ_MEDIA_VIDEO
            )

            else -> mutableListOf(
                android.Manifest.permission.READ_EXTERNAL_STORAGE,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        }
    }

    fun openSettingsPage(action: String, configure: (Intent.() -> Unit)? = null) {
        val intent = Intent(action).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            configure?.invoke(this)
        }
        startActivity(intent)
    }

    fun openAppDetailsSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", packageName, null)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        openSettingsLauncher.launch(intent)
    }

    fun openNotificationSettings() {
        DialogKit.permission(
            context = this,
            title = getString(R.string.permission_request_setting_title),
            message = getString(R.string.permissions_granted_setting),
            confirmText = getString(R.string.go_to_settings),
            onConfirm = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    openSettingsPage(Settings.ACTION_APP_NOTIFICATION_SETTINGS) {
                        putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
                    }
                } else {
                    openAppDetailsSettings()
                }
                true
            }
        )
    }

    private fun showAppSettingsPermissionDialog() {
        DialogKit.permission(
            context = this,
            title = getString(R.string.permission_request_title),
            message = getString(R.string.permission_permanently_denied),
            confirmText = getString(R.string.go_to_settings),
            onConfirm = {
                openAppDetailsSettings()
                true
            }
        )
    }

    open fun authorized(permissionType: Int, permissionList: MutableList<String>) = Unit
}
