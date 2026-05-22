package com.pab.absensi_mahasiswa.model

import com.pab.absensi_mahasiswa.model.User

data class LoginResponse(
    val status: String,
    val message: String,
    val data: User?
)
