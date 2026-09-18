package com.maksimowiczm.foodyou.app.infrastructure.android.healthconnect

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.NutritionRecord

/** The single permission this app ever asks Health Connect for. */
val HEALTH_CONNECT_PERMISSIONS: Set<String> =
    setOf(HealthPermission.getWritePermission(NutritionRecord::class))

/**
 * Where the device stands with respect to Health Connect. Health Connect is Android-only and, on
 * API < 34, a separately installed app — every one of these states is real on `minSdk = 28`.
 */
sealed interface HealthConnectAvailability {
    /** The Health Connect APK isn't and can't be installed (unsupported OS/device). */
    data object Unavailable : HealthConnectAvailability

    /** Health Connect exists for this device but isn't installed, or needs updating. */
    data object NeedsInstall : HealthConnectAvailability

    /** Installed, but this app hasn't been granted [HEALTH_CONNECT_PERMISSIONS] yet. */
    data class NeedsPermission(val client: HealthConnectClient) : HealthConnectAvailability

    /** Installed and permitted — safe to write. */
    data class Ready(val client: HealthConnectClient) : HealthConnectAvailability
}

suspend fun checkHealthConnectAvailability(context: Context): HealthConnectAvailability =
    when (HealthConnectClient.getSdkStatus(context)) {
        HealthConnectClient.SDK_UNAVAILABLE -> HealthConnectAvailability.Unavailable
        HealthConnectClient.SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED ->
            HealthConnectAvailability.NeedsInstall

        HealthConnectClient.SDK_AVAILABLE -> {
            val client = HealthConnectClient.getOrCreate(context)
            val granted = client.permissionController.getGrantedPermissions()
            if (granted.containsAll(HEALTH_CONNECT_PERMISSIONS)) {
                HealthConnectAvailability.Ready(client)
            } else {
                HealthConnectAvailability.NeedsPermission(client)
            }
        }

        else -> HealthConnectAvailability.Unavailable
    }

/** The activity-result contract to launch for [HealthConnectAvailability.NeedsPermission]. */
fun healthConnectPermissionContract() =
    PermissionController.createRequestPermissionResultContract()

/** Opens the Play Store listing for Health Connect, for [HealthConnectAvailability.NeedsInstall]. */
fun healthConnectInstallIntent(): Intent =
    Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.google.android.apps.healthdata"))
        .apply { setPackage("com.android.vending") }
