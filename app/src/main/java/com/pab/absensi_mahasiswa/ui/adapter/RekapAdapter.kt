package com.pab.absensi_mahasiswa.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.pab.absensi_mahasiswa.R
import com.pab.absensi_mahasiswa.model.RekapAbsen

class RekapAdapter(private val list: List<RekapAbsen>) : RecyclerView.Adapter<RekapAdapter.RekapViewHolder>() {

    class RekapViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvMatkul = view.findViewById<TextView>(R.id.tvMatkulRekap)
        val tvHadir = view.findViewById<TextView>(R.id.tvHadirCount)
        val tvSakit = view.findViewById<TextView>(R.id.tvSakitCount)
        val tvIzin = view.findViewById<TextView>(R.id.tvIzinCount)
        val tvTelat = view.findViewById<TextView>(R.id.tvTelatCount)
        val tvPersentase = view.findViewById<TextView>(R.id.tvPersentase)
        val progress = view.findViewById<LinearProgressIndicator>(R.id.progressKehadiran)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RekapViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_rekap, parent, false)
        return RekapViewHolder(view)
    }

    override fun onBindViewHolder(holder: RekapViewHolder, position: Int) {
        val data = list[position]
        holder.tvMatkul.text = data.matakuliah
        holder.tvHadir.text = data.hadir.toString()
        holder.tvSakit.text = data.sakit.toString()
        holder.tvIzin.text = data.izin.toString()
        holder.tvTelat.text = data.terlambat.toString()

        // MASTER LOGIC: Hitung Persentase (Target 16 Pertemuan)
        val percentage = (data.hadir.toFloat() / 16f) * 100
        holder.tvPersentase.text = "Kehadiran: ${percentage.toInt()}%"
        holder.progress.progress = percentage.toInt()

        // LOGIKA KRITIS: Peringatan 75%
        if (percentage < 75) {
            holder.progress.setIndicatorColor(ContextCompat.getColor(holder.itemView.context, R.color.error))
            holder.tvPersentase.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.error))
        } else {
            holder.progress.setIndicatorColor(ContextCompat.getColor(holder.itemView.context, R.color.green_success))
            holder.tvPersentase.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.green_success))
        }
    }

    override fun getItemCount(): Int = list.size
}
