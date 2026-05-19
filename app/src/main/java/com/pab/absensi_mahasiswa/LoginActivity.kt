package com.pab.absensi_mahasiswa

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {

    private lateinit var session: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        session = SessionManager(this)

        val etNim = findViewById<EditText>(R.id.etNim)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)

        btnLogin.setOnClickListener {
            val nim = etNim.text.toString()
            val pass = etPassword.text.toString()

            if (nim.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "NIM dan Password harus diisi!", Toast.LENGTH_SHORT).show()
            } else {
                loginKeServer(nim, pass)
            }
        }
    }

    private fun loginKeServer(nim: String, pass: String) {
        ApiClient.instance.login(nim, pass).enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                if (response.isSuccessful) {
                    val hasil = response.body()
                    if (hasil != null && hasil.status == "success") {
                        // MASTER LEVEL: Simpan data user ke Session agar ID tidak kaku
                        hasil.data?.let { user ->
                            session.saveSession(user.id, user.nim_nik, user.nama)
                        }

                        Toast.makeText(this@LoginActivity, "Berhasil Login! Halo ${hasil.data?.nama}", Toast.LENGTH_LONG).show()

                        val intent = Intent(this@LoginActivity, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        val pesan = hasil?.message ?: "Gagal Login"
                        Toast.makeText(this@LoginActivity, pesan, Toast.LENGTH_SHORT).show()
                    }
                } else {
                    val errorDetail = "Error " + response.code() + ": Respon server gagal"
                    Toast.makeText(this@LoginActivity, errorDetail, Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                Toast.makeText(this@LoginActivity, "Koneksi Error: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }
}