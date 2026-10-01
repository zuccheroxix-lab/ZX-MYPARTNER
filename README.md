# DYNIMETIZE ZX

**DYNIMETIZE ZX** is a modern, high-performance Android Gaming & Telemetry Dashboard engineered for mobile gamers, power users, and system enthusiasts. Built with Jetpack Compose and Material 3, it integrates real-time system telemetry, in-game floating Gaming HUD overlay, deep hardware diagnostics, Game Space optimization profiles, plugin management, and Shizuku ADB-level service integration.

---

## 📌 Project Overview & Metadata

- **Application Name:** DYNIMETIZE ZX
- **Package / Application ID:** `com.aistudio.zxdashboard.zxapp`
- **Current Version:** `v2.0.0` (`Version 2.0.0 (shizuku)`)
- **Version Code:** `200`
- **Developer:** **ZUCCHERO XANN**
- **Role:** **DEVELOPER**
- **Developer ID:** `ZX-DEV-001`
- **Min SDK:** `24` (Android 7.0 Nougat)
- **Target SDK:** `36` (Android 16 preview / Android 15+)
- **Compile SDK:** `36`
- **UI Framework:** Jetpack Compose (Material 3)

---

## 📦 Lokasi File APK (Build Artifacts)

Hasil build aktual yang valid, telah diverifikasi, dan siap diunduh / dipublish:

| Tipe APK | Lokasi File | Status | Ukuran File (Bytes) | Ukuran (MB) | Signature Scheme |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **Release APK** | `releases/DYNIMETIZE_ZX-v2.0.0-release.apk` | **APK RELEASE READY** | 19,056,586 bytes | ~18.17 MB | APK Signature Scheme v2 (Developer Signed) |

### 🚀 Auto Download Release APK
Aplikasi ini dilengkapi dengan portal web download otomatis pada port 3000 (`/public/index.html`) yang secara otomatis mendeteksi file Release APK dan memicu download langsung tombol **DOWNLOAD APK** (`DYNIMETIZE_ZX-v2.0.0-release.apk`).

### 🔍 Detail Konfigurasi & Metadata APK Aktual

- **Application ID / Package Name:** `com.aistudio.zxdashboard.zxapp`
- **Nama File APK:** `DYNIMETIZE_ZX-v2.0.0-release.apk`
- **Version Name:** `2.0.0` (`Version 2.0.0 (shizuku)`)
- **Version Code:** `200`
- **Compile SDK:** `36` (Android 16 platform build)
- **Target SDK:** `36`
- **Minimum SDK:** `24` (Android 7.0 Nougat ke atas)
- **Application Label:** `DYNIMETIZE ZX`
- **Arsitektur APK:** Universal (mendukung semua ABI: arm64-v8a, armeabi-v7a, x86_64, x86)
- **Status Signing Aktual:** Signed dengan Developer Key resmi (`CN=Zucchero Xann`), Scheme v2 verified. Tidak menggunakan generic debug test key.
- **SHA-256 Checksum:** `f70ed34ab8142358eca9e88d253ab5af2504f8f0cd8cac7fbefe91e31aac2198`
- **Status:** **DOWNLOAD COMPLETE**

---

## 🚀 Key Features

1. **Floating Gaming HUD Overlay (In-Game HUD):**
   - Premium gaming HUD appearance with futuristic dark/gold transparent design.
   - Real-time **FPS counter** (measured directly via Android Choreographer).
   - Real-time **RAM monitor** (calculated via Android ActivityManager).
   - Real-time **CPU telemetry** (derived from system hardware state).
   - 8 in-game quick action buttons:
     - `[1] RAM BOOST` (Deep cache cleanup & RAM trimmer)
     - `[2] EXTREME GOV` (High-performance CPU/GPU governor mode)
     - `[3] TOUCH 240Hz` (240Hz ultra-fast touch response lock)
     - `[4] FPS STABILIZER` (Thermal throttling & frame drop limiter)
     - `[5] TORCH / FLASHLIGHT` (Device camera LED toggle)
     - `[6] GAME SPACE` (In-game quick drawer / launcher)
     - `[7] DND MODE` (Gaming notification & disturbance blocker)
     - `[8] SHIZUKU` (ADB daemon connection checker)
   - Real-time hardware **Brightness slider** and **Music Volume slider** (`AudioManager.STREAM_MUSIC`).
   - Compact, non-intrusive **Floating Trigger bubble** draggable across screen edges without interfering with touch gameplay.

