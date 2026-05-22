package com.pab.absensi_mahasiswa.ui.auth

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.pab.absensi_mahasiswa.R
import com.pab.absensi_mahasiswa.api.RetrofitClient
import com.pab.absensi_mahasiswa.databinding.ActivityRegisterBinding
import com.pab.absensi_mahasiswa.helper.FuzzyMatcher
import com.pab.absensi_mahasiswa.model.AbsenResponse
import com.pab.absensi_mahasiswa.session.SessionManager
import com.pab.absensi_mahasiswa.ui.absensi.CameraActivity
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.util.*

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var session: SessionManager
    private var ktmPhotoFile: File? = null
    private var selfiePhotoFile: File? = null
    private var selectedGender: String = ""
    private val calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        session = SessionManager(this)
        setupDropdowns()
        setupListeners()
    }

    private fun setupDropdowns() {
        binding.spinnerJurusan.setAdapter(ArrayAdapter(this, android.R.layout.simple_list_item_1, resources.getStringArray(R.array.jurusan_array)))
        binding.spinnerAngkatan.setAdapter(ArrayAdapter(this, android.R.layout.simple_list_item_1, resources.getStringArray(R.array.angkatan_array)))
        binding.spinnerSemester.setAdapter(ArrayAdapter(this, android.R.layout.simple_list_item_1, resources.getStringArray(R.array.semester_array)))
        binding.spinnerDocType.setAdapter(ArrayAdapter(this, android.R.layout.simple_list_item_1, resources.getStringArray(R.array.doc_type_array)))
        binding.spinnerJalur.setAdapter(ArrayAdapter(this, android.R.layout.simple_list_item_1, resources.getStringArray(R.array.jalur_array)))
        
        binding.spinnerJurusan.setOnItemClickListener { _, _, _, _ -> generateSmartClassList() }
        binding.spinnerAngkatan.setOnItemClickListener { _, _, _, _ -> generateSmartClassList() }
    }

    private fun generateSmartClassList() {
        val jurusan = binding.spinnerJurusan.text.toString()
        val angkatan = binding.spinnerAngkatan.text.toString()
        if (jurusan.isNotEmpty() && angkatan.isNotEmpty()) {
            val prefix = when (jurusan) {
                "Teknik Informatika" -> "TINFC"
                "Sistem Informasi" -> "SINFC"
                "Desain Komunikasi Visual" -> "PDKVC"
                "Teknik Sipil" -> "SIPLC"
                else -> ""
            }
            if (prefix.isNotEmpty()) {
                val classes = arrayOf("$prefix-$angkatan-01", "$prefix-$angkatan-02", "$prefix-$angkatan-03", "$prefix-$angkatan-04")
                binding.spinnerKelas.setAdapter(ArrayAdapter(this, android.R.layout.simple_list_item_1, classes))
                binding.spinnerKelas.text = null
            }
        }
    }

    private fun setupListeners() {
        binding.root.setOnClickListener { hideKeyboard() }
        binding.tvBackToLogin.setOnClickListener { finish() }
        binding.etRegTanggalLahir.setOnClickListener { showDatePicker() }
        
        binding.toggleGender.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                selectedGender = if (checkedId == binding.btnMale.id) "Laki-laki" else "Perempuan"
            }
        }

        binding.btnAmbilKtm.setOnClickListener { ktmPickerLauncher.launch("image/*") }
        binding.btnAmbilSelfie.setOnClickListener { selfieCameraLauncher.launch(Intent(this, CameraActivity::class.java)) }
        binding.btnDoRegister.setOnClickListener { validateAndSubmit() }
    }

    private fun showDatePicker() {
        DatePickerDialog(this, { _, year, month, day ->
            val date = String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, day)
            binding.etRegTanggalLahir.setText(date)
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
    }

    private val ktmPickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { sourceUri -> 
            deepAuditKtmCard(sourceUri)
        }
    }

    private fun deepAuditKtmCard(uri: Uri) {
        val typedName = binding.etRegNama.text.toString().trim()
        val typedNim = binding.etRegNim.text.toString().trim()

        if (typedName.isEmpty() || typedNim.isEmpty()) {
            Toast.makeText(this, "Wajib isi Nama & NIM sebelum validasi!", Toast.LENGTH_LONG).show()
            return
        }

        val image = InputImage.fromFilePath(this, uri)
        val faceOptions = FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
            .setMinFaceSize(0.1f)
            .build()

        val faceDetector = FaceDetection.getClient(faceOptions)
        val textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

        faceDetector.process(image).addOnSuccessListener { faces ->
            if (faces.isEmpty()) {
                Toast.makeText(this, "GAGAL: Wajah tidak terdeteksi di dokumen!", Toast.LENGTH_LONG).show()
            } else {
                textRecognizer.process(image).addOnSuccessListener { visionText ->
                    val fullCardText = visionText.text.lowercase().replace("\n", " ")
                    val nameScore = FuzzyMatcher.getSimilarityScore(typedName, fullCardText)
                    val isNimMatch = fullCardText.contains(typedNim)

                    if (isNimMatch && (nameScore > 0.25)) {
                        saveKtmFile(uri)
                    } else if (!isNimMatch) {
                        Toast.makeText(this, "GAGAL: NIM tidak cocok dengan kartu!", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(this, "GAGAL: Nama tidak sesuai dokumen!", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    private fun saveKtmFile(uri: Uri) {
        val file = File(cacheDir, "KTM_${System.currentTimeMillis()}.jpg")
        contentResolver.openInputStream(uri)?.use { input -> FileOutputStream(file).use { output -> input.copyTo(output) } }
        ktmPhotoFile = file
        Glide.with(this).load(ktmPhotoFile).into(binding.ivKtmPreview)
        Toast.makeText(this, "Dokumen Terverifikasi", Toast.LENGTH_SHORT).show()
    }

    private val selfieCameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uriString = result.data?.getStringExtra("image_uri")
            uriString?.let {
                selfiePhotoFile = File(Uri.parse(it).path!!)
                Glide.with(this).load(selfiePhotoFile).into(binding.ivSelfiePreview)
            }
        }
    }

    private fun validateAndSubmit() {
        if (binding.etRegNama.text.isNullOrEmpty() || ktmPhotoFile == null || selfiePhotoFile == null || 
            binding.spinnerJurusan.text.isEmpty() || binding.spinnerKelas.text.isEmpty() ||
            binding.spinnerDocType.text.isEmpty() || binding.spinnerJalur.text.isEmpty() ||
            selectedGender.isEmpty()) {
            Toast.makeText(this, "Harap lengkapi seluruh formulir!", Toast.LENGTH_SHORT).show()
            return
        }
        setLoading(true)
        sendRegistrationData()
    }

    private fun sendRegistrationData() {
        val rbNim = createPart(binding.etRegNim.text.toString())
        val rbNama = createPart(binding.etRegNama.text.toString())
        val rbGender = createPart(selectedGender)
        val rbJalur = createPart(binding.spinnerJalur.text.toString())
        val rbJurusan = createPart(binding.spinnerJurusan.text.toString())
        val rbAngkatan = createPart(binding.spinnerAngkatan.text.toString())
        val rbKelas = createPart(binding.spinnerKelas.text.toString())
        val rbSemester = createPart(binding.spinnerSemester.text.toString())
        val rbTempat = createPart(binding.etRegTempatLahir.text.toString())
        val rbTgl = createPart(binding.etRegTanggalLahir.text.toString())
        val rbDeviceId = createPart(session.getDeviceId())
        val rbDocType = createPart(binding.spinnerDocType.text.toString())

        val bodyKtm = MultipartBody.Part.createFormData("foto_ktm", ktmPhotoFile!!.name, RequestBody.create(MediaType.parse("image/jpeg"), ktmPhotoFile!!))
        val bodySelfie = MultipartBody.Part.createFormData("foto_selfie", selfiePhotoFile!!.name, RequestBody.create(MediaType.parse("image/jpeg"), selfiePhotoFile!!))

        RetrofitClient.instance.register(rbNim, rbNama, rbGender, rbJalur, rbJurusan, rbAngkatan, rbKelas, rbSemester, rbTempat, rbTgl, rbDeviceId, rbDocType, bodyKtm, bodySelfie)
            .enqueue(object : Callback<AbsenResponse> {
                override fun onResponse(call: Call<AbsenResponse>, response: Response<AbsenResponse>) {
                    setLoading(false)
                    if (response.isSuccessful && response.body()?.status == "success") {
                        Toast.makeText(this@RegisterActivity, getString(R.string.msg_reg_success), Toast.LENGTH_LONG).show()
                        finish()
                    } else {
                        Toast.makeText(this@RegisterActivity, "DITOLAK: ${response.body()?.message}", Toast.LENGTH_LONG).show()
                    }
                }
                override fun onFailure(call: Call<AbsenResponse>, t: Throwable) {
                    setLoading(false)
                    Toast.makeText(this@RegisterActivity, "Server Error", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun createPart(value: String): RequestBody = RequestBody.create(MediaType.parse("text/plain"), value)
    private fun setLoading(isLoading: Boolean) {
        binding.btnDoRegister.isEnabled = !isLoading
        binding.btnDoRegister.text = if (isLoading) "MEMPROSES..." else "KONFIRMASI PENDAFTARAN"
    }
    private fun hideKeyboard() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
    }
}
