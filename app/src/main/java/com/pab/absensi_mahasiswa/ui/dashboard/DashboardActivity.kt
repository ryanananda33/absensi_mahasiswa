package com.pab.absensi_mahasiswa.ui.dashboard

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.pab.absensi_mahasiswa.databinding.ActivityDashboardBinding
import com.pab.absensi_mahasiswa.helper.AlarmHelper
import com.pab.absensi_mahasiswa.helper.FileHelper
import com.pab.absensi_mahasiswa.session.SessionManager
import com.pab.absensi_mahasiswa.ui.absensi.AbsensiActivity
import com.pab.absensi_mahasiswa.ui.auth.LoginActivity
import com.pab.absensi_mahasiswa.ui.riwayat.RekapActivity
import com.pab.absensi_mahasiswa.ui.riwayat.RiwayatActivity
import java.util.*

class DashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardBinding
    private lateinit var session: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        session = SessionManager(this)

        if (!session.isLoggedIn()) {
            goToLogin()
            return
        }

        // Jalankan Penjadwal Alarm Otomatis
        AlarmHelper.scheduleAttendanceReminder(this)

        setupUI()
    }

    private fun setupUI() {
        binding.tvGreeting.text = getSmartGreeting()
        binding.tvNamaUser.text = session.getUserNama()

        // Navigasi Menu
        binding.menuAbsen.setOnClickListener { startActivity(Intent(this, AbsensiActivity::class.java)) }
        binding.menuRiwayat.setOnClickListener { startActivity(Intent(this, RiwayatActivity::class.java)) }
        binding.menuRekap.setOnClickListener { startActivity(Intent(this, RekapActivity::class.java)) }
        binding.menuProfile.setOnClickListener { startActivity(Intent(this, ProfileActivity::class.java)) }
        binding.ivProfileSmall.setOnClickListener { startActivity(Intent(this, ProfileActivity::class.java)) }
        
        // Shortcut Absen Cepat di Card
        binding.btnQuickAbsen.setOnClickListener { startActivity(Intent(this, AbsensiActivity::class.java)) }

        binding.btnLogout.setOnClickListener { showLogoutDialog() }
    }

    private fun getSmartGreeting(): String {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when (hour) {
            in 0..11 -> "Selamat Pagi,"
            in 12..15 -> "Selamat Siang,"
            in 16..18 -> "Selamat Sore,"
            else -> "Selamat Malam,"
        }
    }

    private fun showLogoutDialog() {
        AlertDialog.Builder(this)
            .setTitle("Logout Akun")
            .setMessage("Apakah Anda ingin mengakhiri sesi akademik sekarang?")
            .setPositiveButton("Ya, Keluar") { _, _ -> performSecureLogout() }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun performSecureLogout() {
        AlarmHelper.cancelAllReminders(this)
        FileHelper.clearAppCache(this)
        session.logout()
        goToLogin()
    }

    private fun goToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
