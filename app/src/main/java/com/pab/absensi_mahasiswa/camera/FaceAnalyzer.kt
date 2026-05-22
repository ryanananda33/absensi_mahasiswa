package com.pab.absensi_mahasiswa.camera

import android.annotation.SuppressLint
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import kotlin.math.pow
import kotlin.math.sqrt

class FaceAnalyzer(
    private val onActionDetected: (Action, Double) -> Unit
) : ImageAnalysis.Analyzer {

    enum class Action { BLINK, SMILE, ALIGNED }

    private val options = FaceDetectorOptions.Builder()
        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
        .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
        .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
        .setContourMode(FaceDetectorOptions.CONTOUR_MODE_ALL)
        .setMinFaceSize(0.25f) // Mahasiswa harus benar-benar dekat (fokus)
        .enableTracking()
        .build()

    private val detector = FaceDetection.getClient(options)

    @SuppressLint("UnsafeOptInUsageError")
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image ?: return
        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
        
        detector.process(image)
            .addOnSuccessListener { faces ->
                for (face in faces) {
                    // 1. ANALISIS ANTI-KEMBAR (Rasio Biometrik Anatomis)
                    // Kita ambil rasio jarak Pupil Mata terhadap Lebar Mulut
                    val leftEye = face.getLandmark(com.google.mlkit.vision.face.FaceLandmark.LEFT_EYE)?.position
                    val rightEye = face.getLandmark(com.google.mlkit.vision.face.FaceLandmark.RIGHT_EYE)?.position
                    val mouthLeft = face.getLandmark(com.google.mlkit.vision.face.FaceLandmark.MOUTH_LEFT)?.position
                    val mouthRight = face.getLandmark(com.google.mlkit.vision.face.FaceLandmark.MOUTH_RIGHT)?.position

                    var bioScore = 0.0
                    if (leftEye != null && rightEye != null && mouthLeft != null && mouthRight != null) {
                        val eyeDist = calculateDistance(leftEye.x, leftEye.y, rightEye.x, rightEye.y)
                        val mouthDist = calculateDistance(mouthLeft.x, mouthLeft.y, mouthRight.x, mouthRight.y)
                        bioScore = eyeDist / mouthDist // Rasio ini unik per individu
                    }

                    // 2. DETEKSI POSISI (ALIGNED)
                    if (face.headEulerAngleY in -10f..10f && face.headEulerAngleZ in -10f..10f) {
                        onActionDetected(Action.ALIGNED, bioScore)
                    }

                    // 3. DETEKSI KEDIP (LIVENESS)
                    val leftProb = face.leftEyeOpenProbability ?: 1f
                    val rightProb = face.rightEyeOpenProbability ?: 1f
                    if (leftProb < 0.15f && rightProb < 0.15f) {
                        onActionDetected(Action.BLINK, bioScore)
                    }

                    // 4. DETEKSI SENYUM (LIVENESS)
                    val smileProb = face.smilingProbability ?: 0f
                    if (smileProb > 0.90f) {
                        onActionDetected(Action.SMILE, bioScore)
                    }
                }
            }
            .addOnCompleteListener { imageProxy.close() }
    }

    private fun calculateDistance(x1: Float, y1: Float, x2: Float, y2: Float): Double {
        return sqrt((x2 - x1).toDouble().pow(2) + (y2 - y1).toDouble().pow(2))
    }
}
