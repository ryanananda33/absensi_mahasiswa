package com.pab.absensi_mahasiswa.location

import android.location.Location

class GeofenceManager {

    // KOORDINAT GEDUNG KULIAH TEORI (Contoh)
    private val CLASSROOM_LAT = -6.1754
    private val CLASSROOM_LNG = 106.8272

    // KOORDINAT LABORATORIUM KOMPUTER (Penting untuk Praktikum)
    private val LAB_LAT = -6.1760 
    private val LAB_LNG = 106.8280

    private val MAX_DISTANCE_METERS = 100.0

    fun cekDalamRadius(lat: Double, lng: Double, isPraktikum: Boolean = false): Boolean {
        val results = FloatArray(1)
        val targetLat = if (isPraktikum) LAB_LAT else CLASSROOM_LAT
        val targetLng = if (isPraktikum) LAB_LNG else CLASSROOM_LNG
        
        Location.distanceBetween(lat, lng, targetLat, targetLng, results)
        return results[0] <= MAX_DISTANCE_METERS
    }

    fun hitungJarak(lat: Double, lng: Double, isPraktikum: Boolean = false): Float {
        val results = FloatArray(1)
        val targetLat = if (isPraktikum) LAB_LAT else CLASSROOM_LAT
        val targetLng = if (isPraktikum) LAB_LNG else CLASSROOM_LNG
        
        Location.distanceBetween(lat, lng, targetLat, targetLng, results)
        return results[0]
    }
}
