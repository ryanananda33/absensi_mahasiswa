package com.pab.absensi_mahasiswa.model

data class User(
    val id: Int,
    val nim_nik: String,
    val nama: String,
    val gender: String?,
    val jalur: String?, // Reguler / Karyawan Malam / Karyawan Sabtu
    val jurusan: String?,
    val angkatan: String?,
    val kelas: String?,
    val semester: String?,
    val role: String
)
