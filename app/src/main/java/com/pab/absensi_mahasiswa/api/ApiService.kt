package com.pab.absensi_mahasiswa.api

import com.pab.absensi_mahasiswa.model.Absen
import com.pab.absensi_mahasiswa.model.AbsenResponse
import com.pab.absensi_mahasiswa.model.LoginResponse
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

    @Multipart
    @POST("register.php")
    fun register(
        @Part("nim_nik") nim: RequestBody,
        @Part("nama") nama: RequestBody,
        @Part("gender") gender: RequestBody,
        @Part("jalur") jalur: RequestBody, // MASTER: Tambahan Jalur Perkuliahan
        @Part("jurusan") jurusan: RequestBody,
        @Part("angkatan") angkatan: RequestBody,
        @Part("kelas") kelas: RequestBody,
        @Part("semester") semester: RequestBody,
        @Part("tempat_lahir") tempat: RequestBody,
        @Part("tanggal_lahir") tanggal: RequestBody,
        @Part("device_id") deviceId: RequestBody,
        @Part("doc_type") docType: RequestBody,
        @Part foto_ktm: MultipartBody.Part,
        @Part foto_selfie: MultipartBody.Part
    ): Call<AbsenResponse>

    @Multipart
    @POST("absensi.php")
    fun kirimAbsen(
        @Part("user_id") userId: RequestBody,
        @Part("matakuliah") matkul: RequestBody,
        @Part("keterangan") keterangan: RequestBody,
        @Part("latitude") lat: RequestBody,
        @Part("longitude") lng: RequestBody,
        @Part("device_id") deviceId: RequestBody,
        @Part foto: MultipartBody.Part
    ): Call<AbsenResponse>

    @GET("riwayat.php")
    fun getAbsen(@Query("user_id") userId: Int): Call<List<Absen>>

    @FormUrlEncoded
    @POST("delete_absen.php")
    fun deleteAbsen(
        @Field("id_absensi") idAbsensi: Int
    ): Call<AbsenResponse>
}
