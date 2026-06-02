<?php
require_once('koneksi.php');

// 1. Logika Approve Akun Pending (KTM Hilang)
if(isset($_GET['approve'])) {
    $id = mysqli_real_escape_string($con, $_GET['approve']);
    mysqli_query($con, "UPDATE users SET status_akun = 'aktif' WHERE id = '$id'");
    header("Location: admin.php?msg=approved");
}

// 2. Logika Hapus Absensi Palsu
if(isset($_GET['hapus_absen'])) {
    $id = mysqli_real_escape_string($con, $_GET['hapus_absen']);
    mysqli_query($con, "DELETE FROM absensi WHERE id = '$id'");
    header("Location: admin.php?msg=deleted");
}
?>
<!DOCTYPE html>
<html lang="id">
<head>
    <meta charset="UTF-8">
    <title>MASTER ADMIN - Sistem Absensi Biometrik</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.5/font/bootstrap-icons.css">
    <style>
        :root { --master-navy: #0A2647; --master-blue: #144272; }
        body { background-color: #f0f2f5; font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; }
        .navbar-master { background: linear-gradient(135deg, var(--master-navy), var(--master-blue)); color: white; padding: 1.5rem; }
        .card { border: none; border-radius: 15px; box-shadow: 0 4px 20px rgba(0,0,0,0.08); margin-bottom: 2rem; }
        .table img { border-radius: 8px; object-fit: cover; cursor: zoom-in; transition: 0.3s; }
        .table img:hover { transform: scale(3); position: relative; z-index: 100; }
        .badge-pending { background-color: #ff9800; color: #fff; }
        .status-dot { height: 10px; width: 10px; background-color: #4caf50; border-radius: 50%; display: inline-block; margin-right: 5px; }
    </style>
</head>
<body>

<nav class="navbar navbar-master mb-4">
    <div class="container d-flex justify-content-between align-items-center">
        <div>
            <h3 class="mb-0 fw-bold"><i class="bi bi-shield-lock-fill me-2"></i>PUSAT KONTROL AKADEMIK</h3>
            <small class="opacity-75">Sistem Verifikasi Biometrik & Geofencing v1.1</small>
        </div>
        <div class="text-end">
            <span class="d-block">Administrator Utama</span>
            <span class="badge bg-light text-dark"><span class="status-dot"></span>SERVER ONLINE</span>
        </div>
    </div>
</nav>

<div class="container">

    <!-- SECTION 1: VERIFIKASI MAHASISWA BARU (KTM HILANG/KRS) -->
    <div class="card">
        <div class="card-header bg-white border-bottom p-3">
            <h5 class="mb-0 fw-bold text-success"><i class="bi bi-person-check-fill me-2"></i>Verifikasi Dokumen Pendaftaran (Pending)</h5>
        </div>
        <div class="card-body p-0">
            <div class="table-responsive">
                <table class="table table-hover align-middle mb-0">
                    <thead class="table-light">
                        <tr>
                            <th>Identitas</th>
                            <th>Jalur & Prodi</th>
                            <th>Dokumen (KTM/KRS)</th>
                            <th>Selfie Biometrik</th>
                            <th class="text-center">Aksi</th>
                        </tr>
                    </thead>
                    <tbody>
                        <?php
                        $res = mysqli_query($con, "SELECT * FROM users WHERE status_akun = 'pending' ORDER BY id DESC");
                        if(mysqli_num_rows($res) == 0) echo "<tr><td colspan='5' class='text-center py-4 text-muted'>Tidak ada antrean verifikasi saat ini.</td></tr>";
                        while($u = mysqli_fetch_assoc($res)) { ?>
                        <tr>
                            <td>
                                <strong><?= htmlspecialchars($u['nama']) ?></strong><br>
                                <code class="text-primary"><?= $u['nim_nik'] ?></code>
                            </td>
                            <td>
                                <span class="badge bg-secondary"><?= $u['jalur'] ?></span><br>
                                <small><?= $u['jurusan'] ?></small>
                            </td>
                            <td>
                                <img src="uploads/ktm/<?= $u['foto_ktm'] ?>" width="100" title="Klik untuk perbesar">
                                <div class="mt-1 small text-muted"><?= $u['doc_type'] ?></div>
                            </td>
                            <td><img src="uploads/selfie/<?= $u['foto_selfie'] ?>" width="60"></td>
                            <td class="text-center">
                                <a href="admin.php?approve=<?= $u['id'] ?>" class="btn btn-success fw-bold rounded-pill px-4" onclick="return confirm('Apakah Anda sudah memvalidasi kecocokan Nama, NIM, dan Foto?')">SETUJUI AKUN</a>
                            </td>
                        </tr>
                        <?php } ?>
                    </tbody>
                </table>
            </div>
        </div>
    </div>

    <!-- SECTION 2: LOG KEHADIRAN MAHASISWA (REAL-TIME) -->
    <div class="card">
        <div class="card-header bg-white border-bottom p-3">
            <h5 class="mb-0 fw-bold text-primary"><i class="bi bi-clock-history me-2"></i>Log Kehadiran Mahasiswa (Real-time Audit)</h5>
        </div>
        <div class="card-body p-0">
            <div class="table-responsive">
                <table class="table table-hover align-middle mb-0">
                    <thead class="table-light">
                        <tr>
                            <th>Mahasiswa</th>
                            <th>Mata Kuliah</th>
                            <th>Status</th>
                            <th>Bukti Absen</th>
                            <th>Audit Lokasi</th>
                            <th>Waktu Presensi</th>
                            <th>Aksi</th>
                        </tr>
                    </thead>
                    <tbody>
                        <?php
                        $query = "SELECT a.*, u.nama, u.nim_nik FROM absensi a JOIN users u ON a.user_id = u.id ORDER BY a.waktu_absen DESC LIMIT 50";
                        $res = mysqli_query($con, $query);
                        if(mysqli_num_rows($res) == 0) echo "<tr><td colspan='7' class='text-center py-4 text-muted'>Belum ada data absensi masuk hari ini.</td></tr>";
                        while($r = mysqli_fetch_assoc($res)) { ?>
                        <tr>
                            <td>
                                <strong><?= htmlspecialchars($r['nama']) ?></strong><br>
                                <small class="text-muted"><?= $r['nim_nik'] ?></small>
                            </td>
                            <td><?= htmlspecialchars($r['matakuliah']) ?></td>
                            <td>
                                <?php if($r['keterangan'] == 'Terlambat') { ?>
                                    <span class="badge bg-danger">TERLAMBAT</span>
                                <?php } else { ?>
                                    <span class="badge bg-success">HADIR</span>
                                <?php } ?>
                            </td>
                            <td><img src="uploads/selfie/<?= $r['foto'] ?>" width="60"></td>
                            <td>
                                <a href="https://www.google.com/maps?q=<?= $r['latitude'] ?>,<?= $r['longitude'] ?>" target="_blank" class="btn btn-sm btn-outline-info">
                                    <i class="bi bi-geo-alt-fill"></i> Peta
                                </a>
                            </td>
                            <td><small><?= $r['waktu_absen'] ?></small></td>
                            <td>
                                <a href="admin.php?hapus_absen=<?= $r['id'] ?>" class="text-danger" onclick="return confirm('Hapus data absensi ini?')"><i class="bi bi-trash"></i></a>
                            </td>
                        </tr>
                        <?php } ?>
                    </tbody>
                </table>
            </div>
        </div>
    </div>

</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
