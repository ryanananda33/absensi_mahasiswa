package com.pab.absensi_mahasiswa

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    private var sharedPreferences: SharedPreferences = context.getSharedPreferences("AbsensiMahasiswa", Context.MODE_PRIVATE)
    private var editor: SharedPreferences.Editor = sharedPreferences.edit()

    fun saveSession(id: Int, nim: String, nama: String) {
        editor.putInt("ID_USER", id)
        editor.putString("NIM_USER", nim)
        editor.putString("NAMA_USER", nama)
        editor.apply()
    }

    fun getUserId(): Int = sharedPreferences.getInt("ID_USER", 0)
    fun getUserNama(): String? = sharedPreferences.getString("NAMA_USER", "Mahasiswa")
}