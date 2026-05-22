package com.pab.absensi_mahasiswa.ui.riwayat

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.pab.absensi_mahasiswa.R

class DetailAbsensiActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_absensi)

        val matkul = intent.getStringExtra("matkul")
        val waktu = intent.getStringExtra("waktu")
        val status = intent.getStringExtra("status")
        val foto = intent.getStringExtra("foto")

        val ivFoto = findViewById<ImageView>(R.id.ivDetailFoto)
        val tvMatkul = findViewById<TextView>(R.id.tvDetailMatkul)
        val tvWaktu = findViewById<TextView>(R.id.tvDetailWaktu)
        val tvStatus = findViewById<TextView>(R.id.tvDetailStatus)

        tvMatkul.text = matkul
        tvWaktu.text = waktu
        tvStatus.text = status

        val urlFoto = "http://10.0.2.2/absensi_api/uploads/selfie/$foto"
        Glide.with(this)
            .load(urlFoto)
            .placeholder(android.R.drawable.ic_menu_gallery)
            .into(ivFoto)
    }
}
