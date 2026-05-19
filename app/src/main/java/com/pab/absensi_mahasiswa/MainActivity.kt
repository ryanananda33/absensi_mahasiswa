package com.pab.absensi_mahasiswa

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var session: SessionManager

    private lateinit var tvWelcome: TextView
    private lateinit var tvLokasi: TextView
    private lateinit var etMatkul: EditText
    private lateinit var rgKeterangan: RadioGroup
    private lateinit var ivSelfie: ImageView
    private lateinit var btnAmbilFoto: Button
    private lateinit var btnAbsen: Button
    private lateinit var btnRiwayat: Button

    private var currentLatitude: Double = 0.0
    private var currentLongitude: Double = 0.0
    private var photoFile: File? = null

    // KOORDINAT KAMPUS (Sesuaikan dengan koordinat asli kampus Anda)
    private val CAMPUS_LAT = -6.1754
    private val CAMPUS_LNG = 106.8272
    private val MAX_DISTANCE_METERS = 100.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        session = SessionManager(this)

        tvWelcome = findViewById(R.id.tvWelcome)
        tvLokasi = findViewById(R.id.tvLokasi)
        etMatkul = findViewById(R.id.etMatkul)
        rgKeterangan = findViewById(R.id.rgKeterangan)
        ivSelfie = findViewById(R.id.ivSelfie)
        btnAmbilFoto = findViewById(R.id.btnAmbilFoto)
        btnAbsen = findViewById(R.id.btnAbsen)
        btnRiwayat = findViewById(R.id.btnRiwayat)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // MASTER LEVEL: Tampilkan nama user asli dari Session
        tvWelcome.text = "Portal Absensi: ${session.getUserNama()}"

        ambilLokasi()

        btnAmbilFoto.setOnClickListener { bukaKamera() }

        btnRiwayat.setOnClickListener {
            startActivity(Intent(this, HistoryActivity::class.java))
        }

        btnAbsen.setOnClickListener { validasiDanKirim() }
    }

    private fun ambilLokasi() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 100)
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            if (location != null) {
                currentLatitude = location.latitude
                currentLongitude = location.longitude

                val results = FloatArray(1)
                Location.distanceBetween(currentLatitude, currentLongitude, CAMPUS_LAT, CAMPUS_LNG, results)
                val distance = results[0]

                if (distance <= MAX_DISTANCE_METERS) {
                    tvLokasi.text = "Lokasi: Di Area Kampus (${distance.toInt()}m)"
                    tvLokasi.setTextColor(resources.getColor(android.R.color.holo_green_dark))
                } else {
                    tvLokasi.text = "Lokasi: Luar Area (${distance.toInt()}m dari Kampus)"
                    tvLokasi.setTextColor(resources.getColor(android.R.color.holo_red_dark))
                }
            }
        }
    }

    private fun bukaKamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        try {
            photoFile = createImageFile()
            photoFile?.let {
                val photoURI: Uri = FileProvider.getUriForFile(this, "com.pab.absensi_mahasiswa.fileprovider", it)
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                cameraLauncher.launch(intent)
            }
        } catch (ex: IOException) {
            Toast.makeText(this, "Gagal membuka kamera", Toast.LENGTH_SHORT).show()
        }
    }

    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val bitmap = BitmapFactory.decodeFile(photoFile?.absolutePath)
            ivSelfie.setImageBitmap(bitmap)
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir)
    }

    private fun validasiDanKirim() {
        val matkul = etMatkul.text.toString()
        val selectedId = rgKeterangan.checkedRadioButtonId

        if (matkul.isEmpty()) {
            Toast.makeText(this, "Isi Matakuliah!", Toast.LENGTH_SHORT).show()
            return
        }
        if (photoFile == null) {
            Toast.makeText(this, "Ambil Foto Selfie dulu!", Toast.LENGTH_SHORT).show()
            return
        }
        if (selectedId == -1) {
            Toast.makeText(this, "Pilih Keterangan!", Toast.LENGTH_SHORT).show()
            return
        }

        val keterangan = findViewById<RadioButton>(selectedId).text.toString()

        val results = FloatArray(1)
        Location.distanceBetween(currentLatitude, currentLongitude, CAMPUS_LAT, CAMPUS_LNG, results)
        val distance = results[0]

        if (distance > MAX_DISTANCE_METERS) {
            Toast.makeText(this, "Anda terlalu jauh dari Kampus!", Toast.LENGTH_LONG).show()
            return
        }

        // MASTER LEVEL: Kirim ID User yang asli dari session
        kirimData(session.getUserId(), matkul, keterangan)
    }

    private fun kirimData(userId: Int, matkul: String, ket: String) {
        val rbUserId = RequestBody.create(MediaType.parse("text/plain"), userId.toString())
        val rbMatkul = RequestBody.create(MediaType.parse("text/plain"), matkul)
        val rbKet = RequestBody.create(MediaType.parse("text/plain"), ket)
        val rbLat = RequestBody.create(MediaType.parse("text/plain"), currentLatitude.toString())
        val rbLng = RequestBody.create(MediaType.parse("text/plain"), currentLongitude.toString())

        val requestFile = RequestBody.create(MediaType.parse("image/jpeg"), photoFile!!)
        val bodyFoto = MultipartBody.Part.createFormData("foto", photoFile!!.name, requestFile)

        ApiClient.instance.kirimAbsen(rbUserId, rbMatkul, rbKet, rbLat, rbLng, bodyFoto)
            .enqueue(object : Callback<AbsenResponse> {
                override fun onResponse(call: Call<AbsenResponse>, response: Response<AbsenResponse>) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@MainActivity, "Absensi Master Berhasil!", Toast.LENGTH_LONG).show()
                        finish()
                    }
                }
                override fun onFailure(call: Call<AbsenResponse>, t: Throwable) {
                    Toast.makeText(this@MainActivity, "Gagal: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }
}
