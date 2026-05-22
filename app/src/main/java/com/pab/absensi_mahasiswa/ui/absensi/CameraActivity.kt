package com.pab.absensi_mahasiswa.ui.absensi

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.pab.absensi_mahasiswa.R
import com.pab.absensi_mahasiswa.camera.CameraManager
import com.pab.absensi_mahasiswa.camera.FaceAnalyzer

class CameraActivity : AppCompatActivity() {

    private lateinit var cameraManager: CameraManager
    private var step = 1 // 1: Align, 2: Blink, 3: Smile
    private var biometricHash: Double = 0.0
    
    private lateinit var tvStatus: TextView
    private lateinit var btnCapture: FloatingActionButton
    private lateinit var btnReset: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)

        cameraManager = CameraManager(this)
        tvStatus = findViewById(R.id.tvCameraStatus)
        btnCapture = findViewById(R.id.btnCapture)
        btnReset = findViewById(R.id.btnOpenGallery)
        val viewFinder = findViewById<PreviewView>(R.id.viewFinder)

        setupInitialState()

        cameraManager.startCamera(this, viewFinder) { action, score ->
            biometricHash = score
            runOnUiThread { handleBiometricStep(action) }
        }

        btnCapture.setOnClickListener {
            cameraManager.takePhoto(
                onImageCaptured = { uri -> finishWithResult(uri) },
                onError = { Toast.makeText(this, "Sistem Gagal Mengunci Biometrik", Toast.LENGTH_SHORT).show() }
            )
        }

        btnReset.setOnClickListener { setupInitialState() }
    }

    private fun setupInitialState() {
        step = 1
        btnCapture.visibility = View.GONE
        tvStatus.text = "TAHAP 1: POSISIKAN WAJAH LURUS"
        tvStatus.setTextColor(ContextCompat.getColor(this, android.R.color.white))
        btnReset.setImageResource(android.R.drawable.ic_menu_rotate)
    }

    private fun handleBiometricStep(action: FaceAnalyzer.Action) {
        when (step) {
            1 -> if (action == FaceAnalyzer.Action.ALIGNED) {
                step = 2
                tvStatus.text = "TAHAP 2: KEDIPKAN MATA"
                tvStatus.setTextColor(ContextCompat.getColor(this, android.R.color.holo_orange_light))
            }
            2 -> if (action == FaceAnalyzer.Action.BLINK) {
                step = 3
                tvStatus.text = "TAHAP 3: SEKARANG SENYUM"
                tvStatus.setTextColor(ContextCompat.getColor(this, android.R.color.holo_green_light))
            }
            3 -> if (action == FaceAnalyzer.Action.SMILE) {
                step = 4
                tvStatus.text = "BIOMETRIK TERVERIFIKASI!"
                tvStatus.setTextColor(ContextCompat.getColor(this, android.R.color.holo_blue_light))
                btnCapture.visibility = View.VISIBLE
            }
        }
    }

    private fun finishWithResult(uri: Uri) {
        val intent = Intent().apply { 
            putExtra("image_uri", uri.toString())
            putExtra("bio_hash", biometricHash) // Kirim skor biometrik untuk data audit
        }
        setResult(Activity.RESULT_OK, intent)
        finish()
    }
}
