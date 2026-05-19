package com.pab.absensi_mahasiswa

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.compose.animation.with
import androidx.recyclerview.widget.RecyclerView
import androidx.wear.compose.material.placeholder
import com.github.bumptech.glide.Glide

class AbsenAdapter(private val list: List<Absen>) : RecyclerView.Adapter<AbsenAdapter.AbsenViewHolder>() {

    class AbsenViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvMatkul = view.findViewById<TextView>(R.id.tvItemMatkul)
        val tvKet = view.findViewById<TextView>(R.id.tvItemKeterangan)
        val tvWaktu = view.findViewById<TextView>(R.id.tvItemWaktu)
        val ivBukti = view.findViewById<ImageView>(R.id.ivBuktiFoto)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AbsenViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_absen, parent, false)
        return AbsenViewHolder(view)
    }

    override fun onBindViewHolder(holder: AbsenViewHolder, position: Int) {
        val data = list[position]
        holder.tvMatkul.text = data.matakuliah
        holder.tvKet.text = data.keterangan
        holder.tvWaktu.text = data.waktu_absen

        // MASTER LEVEL: Load foto dari server menggunakan Glide
        val urlFoto = "http://10.0.2.2/ABSENSI-API/uploads/" + data.foto
        Glide.with(holder.itemView.context)
            .load(urlFoto)
            .placeholder(android.R.drawable.ic_menu_gallery)
            .into(holder.ivBukti)
    }

    override fun getItemCount(): Int = list.size
}