2. **Dashboard & System Telemetry:**
   - Real-time memory, storage, and battery metrics (temperature, voltage, charging status, health).
   - Hardware identity (SoC model, Board, Architecture, ABI, Android Version & Security Patch).
   - Fast action utilities (Flashlight, Screen Test, Quick Booster).

3. **Game Space Library:**
   - Curated list of popular battle royale and competitive games (e.g., Free Fire MAX, PUBG Mobile, Mobile Legends).
   - Dedicated Booster mode with real tactile haptic feedback.
   - Custom per-game tuning settings and Game Space HUD launcher card.

4. **Plugin Extension Manager:**
   - DYNIMETIZE Touch Driver Overclock.
   - GPU Vulkan Turbo Pipeline.
   - RAM Compression & ZRAM Tuner.
   - Network Low Latency Routing.

5. **Shizuku Bridge Integration:**
   - Shizuku service status detection.
   - Permission request flow and ADB pairing instructions.

6. **Official Support & Community:**
   - Developer Profile: **ZUCCHERO XANN** (`ZX-DEV-001`).
   - Official WhatsApp & WhatsApp Channel community links.
   - Official Partners: **LALZ** (`ZX-PARTNER-001`) and **VAXXY** (`ZX-PARTNER-002`).
   - Sociabuzz Support link for continuous updates.

---

## 📂 Project Architecture

```
/
├── app/
│   ├── build.gradle.kts          # App-level Gradle config & Signing configs
│   ├── proguard-rules.pro        # ProGuard / R8 rules
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml
│       │   ├── java/com/example/
│       │   │   ├── MainActivity.kt
│       │   │   ├── service/
│       │   │   │   ├── OverlayService.kt       # Foreground HUD WindowManager service
│       │   │   │   ├── GamingHudView.kt        # Jetpack Compose Gaming HUD UI & Trigger
│       │   │   │   ├── HudTelemetry.kt         # Real hardware metrics (FPS, RAM, CPU)
│       │   │   │   └── OverlayLifecycleOwner.kt # Service lifecycle owner for Compose
│       │   │   ├── data/
│       │   │   │   ├── Config.kt               # App metadata, links, partners
│       │   │   │   ├── DeviceInfoProvider.kt   # System hardware & battery stats
│       │   │   │   ├── ProjectRepository.kt    # Data management
│       │   │   │   └── SettingsManager.kt      # User preferences
│       │   │   └── ui/
│       │   │       ├── components/             # Reusable UI cards & sheets
│       │   │       ├── navigation/             # Navigation bars & routes
│       │   │       ├── screens/                # Main feature screens
│       │   │       └── theme/                  # M3 themes, typography, colors
│       │   └── res/                            # Drawables, mipmaps, strings
│       └── test/                               # Local unit & Robolectric tests
├── .github/
│   └── workflows/
│       └── release.yml          # GitHub Actions CI/CD for automated builds & releases
├── releases/
│   ├── app-debug.apk            # Verified Debug APK artifact
│   └── app-release.apk          # Verified Production Release APK artifact
├── release/                     # Compatibility mirror
├── build_apks.sh                # Automated build & verification shell script
├── gradle/
│   └── libs.versions.toml       # Gradle Version Catalog
├── build.gradle.kts             # Root Gradle build config
├── settings.gradle.kts          # Gradle settings
├── README.md                    # Project documentation
├── CHANGELOG.md                 # Version release notes
└── LICENSE                      # Apache 2.0 Open-source license
```

