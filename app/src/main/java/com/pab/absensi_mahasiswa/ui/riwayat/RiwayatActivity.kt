package com.pab.absensi_mahasiswa.ui.riwayat

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.pab.absensi_mahasiswa.api.RetrofitClient
import com.pab.absensi_mahasiswa.databinding.ActivityHistoryBinding
import com.pab.absensi_mahasiswa.model.Absen
import com.pab.absensi_mahasiswa.model.AbsenResponse
import com.pab.absensi_mahasiswa.session.SessionManager
import com.pab.absensi_mahasiswa.ui.adapter.RiwayatAdapter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RiwayatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHistoryBinding
    private lateinit var adapter: RiwayatAdapter
    private lateinit var session: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        session = SessionManager(this)

        setupToolbar()
        setupRecyclerView()
        loadData()
    }

    private fun setupToolbar() {
        binding.toolbarHistory.setNavigationOnClickListener { finish() }
    }

    private fun setupRecyclerView() {
        adapter = RiwayatAdapter(emptyList()) { absen ->
            hapusAbsen(absen.id)
        }
        binding.rvAbsen.layoutManager = LinearLayoutManager(this)
        binding.rvAbsen.adapter = adapter
    }

    private fun loadData() {
        RetrofitClient.instance.getAbsen(session.getUserId()).enqueue(object : Callback<List<Absen>> {
            override fun onResponse(call: Call<List<Absen>>, response: Response<List<Absen>>) {
                if (response.isSuccessful) {
                    val listAbsen = response.body() ?: emptyList()
                    adapter.updateData(listAbsen)
                }
            }
            override fun onFailure(call: Call<List<Absen>>, t: Throwable) {
                Toast.makeText(this@RiwayatActivity, "Gagal memuat data", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun hapusAbsen(idAbsen: Int) {
        RetrofitClient.instance.deleteAbsen(idAbsen).enqueue(object : Callback<AbsenResponse> {
            override fun onResponse(call: Call<AbsenResponse>, response: Response<AbsenResponse>) {
                if (response.isSuccessful && response.body()?.status == "success") {
                    Toast.makeText(this@RiwayatActivity, "Berhasil dihapus", Toast.LENGTH_SHORT).show()
                    loadData() // Refresh data
                }
            }
            override fun onFailure(call: Call<AbsenResponse>, t: Throwable) {
                Toast.makeText(this@RiwayatActivity, "Gagal menghapus", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
