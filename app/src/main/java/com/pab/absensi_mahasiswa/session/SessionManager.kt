package com.pab.absensi_mahasiswa.session

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.provider.Settings

class SessionManager(private val context: Context) {
    private var sharedPreferences: SharedPreferences = context.getSharedPreferences("AbsensiMahasiswa", Context.MODE_PRIVATE)
    private var editor: SharedPreferences.Editor = sharedPreferences.edit()

    fun saveSession(id: Int, nim: String, nama: String, role: String, gender: String? = null, jalur: String? = null, jurusan: String? = null, angkatan: String? = null, kelas: String? = null, semester: String? = null) {
        editor.putInt("ID_USER", id)
        editor.putString("NIM_USER", nim)
        editor.putString("NAMA_USER", nama)
        editor.putString("ROLE_USER", role)
        editor.putString("GENDER_USER", gender)
        editor.putString("JALUR_USER", jalur)
        editor.putString("JURUSAN_USER", jurusan)
        editor.putString("ANGKATAN_USER", angkatan)
        editor.putString("KELAS_USER", kelas)
        editor.putString("SEMESTER_USER", semester)
        editor.putBoolean("IS_LOGGED_IN", true)
        editor.apply()
    }

    @SuppressLint("HardwareIds")
    fun getDeviceId(): String {
        return Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
    }

    fun getUserId(): Int = sharedPreferences.getInt("ID_USER", 0)
    fun getUserNama(): String = sharedPreferences.getString("NAMA_USER", "User") ?: "User"
    fun getUserNim(): String = sharedPreferences.getString("NIM_USER", "-") ?: "-"
    fun getUserRole(): String = sharedPreferences.getString("ROLE_USER", "mahasiswa") ?: "mahasiswa"
    fun getUserJalur(): String = sharedPreferences.getString("JALUR_USER", "Reguler") ?: "Reguler"
    fun getUserJurusan(): String = sharedPreferences.getString("JURUSAN_USER", "-") ?: "-"
    fun getUserAngkatan(): String = sharedPreferences.getString("ANGKATAN_USER", "-") ?: "-"
    fun getUserKelas(): String = sharedPreferences.getString("KELAS_USER", "-") ?: "-"
    fun getUserSemester(): String = sharedPreferences.getString("SEMESTER_USER", "-") ?: "-"
    fun getUserGender(): String = sharedPreferences.getString("GENDER_USER", "-") ?: "-"

    fun isLoggedIn(): Boolean = sharedPreferences.getBoolean("IS_LOGGED_IN", false)

    fun logout() {
        editor.clear()
        editor.apply()
    }
}
