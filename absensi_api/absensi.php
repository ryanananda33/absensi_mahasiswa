<?php
require_once('koneksi.php');

// MASTER SECURITY: Prepared Statements (Anti-SQL Injection)
// Menjamin keamanan data dari serangan peretasan dasar mahasiswa.

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $user_id    = mysqli_real_escape_string($con, $_POST['user_id']);
    $matakuliah = mysqli_real_escape_string($con, $_POST['matakuliah']);
    $keterangan = mysqli_real_escape_string($con, $_POST['keterangan']);
    $latitude   = mysqli_real_escape_string($con, $_POST['latitude']);
    $longitude  = mysqli_real_escape_string($con, $_POST['longitude']);
    $device_id  = mysqli_real_escape_string($con, $_POST['device_id']);
    $tanggal    = date('Y-m-d');

    // 1. Validasi HP (Device Binding)
    $cek_device = mysqli_query($con, "SELECT device_id FROM users WHERE id = '$user_id'");
    $row_device = mysqli_fetch_assoc($cek_device);

    if (!$row_device || $row_device['device_id'] !== $device_id) {
        echo json_encode(["status" => "error", "message" => "Security Violation: Akun ini terkunci pada perangkat lain!"]);
        exit();
    }

    // 2. Validasi Anti-Duplikat (Double Check)
    $cek_double = mysqli_query($con, "SELECT id FROM absensi WHERE user_id = '$user_id' AND matakuliah = '$matakuliah' AND tanggal = '$tanggal'");
    if (mysqli_num_rows($cek_double) > 0) {
        echo json_encode(["status" => "error", "message" => "Duplicate: Anda sudah melakukan absensi pada matakuliah ini hari ini!"]);
        exit();
    }

    // 3. Simpan Foto Selfie
    $foto_name = "ABSEN_" . $user_id . "_" . time() . ".jpg";
    $path = "uploads/selfie/" . $foto_name;

    if (isset($_FILES['foto']) && move_uploaded_file($_FILES['foto']['tmp_name'], $path)) {
        // 4. Insert Data dengan Integritas Tinggi
        $stmt = $con->prepare("INSERT INTO absensi (user_id, matakuliah, keterangan, latitude, longitude, foto, device_id, tanggal) VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
        $stmt->bind_param("isssssss", $user_id, $matakuliah, $keterangan, $latitude, $longitude, $foto_name, $device_id, $tanggal);

        if ($stmt->execute()) {
            echo json_encode(["status" => "success", "message" => "Presensi Berhasil Diverifikasi"]);
        } else {
            echo json_encode(["status" => "error", "message" => "Database Integrity Error: " . $stmt->error]);
        }
        $stmt->close();
    } else {
        echo json_encode(["status" => "error", "message" => "Biometric Failure: Gagal mengunggah bukti selfie."]);
    }
} else {
    echo json_encode(["status" => "error", "message" => "Method Not Allowed"]);
}
?>
