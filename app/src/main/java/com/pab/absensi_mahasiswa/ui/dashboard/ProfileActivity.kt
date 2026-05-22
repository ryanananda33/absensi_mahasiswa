package com.pab.absensi_mahasiswa.ui.dashboard

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.pab.absensi_mahasiswa.databinding.ActivityProfileBinding
import com.pab.absensi_mahasiswa.session.SessionManager

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private lateinit var session: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        session = SessionManager(this)

        setupToolbar()
        displayProfileData()
    }

    private fun setupToolbar() {
        binding.toolbarProfile.setNavigationOnClickListener { finish() }
    }

    private fun displayProfileData() {
        // Master Logic: Mengambil data real dari Session
        binding.tvProfileNama.text = session.getUserNama()
        binding.tvProfileNim.text = "NIM: ${session.getUserNim()}"
        
        binding.tvValGender.text = session.getUserGender()
        binding.tvValTtl.text = session.getUserSemester() // Simulasi TTL jika disimpan di semester field sementara
        binding.tvValProdi.text = session.getUserJurusan()
        binding.tvValKelas.text = "${session.getUserKelas()} / ${session.getUserSemester()}"
    }
}
