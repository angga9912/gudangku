# GudangKu

Aplikasi manajemen inventori gudang sederhana (Android native, Kotlin + Jetpack Compose + Room).

## Build APK otomatis via GitHub Actions

Repo ini sudah dilengkapi workflow di `.github/workflows/build.yml` yang akan
build APK debug secara otomatis setiap kali ada push ke branch `main`/`master`,
setiap pull request, atau saat dijalankan manual.

### Langkah-langkah

1. Buat repository baru di GitHub (public atau private).
2. Push project ini ke repo tersebut:
   ```bash
   cd GudangKu
   git init
   git add .
   git commit -m "Initial commit: GudangKu app"
   git branch -M main
   git remote add origin https://github.com/USERNAME/NAMA_REPO.git
   git push -u origin main
   ```
3. Buka tab **Actions** di repo GitHub Anda. Workflow "Build APK" akan
   otomatis berjalan setelah push.
4. Setelah selesai (centang hijau), klik run workflow tersebut, lalu di
   bagian bawah halaman ada **Artifacts** → unduh `GudangKu-debug-apk.zip`.
   Di dalamnya ada file `app-debug.apk` yang bisa langsung diinstal di HP
   Android (aktifkan "Install dari sumber tidak dikenal" jika diminta).

### Menjalankan workflow secara manual

Jika ingin build ulang tanpa push kode baru:
Tab **Actions** → pilih workflow **Build APK** di sidebar kiri → klik
**Run workflow** → pilih branch → **Run workflow**.

### Build APK release (bertanda tangan) — opsional

Workflow saat ini hanya build APK **debug** (tidak perlu keystore, langsung
bisa dipakai untuk testing). Jika nanti butuh APK **release** untuk
diunggah ke Play Store atau dibagikan resmi, beri tahu saya — perlu
tambahan:
- Keystore (`.jks`) yang disimpan sebagai **GitHub Secret**, bukan
  di-commit ke repo.
- Konfigurasi signing di `app/build.gradle.kts`.
- Step tambahan di workflow untuk decode keystore dari secret lalu
  menjalankan `gradle assembleRelease`.

## Menjalankan project secara lokal di Android Studio

1. Buka Android Studio → **Open** → pilih folder `GudangKu`.
2. Tunggu Gradle Sync selesai (Android Studio akan otomatis membuatkan
   `gradlew`/`gradle-wrapper.jar` jika belum ada).
3. Klik **Run ▶** pada device/emulator (min. Android 7.0 / API 24).
