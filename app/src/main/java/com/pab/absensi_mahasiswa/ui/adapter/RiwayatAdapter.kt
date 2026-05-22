package com.pab.absensi_mahasiswa.ui.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.pab.absensi_mahasiswa.R
import com.pab.absensi_mahasiswa.model.Absen
import com.pab.absensi_mahasiswa.ui.riwayat.DetailAbsensiActivity

class RiwayatAdapter(
    private var list: List<Absen>,
    private val onDeleteClick: (Absen) -> Unit
) : RecyclerView.Adapter<RiwayatAdapter.RiwayatViewHolder>() {

    class RiwayatViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvMatkul: TextView = view.findViewById(R.id.tvItemMatkul)
        val tvKet: TextView = view.findViewById(R.id.tvItemKeterangan)
        val tvWaktu: TextView = view.findViewById(R.id.tvItemWaktu)
        val ivBukti: ImageView = view.findViewById(R.id.ivBuktiFoto)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RiwayatViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_absen, parent, false)
        return RiwayatViewHolder(view)
    }

    override fun onBindViewHolder(holder: RiwayatViewHolder, position: Int) {
        val data = list[position]
        holder.tvMatkul.text = data.matakuliah
        holder.tvKet.text = data.keterangan
        holder.tvWaktu.text = data.waktu_absen

        val urlFoto = "http://10.0.2.2/absensi_api/uploads/selfie/" + data.foto
        Glide.with(holder.itemView.context)
            .load(urlFoto)
            .placeholder(android.R.drawable.ic_menu_gallery)
            .into(holder.ivBukti)

        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, DetailAbsensiActivity::class.java).apply {
                putExtra("matkul", data.matakuliah)
                putExtra("waktu", data.waktu_absen)
                putExtra("status", data.keterangan)
                putExtra("foto", data.foto)
            }
            holder.itemView.context.startActivity(intent)
        }

        // SYARAT CRUD: Delete via Long Click (Tekan lama untuk hapus)
        holder.itemView.setOnLongClickListener {
            AlertDialog.Builder(holder.itemView.context)
                .setTitle("Hapus Riwayat")
                .setMessage("Yakin ingin menghapus riwayat absen ini?")
                .setPositiveButton("Hapus") { _, _ ->
                    onDeleteClick(data)
                }
                .setNegativeButton("Batal", null)
                .show()
            true
        }
    }

    override fun getItemCount(): Int = list.size

    fun updateData(newList: List<Absen>) {
        this.list = newList
        notifyDataSetChanged()
    }
}
