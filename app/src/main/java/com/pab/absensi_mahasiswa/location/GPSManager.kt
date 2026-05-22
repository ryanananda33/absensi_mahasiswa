package com.pab.absensi_mahasiswa.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Looper
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*

class GPSManager(private val context: Context) {

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    private var lastKnownLocation: Location? = null

    @SuppressLint("MissingPermission")
    fun ambilLokasi(onSuccess: (latitude: Double, longitude: Double) -> Unit, onError: (String) -> Unit) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            onError("Izin lokasi tidak diberikan")
            return
        }

        // MASTER LOGIC: Request Lokasi Baru dengan Akurasi Tinggi (Bukan cuma lokasi lama)
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000)
            .setWaitForAccurateLocation(true)
            .setMaxUpdates(1) // Ambil satu kali saja yang paling akurat
            .build()

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                val location = locationResult.lastLocation ?: return
                
                // DETEKSI ANTI-FAKE GPS (Kritikal)
                val isMock = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    location.isMock
                } else {
                    @Suppress("DEPRECATION")
                    location.isFromMockProvider
                }

                if (isMock) {
                    onError("SISTEM: Lokasi palsu terdeteksi! Gunakan GPS asli.")
                } else {
                    lastKnownLocation = location
                    onSuccess(location.latitude, location.longitude)
                }
            }
        }

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
    }

    fun ambilLatitude(): Double = lastKnownLocation?.latitude ?: 0.0
    fun ambilLongitude(): Double = lastKnownLocation?.longitude ?: 0.0

    fun cekGPSAktif(): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }
}
