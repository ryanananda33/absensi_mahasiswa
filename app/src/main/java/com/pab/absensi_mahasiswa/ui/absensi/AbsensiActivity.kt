package com.pab.absensi_mahasiswa.ui.absensi

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.ArrayAdapter
import android.widget.RadioButton
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.work.*
import com.pab.absensi_mahasiswa.R
import com.pab.absensi_mahasiswa.camera.SelfieProcessor
import com.pab.absensi_mahasiswa.databinding.ActivityAbsensiBinding
import com.pab.absensi_mahasiswa.helper.TimeHelper
import com.pab.absensi_mahasiswa.location.GPSManager
import com.pab.absensi_mahasiswa.location.GeofenceManager
import com.pab.absensi_mahasiswa.session.SessionManager
import com.pab.absensi_mahasiswa.worker.UploadWorker
import java.io.File
import java.io.FileOutputStream
import java.util.*

class AbsensiActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAbsensiBinding
    private lateinit var gpsManager: GPSManager
    private lateinit var geofenceManager: GeofenceManager
    private lateinit var session: SessionManager
    private lateinit var selfieProcessor: SelfieProcessor

    private var currentLatitude: Double = 0.0
    private var currentLongitude: Double = 0.0
    private var photoFile: File? = null
    private var isPracticumMode = false

    // JADWAL MASTER TERPADU (Sesuai Seluruh Jurusan & Jalur)
    private lateinit var scheduleMap: Map<String, String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAbsensiBinding.inflate(layoutInflater)
        setContentView(binding.root)

        session = SessionManager(this)
        gpsManager = GPSManager(this)
        geofenceManager = GeofenceManager()
        selfieProcessor = SelfieProcessor()

        initializeMasterSchedule()
        setupToolbar()
        setupDropdownMatkul()
        checkPermissions()
        setupStatusListeners()

        binding.btnAmbilFoto.setOnClickListener { bukaKamera() }
        binding.ivSelfie.setOnClickListener { galleryLauncher.launch("image/*") }
        binding.btnAbsen.setOnClickListener { validateBeforeQueue() }

        binding.toggleMeetingType.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                isPracticumMode = (checkedId == R.id.btnPraktikum)
                updateLokasi()
            }
        }
    }

    private fun initializeMasterSchedule() {
        val jalur = session.getUserJalur()
        val today = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)

        // LOGIKA PENJADWALAN DUNIA NYATA (Anti-Ngaco)
        scheduleMap = when {
            // JALUR 1: KARYAWAN MALAM (Senin - Jumat, 19:00 - 21:30)
            jalur.contains("Malam") -> {
                if (today in Calendar.MONDAY..Calendar.FRIDAY) {
                    mapOf("Sesi Malam 1" to "19:00", "Sesi Malam 2" to "20:30")
                } else emptyMap()
            }

            // JALUR 2: KARYAWAN WEEKEND (Sabtu Saja)
            jalur.contains("Sabtu") || jalur.contains("Weekend") -> {
                if (today == Calendar.SATURDAY) {
                    mapOf("Sesi Pagi (Weekend)" to "07:00", "Sesi Siang (Weekend)" to "14:30")
                } else emptyMap()
            }

            // JALUR 3: REGULER (Senin - Jumat, 08:00 - 17:40)
            else -> {
                if (today in Calendar.MONDAY..Calendar.FRIDAY) {
                    mapOf(
                        "Matakuliah Pagi" to "08:00",
                        "Matakuliah Siang" to "10:30",
                        "Matakuliah Sore 1" to "13:30",
                        "Matakuliah Sore 2" to "15:30"
                    )
                } else emptyMap()
            }
        }
    }

    private fun setupDropdownMatkul() {
        if (scheduleMap.isEmpty()) {
            binding.etMatkul.setText("Tidak ada jadwal aktif hari ini")
            binding.etMatkul.isEnabled = false
            return
        }
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, scheduleMap.keys.toList())
        binding.etMatkul.setAdapter(adapter)
    }

    private fun validateBeforeQueue() {
        val matkul = binding.etMatkul.text.toString()
        val selectedId = binding.rgKeterangan.checkedRadioButtonId
        
        if (matkul.isEmpty() || matkul.contains("Tidak ada") || selectedId == -1 || photoFile == null) {
            Toast.makeText(this, "Lengkapi data dan bukti foto!", Toast.LENGTH_SHORT).show()
            return
        }

        if (TimeHelper.isUsingManualTime(this)) {
            showSecurityAlert("Kecurangan Waktu", "Harap aktifkan Waktu Otomatis.")
            return
        }

        val startTime = scheduleMap[matkul] ?: "08:00"
        val status = findViewById<RadioButton>(selectedId).text.toString()
        var finalStatus = status

        // LOGIKA MASTER: Pemicu Terlambat (Grace Period 20 Menit)
        if (TimeHelper.isLate(startTime)) finalStatus = "Terlambat"

        if (status == "Hadir") {
            if (!geofenceManager.cekDalamRadius(currentLatitude, currentLongitude, isPracticumMode)) {
                val loc = if (isPracticumMode) "Lab" else "Gedung Teori"
                Toast.makeText(this, "AKSES DITOLAK: Anda berada di luar radius $loc!", Toast.LENGTH_LONG).show()
                return
            }
        }
        enqueueAttendance(matkul, finalStatus)
    }

    private fun enqueueAttendance(matkul: String, status: String) {
        val uploadData = workDataOf(
            "user_id" to session.getUserId().toString(),
            "matkul" to matkul,
            "keterangan" to status,
            "lat" to currentLatitude.toString(),
            "lng" to currentLongitude.toString(),
            "photo_path" to photoFile!!.absolutePath,
            "device_id" to session.getDeviceId()
        )
        val constraints = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
        val uploadRequest = OneTimeWorkRequestBuilder<UploadWorker>()
            .setInputData(uploadData).setConstraints(constraints).build()
        WorkManager.getInstance(this).enqueueUniqueWork("upload_${System.currentTimeMillis()}", ExistingWorkPolicy.REPLACE, uploadRequest)
        showSuccessDialog(status)
    }

    private fun setupToolbar() { binding.toolbarAbsen.setNavigationOnClickListener { finish() } }

    private fun setupStatusListeners() {
        binding.rgKeterangan.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rbHadir -> binding.tvLabelFoto.text = "Bukti Selfie di Kelas/Lab"
                R.id.rbSakit -> binding.tvLabelFoto.text = "Bukti Surat Sakit / Kondisi"
                R.id.rbIzin -> binding.tvLabelFoto.text = "Bukti Izin Resmi"
            }
        }
    }

    private fun checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) updateLokasi()
        else requestPermissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.CAMERA))
    }

    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { if (it[Manifest.permission.ACCESS_FINE_LOCATION] == true) updateLokasi() }

    private fun updateLokasi() {
        binding.tvLokasi.text = "MENGUNCI SINYAL SATELIT..."
        gpsManager.ambilLokasi(
            onSuccess = { lat, lng ->
                currentLatitude = lat
                currentLongitude = lng
                val distance = geofenceManager.hitungJarak(lat, lng, isPracticumMode)
                if (geofenceManager.cekDalamRadius(lat, lng, isPracticumMode)) {
                    binding.tvLokasi.text = "SINYAL TERKUNCI: DI AREA KAMPUS (${distance.toInt()}m)"
                    binding.tvLokasi.setTextColor(ContextCompat.getColor(this, android.R.color.white))
                } else {
                    binding.tvLokasi.text = "PERINGATAN: DI LUAR AREA (${distance.toInt()}m)"
                    binding.tvLokasi.setTextColor(ContextCompat.getColor(this, R.color.error))
                }
            },
            onError = { binding.tvLokasi.text = it }
        )
    }

    private fun showSuccessDialog(status: String) {
        val msg = if (status == "Terlambat") "Tersimpan: STATUS TERLAMBAT" else "Absensi Berhasil Dikirim."
        AlertDialog.Builder(this).setTitle("Berhasil").setMessage(msg).setPositiveButton("Selesai") { _, _ -> finish() }.show()
    }

    private fun bukaKamera() { cameraLauncher.launch(Intent(this, CameraActivity::class.java)) }

    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == RESULT_OK) {
            val uri = Uri.parse(it.data?.getStringExtra("image_uri"))
            photoFile = selfieProcessor.compressImage(File(uri.path!!))
            binding.ivSelfie.setImageBitmap(BitmapFactory.decodeFile(photoFile?.absolutePath))
        }
    }

    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            val file = File(cacheDir, "TEMP_PROOF.jpg")
            contentResolver.openInputStream(it)?.copyTo(FileOutputStream(file))
            photoFile = selfieProcessor.compressImage(file)
            binding.ivSelfie.setImageURI(it)
        }
    }

    private fun showSecurityAlert(title: String, message: String) {
        AlertDialog.Builder(this).setTitle(title).setMessage(message)
            .setPositiveButton("Setelan") { _, _ -> startActivity(Intent(Settings.ACTION_DATE_SETTINGS)) }
            .setNegativeButton("Tutup", null).show()
    }

    override fun onResume() { super.onResume()
        if (gpsManager.cekGPSAktif() && currentLatitude == 0.0) updateLokasi()
    }
}
