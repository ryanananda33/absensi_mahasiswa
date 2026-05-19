package com.pab.absensi_mahasiswa

data class LoginResponse(
    val status: String,
    val message: String,
    val data: User?
)
