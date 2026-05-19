package com.pab.absensi_mahasiswa

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HistoryActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        val rvAbsen = findViewById<RecyclerView>(R.id.rvAbsen)
        rvAbsen.layoutManager = LinearLayoutManager(this)

        // Panggil API untuk mengambil data
        ApiClient.instance.getAbsen().enqueue(object : Callback<List<Absen>> {
            override fun onResponse(call: Call<List<Absen>>, response: Response<List<Absen>>) {
                if (response.isSuccessful) {
                    val listAbsen = response.body() ?: emptyList()
                    val adapter = AbsenAdapter(listAbsen)
                    rvAbsen.adapter = adapter
                }
            }

            override fun onFailure(call: Call<List<Absen>>, t: Throwable) {
                Toast.makeText(this@HistoryActivity, "Gagal mengambil data", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