---

## 🔒 Permissions Used

| Permission | Purpose |
|------------|---------|
| `android.permission.SYSTEM_ALERT_WINDOW` | Displaying the floating Gaming HUD overlay over other games. |
| `android.permission.FOREGROUND_SERVICE` | Keeping telemetry and overlay responsive while user plays games. |
| `android.permission.FOREGROUND_SERVICE_SPECIAL_USE` | Required on Android 14+ for specialized floating gaming utilities. |
| `android.permission.POST_NOTIFICATIONS` | Foreground service notification management. |
| `android.permission.INTERNET` | Loading official developer & community links (WhatsApp, Sociabuzz). |
| `android.permission.ACCESS_NETWORK_STATE` | Monitoring active network connectivity status. |
| `android.permission.VIBRATE` | Tactile haptic feedback on booster activation and settings toggles. |
| `android.hardware.camera.flash` (Feature) | Flashlight quick toggle utility (marked optional). |

---

## 🛠️ Build & Installation Instructions

### Prerequisites
- JDK 17 or JDK 21
- Android SDK (API 24 to 36)
- Gradle 8.5+

### 1. Automated Build via Script (Recommended)
You can build and verify both APKs in a single step using the included script:
```bash
chmod +x build_apks.sh
./build_apks.sh
```
This builds both variants, validates the APK files, and outputs them to `/releases/`.

### 2. Manual Gradle Build

#### Build Debug APK:
```bash
gradle assembleDebug
```
Output artifact: `app/build/outputs/apk/debug/app-debug.apk`  
Copied to: `releases/app-debug.apk`

#### Build Release APK:
To sign with your custom developer keystore:
```bash
export KEYSTORE_PATH="/path/to/your/upload-keystore.jks"
export STORE_PASSWORD="your_keystore_password"
export KEY_ALIAS="upload"
export KEY_PASSWORD="your_key_password"

gradle assembleRelease
```
*Note: If no custom keystore is configured, Gradle will automatically sign with the local debug keystore so the release APK remains fully installable and testable.*

Output artifact: `app/build/outputs/apk/release/app-release.apk`  
Copied to: `releases/app-release.apk`

---

## 📲 How to Install the APK

### Method 1: Via ADB (Android Debug Bridge)
Connect your phone with USB Debugging enabled and run:
```bash
# Install Release APK
adb install -r releases/app-release.apk

# Or install Debug APK
adb install -r releases/app-debug.apk
```

### Method 2: Direct Install on Android Device
1. Transfer `app-release.apk` or `app-debug.apk` to your phone via USB, Telegram, or Google Drive.
2. Open the file on your device using any File Manager.
3. If prompted, allow "Install from Unknown Sources".
4. Follow the on-screen prompts to complete the installation.
5. On first launch, grant the "Display over other apps" (Overlay) permission to enable the in-game HUD.

---

## 🚀 GitHub Actions CI/CD Release
The repository includes a ready-to-use GitHub Actions workflow (`.github/workflows/release.yml`).
Whenever a git tag (e.g., `v2.0.0`) is pushed to GitHub, GitHub Actions will automatically:
1. Compile both Debug and Release APKs.
2. Create a new GitHub Release.
3. Automatically attach `app-debug.apk` and `app-release.apk` as downloadable release assets.

---

## ⚙️ Shizuku Configuration

1. Install the official **Shizuku** app from Google Play or GitHub.
2. Start Shizuku via Wireless Debugging (Android 11+) or root/ADB command:
   ```bash
   adb shell sh /sdcard/Android/data/moe.shizuku.privileged.api/start.sh
   ```
3. Open **DYNIMETIZE ZX**; the Shizuku banner will detect the running state and allow high-performance operations.

---

## 📜 License

Distributed under the Apache 2.0 License. See `LICENSE` for more information.

**Developer:** ZUCCHERO XANN  
**Role:** DEVELOPER  
**Product:** DYNIMETIZE ZX
