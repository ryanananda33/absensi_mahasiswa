package com.pab.absensi_mahasiswa.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.pab.absensi_mahasiswa.api.RetrofitClient
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File

class UploadWorker(appContext: Context, params: WorkerParameters) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val userId = inputData.getString("user_id") ?: return Result.failure()
        val matkul = inputData.getString("matkul") ?: return Result.failure()
        val keterangan = inputData.getString("keterangan") ?: return Result.failure()
        val lat = inputData.getString("lat") ?: return Result.failure()
        val lng = inputData.getString("lng") ?: return Result.failure()
        val photoPath = inputData.getString("photo_path") ?: return Result.failure()
        val deviceId = inputData.getString("device_id") ?: "unknown"

        return try {
            val file = File(photoPath)
            if (!file.exists()) return Result.failure()

            // MASTER SECURITY 1: Randomized Delay (0-5 detik)
            // Biar ribuan data nggak masuk barengan ke server Ryan
            kotlinx.coroutines.delay((0..5000).random().toLong())

            val rbUserId = createPart(userId)
            val rbMatkul = createPart(matkul)
            val rbKet = createPart(keterangan)
            val rbLat = createPart(lat)
            val rbLng = createPart(lng)
            val rbDeviceId = createPart(deviceId) // Kirim Device ID untuk divalidasi Ryan

            val bodyFoto = MultipartBody.Part.createFormData("foto", file.name, RequestBody.create(MediaType.parse("image/jpeg"), file))

            // Update ApiService.kt untuk menerima device_id
            val response = RetrofitClient.instance.kirimAbsen(rbUserId, rbMatkul, rbKet, rbLat, rbLng, bodyFoto).execute()

            if (response.isSuccessful) Result.success() else Result.retry()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    private fun createPart(value: String) = RequestBody.create(MediaType.parse("text/plain"), value)
}
