package com.pab.absensi_mahasiswa

data class Absen(
    val id: Int,
    val nama: String,
    val matakuliah: String,
    val keterangan: String,
    val foto: String?, // MASTER LEVEL: Wajib ada untuk menampilkan bukti selfie
    val waktu_absen: String
)