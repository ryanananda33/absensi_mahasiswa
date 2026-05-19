package com.pab.absensi_mahasiswa

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val etNama = findViewById<EditText>(R.id.etRegNama)
        val etNim = findViewById<EditText>(R.id.etRegNim)
        val etPass = findViewById<EditText>(R.id.etRegPassword)
        val btnReg = findViewById<Button>(R.id.btnDoRegister)
        val tvBack = findViewById<TextView>(R.id.tvBackToLogin)

        tvBack.setOnClickListener { finish() }

        btnReg.setOnClickListener {
            val nama = etNama.text.toString()
            val nim = etNim.text.toString()
            val pass = etPass.text.toString()

            if (nama.isNotEmpty() && nim.isNotEmpty() && pass.isNotEmpty()) {
                prosesDaftar(nim, nama, pass)
            } else {
                Toast.makeText(this, "Lengkapi semua data!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun prosesDaftar(nim: String, nama: String, pass: String) {
        ApiClient.instance.register(nim, nama, pass).enqueue(object : Callback<AbsenResponse> {
            override fun onResponse(call: Call<AbsenResponse>, response: Response<AbsenResponse>) {
                if (response.isSuccessful && response.body()?.status == "success") {
                    Toast.makeText(this@RegisterActivity, "Berhasil Daftar! Silakan Login", Toast.LENGTH_LONG).show()
                    finish()
                } else {
                    Toast.makeText(this@RegisterActivity, response.body()?.message ?: "Gagal Daftar", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<AbsenResponse>, t: Throwable) {
                Toast.makeText(this@RegisterActivity, "Koneksi Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}