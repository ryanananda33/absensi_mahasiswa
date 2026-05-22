package com.pab.absensi_mahasiswa.model

data class Absen(
    val id: Int,
    val nama: String,
    val matakuliah: String,
    val keterangan: String,
    val foto: String?,
    val waktu_absen: String
)
