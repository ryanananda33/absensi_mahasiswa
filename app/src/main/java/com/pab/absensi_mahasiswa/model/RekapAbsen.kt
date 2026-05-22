package com.pab.absensi_mahasiswa.model

data class RekapAbsen(
    val matakuliah: String,
    val hadir: Int,
    val izin: Int,
    val sakit: Int,
    val terlambat: Int,
    val total_pertemuan: Int = 16
)
