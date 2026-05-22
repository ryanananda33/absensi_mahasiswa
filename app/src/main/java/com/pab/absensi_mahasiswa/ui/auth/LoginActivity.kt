package com.pab.absensi_mahasiswa.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.pab.absensi_mahasiswa.R
import com.pab.absensi_mahasiswa.api.RetrofitClient
import com.pab.absensi_mahasiswa.databinding.ActivityLoginBinding
import com.pab.absensi_mahasiswa.model.LoginResponse
import com.pab.absensi_mahasiswa.session.SessionManager
import com.pab.absensi_mahasiswa.ui.dashboard.DashboardActivity
import com.pab.absensi_mahasiswa.ui.dashboard.DosenDashboardActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var session: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        session = SessionManager(this)

        binding.tvGoToRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        binding.btnLogin.setOnClickListener {
            val nim = binding.etNim.text.toString().trim()
            val pass = binding.etPassword.text.toString().trim()

            if (nim.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, getString(R.string.msg_empty_nim_pass), Toast.LENGTH_SHORT).show()
            } else {
                loginKeServer(nim, pass)
            }
        }
    }

    private fun loginKeServer(nim: String, pass: String) {
        setLoading(true)
        RetrofitClient.instance.login(nim, pass).enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                setLoading(false)
                if (response.isSuccessful) {
                    val hasil = response.body()
                    if (hasil != null && hasil.status == "success") {
                        val user = hasil.data!!
                        
                        // MASTER LOGIC: Simpan Profil Lengkap
                        session.saveSession(
                            id = user.id,
                            nim = user.nim_nik,
                            nama = user.nama,
                            role = user.role, // "mahasiswa" atau "dosen"
                            gender = user.gender,
                            jalur = user.jalur,
                            jurusan = user.jurusan,
                            angkatan = user.angkatan,
                            kelas = user.kelas,
                            semester = user.semester
                        )

                        routeToDashboard(user.role)
                    } else {
                        Toast.makeText(this@LoginActivity, hasil?.message ?: "Otentikasi Gagal", Toast.LENGTH_LONG).show()
                    }
                }
            }
            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                setLoading(false)
                Toast.makeText(this@LoginActivity, "Masalah Koneksi", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun routeToDashboard(role: String) {
        val destination = if (role == "dosen") {
            DosenDashboardActivity::class.java
        } else {
            DashboardActivity::class.java
        }
        
        Toast.makeText(this, "Login Berhasil sebagai ${role.uppercase()}", Toast.LENGTH_SHORT).show()
        startActivity(Intent(this, destination))
        finish()
    }

    private fun setLoading(isLoading: Boolean) {
        binding.btnLogin.isEnabled = !isLoading
        binding.btnLogin.text = if (isLoading) "VERIFIKASI..." else "MASUK SISTEM"
    }
}
