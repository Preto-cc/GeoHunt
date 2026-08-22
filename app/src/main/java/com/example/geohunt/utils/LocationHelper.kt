package com.example.geohunt.utils

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.Build
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.tasks.await

class LocationHelper(context: Context) {
    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(): Result<Location> {
        return try {
            val location = fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                CancellationTokenSource().token
            ).await()

            if (location != null) {
                if (isMock(location)) {
                    Result.failure(Exception("Ubicación falsa detectada"))
                } else {
                    Result.success(location)
                }
            } else {
                Result.failure(Exception("No se pudo obtener la ubicación, compruebe que tiene el GPS activado"))
            }
        } catch (e: Exception) {
            val errorMsg = when {
                e.message?.contains("location settings") == true -> "El GPS está desactivado. Actívelo para continuar."
                e.message?.contains("permission") == true -> "Permiso de ubicación denegado."
                else -> "Error al obtener la ubicación: ${e.localizedMessage ?: "desconocido"}"
            }
            Result.failure(Exception(errorMsg))
        }
    }

    @Suppress("DEPRECATION")
    private fun isMock(location: Location): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            location.isMock
        } else {
            location.isFromMockProvider
        }
    }
}
