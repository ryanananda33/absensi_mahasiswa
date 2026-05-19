package com.pab.absensi_mahasiswa

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*

interface ApiService {
    @FormUrlEncoded
    @POST("login.php")
    fun login(
        @Field("nim_nik") nim_nik: String,
        @Field("password") password: String
    ): Call<LoginResponse>

    // FITUR TAMBAHAN NILAI PLUS: Registrasi
    @FormUrlEncoded
    @POST("register.php")
    fun register(
        @Field("nim_nik") nim: String,
        @Field("nama") nama: String,
        @Field("password") pass: String
    ): Call<AbsenResponse>

    @Multipart
    @POST("tambah_absen_pro.php")
    fun kirimAbsen(
        @Part("user_id") userId: RequestBody,
        @Part("matakuliah") matkul: RequestBody,
        @Part("keterangan") keterangan: RequestBody,
        @Part("latitude") lat: RequestBody,
        @Part("longitude") lng: RequestBody,
        @Part foto: MultipartBody.Part
    ): Call<AbsenResponse>

    @GET("get_absen.php")
    fun getAbsen(): Call<List<Absen>>
}