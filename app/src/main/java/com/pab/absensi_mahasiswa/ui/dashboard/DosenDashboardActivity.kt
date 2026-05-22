package com.pab.absensi_mahasiswa.ui.dashboard

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.pab.absensi_mahasiswa.databinding.ActivityDashboardDosenBinding
import com.pab.absensi_mahasiswa.session.SessionManager
import com.pab.absensi_mahasiswa.ui.auth.LoginActivity
import com.pab.absensi_mahasiswa.ui.riwayat.RiwayatActivity

class DosenDashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardDosenBinding
    private lateinit var session: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardDosenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        session = SessionManager(this)

        setupUI()
    }

    private fun setupUI() {
        binding.tvNamaDosen.text = "Selamat Datang, Bapak/Ibu ${session.getUserNama()}"
        
        binding.menuMonitoring.setOnClickListener {
            val intent = Intent(this, RiwayatActivity::class.java)
            intent.putExtra("is_dosen", true)
            startActivity(intent)
        }

        binding.btnLogout.setOnClickListener {
            session.logout()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
}
