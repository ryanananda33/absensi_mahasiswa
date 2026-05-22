package com.pab.absensi_mahasiswa.camera

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

class SelfieProcessor {

    fun compressImage(imageFile: File): File {
        val bitmap = BitmapFactory.decodeFile(imageFile.absolutePath)
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, stream)
        val byteArray = stream.toByteArray()

        val compressedFile = File(imageFile.parent, "compressed_" + imageFile.name)
        val fos = FileOutputStream(compressedFile)
        fos.write(byteArray)
        fos.flush()
        fos.close()
        return compressedFile
    }
}
