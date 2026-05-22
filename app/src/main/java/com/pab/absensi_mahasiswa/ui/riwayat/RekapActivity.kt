package com.pab.absensi_mahasiswa.ui.riwayat

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.pab.absensi_mahasiswa.databinding.ActivityRekapBinding
import com.pab.absensi_mahasiswa.model.RekapAbsen
import com.pab.absensi_mahasiswa.ui.adapter.RekapAdapter

class RekapActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRekapBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRekapBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbarRekap.setNavigationOnClickListener { finish() }

        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        // Simulasi Data Master FRS (Nanti ditarik dari API get_rekap.php)
        val dummyRekap = listOf(
            RekapAbsen("Pemrograman Aplikasi Mobile", 14, 0, 1, 1),
            RekapAbsen("Rekayasa Perangkat Lunak", 10, 2, 0, 4), // Persentase Rendah!
            RekapAbsen("Desain Antarmuka (UI/UX)", 16, 0, 0, 0), // Sempurna
            RekapAbsen("Sistem Basis Data", 12, 1, 1, 2)
        )

        binding.rvRekap.layoutManager = LinearLayoutManager(this)
        binding.rvRekap.adapter = RekapAdapter(dummyRekap)
    }
}
