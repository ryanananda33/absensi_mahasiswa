package com.pab.absensi_mahasiswa

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {
    // Alamat IP laptop Anda dan folder di htdocs

    // File: ApiClient.kt
    private const val BASE_URL = "http://10.0.2.2/ABSENSI-API/" // Samakan persis dengan nama folder di htdocs!
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val instance: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }
}
