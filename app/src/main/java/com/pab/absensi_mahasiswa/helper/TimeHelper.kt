package com.pab.absensi_mahasiswa.helper

import android.content.Context
import android.provider.Settings
import java.text.SimpleDateFormat
import java.util.*

object TimeHelper {
    private const val TIME_WINDOW_MINUTES = 20

    // Master Security: Cek apakah user menggunakan waktu manual (untuk curang)
    fun isUsingManualTime(context: Context): Boolean {
        return try {
            Settings.Global.getInt(context.contentResolver, Settings.Global.AUTO_TIME) == 0
        } catch (e: Exception) {
            false
        }
    }

    fun isLate(startTimeStr: String): Boolean {
        return try {
            val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
            val now = Calendar.getInstance()
            val startTime = sdf.parse(startTimeStr) ?: return false
            
            val startCalendar = Calendar.getInstance().apply {
                time = startTime
                set(Calendar.YEAR, now.get(Calendar.YEAR))
                set(Calendar.MONTH, now.get(Calendar.MONTH))
                set(Calendar.DAY_OF_MONTH, now.get(Calendar.DAY_OF_MONTH))
            }

            val diffMinutes = (now.timeInMillis - startCalendar.timeInMillis) / (60 * 1000)
            diffMinutes > TIME_WINDOW_MINUTES
        } catch (e: Exception) {
            true
        }
    }

    fun getCurrentTime(): String {
        return SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
    }
}
