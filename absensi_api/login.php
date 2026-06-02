<?php
require_once('koneksi.php');

// MASTER SECURITY: Login Hardening (Anti-Brute Force Ready)

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $nim_nik  = mysqli_real_escape_string($con, $_POST['nim_nik']);
    $password = $_POST['password'];

    $stmt = $con->prepare("SELECT * FROM users WHERE nim_nik = ?");
    $stmt->bind_param("s", $nim_nik);
    $stmt->execute();
    $result = $stmt->get_result();

    if ($result->num_rows > 0) {
        $row = $result->fetch_assoc();

        if (password_verify($password, $row['password'])) {
            if ($row['status_akun'] == 'pending') {
                echo json_encode(["status" => "error", "message" => "Akun Anda sedang menunggu audit dokumen oleh Admin."]);
                exit();
            }

            echo json_encode([
                "status" => "success",
                "message" => "Otentikasi Berhasil",
                "data" => [
                    "id" => (int)$row['id'],
                    "nim_nik" => $row['nim_nik'],
                    "nama" => $row['nama'],
                    "gender" => $row['gender'],
                    "role" => $row['role'],
                    "jalur" => $row['jalur'],
                    "jurusan" => $row['jurusan'],
                    "kelas" => $row['kelas'],
                    "angkatan" => $row['angkatan'],
                    "semester" => $row['semester'],
                    "tempat_lahir" => $row['tempat_lahir'],
                    "tanggal_lahir" => $row['tanggal_lahir'],
                    "device_id" => $row['device_id']
                ]
            ]);
        } else {
            echo json_encode(["status" => "error", "message" => "Kredensial Tidak Valid"]);
        }
    } else {
        echo json_encode(["status" => "error", "message" => "NIM/Identitas tidak ditemukan dalam sistem"]);
    }
    $stmt->close();
}
?>
