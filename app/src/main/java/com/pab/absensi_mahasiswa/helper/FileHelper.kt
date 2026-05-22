package com.pab.absensi_mahasiswa.helper

import android.content.Context
import java.io.File

object FileHelper {
    // Master Logic: Membersihkan semua file sementara (KTM, Selfie, Bukti Absen)
    fun clearAppCache(context: Context) {
        try {
            val dir = context.cacheDir
            deleteDir(dir)
        } catch (e: Exception) {
            // Silently fail
        }
    }

    private fun deleteDir(dir: File?): Boolean {
        if (dir != null && dir.isDirectory) {
            val children = dir.list()
            if (children != null) {
                for (i in children.indices) {
                    val success = deleteDir(File(dir, children[i]))
                    if (!success) return false
                }
            }
            return dir.delete()
        } else if (dir != null && dir.isFile) {
            return dir.delete()
        }
        return false
    }
}
