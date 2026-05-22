package com.pab.absensi_mahasiswa.helper

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.pab.absensi_mahasiswa.session.SessionManager

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val session = SessionManager(context)
            if (session.isLoggedIn()) {
                // Jika masih login, nyalakan lagi alarmnya
                AlarmHelper.scheduleAttendanceReminder(context)
            }
        }
    }
}
