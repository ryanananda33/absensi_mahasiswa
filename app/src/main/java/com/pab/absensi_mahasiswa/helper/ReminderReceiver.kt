package com.pab.absensi_mahasiswa.helper

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.pab.absensi_mahasiswa.R
import com.pab.absensi_mahasiswa.ui.absensi.AbsensiActivity

class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        showNotification(context)
        
        // Master Logic: Jadwalkan ulang untuk hari berikutnya (Repeating)
        AlarmHelper.scheduleAttendanceReminder(context)
    }

    private fun showNotification(context: Context) {
        val channelId = "absensi_reminder_channel"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId, "Pengingat Absensi",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifikasi untuk mengingatkan jam absensi kuliah"
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Action: Langsung buka halaman Absen saat notifikasi diklik
        val mainIntent = Intent(context, AbsensiActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context, 0, mainIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("WAKTUNYA ABSENSI!")
            .setContentText("Matakuliah sedang dimulai. Klik di sini untuk melakukan presensi biometrik.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setColor(context.getColor(R.color.primary))
            .build()

        notificationManager.notify(101, notification)
    }
}
