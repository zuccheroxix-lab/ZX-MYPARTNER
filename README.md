# DYNIMETIZE ZX

**DYNIMETIZE ZX** is a modern, high-performance Android Gaming & Telemetry Dashboard engineered for mobile gamers, power users, and system enthusiasts. Built with Jetpack Compose and Material 3, it integrates system telemetry, deep hardware diagnostics, Game Space optimization profiles, plugin management, and Shizuku ADB-level service integration.

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

## 🚀 Key Features

1. **Dashboard & Telemetry:**
   - Real-time CPU usage, battery temperature, battery health, and memory stats.
   - Device Hardware identity (SoC model, Board, Architecture, ABI, Android Version & Security Patch).
   - Fast action tiles (Flashlight, Screen Test, Quick Booster).

2. **Game Space Library:**
   - Curated list of popular battle royale and competitive games (e.g., Free Fire, PUBG Mobile, Mobile Legends, Genshin Impact).
   - Dedicated Booster mode with real tactile haptic feedback.
   - Custom per-game tuning settings.

3. **Plugin Extension Manager:**
   - DYNIMETIZE Touch Driver Overclock.
   - GPU Vulkan Turbo Pipeline.
   - RAM Compression & ZRAM Tuner.
   - Network Low Latency Routing.

4. **Shizuku Bridge Integration:**
   - Shizuku service status detection.
   - Permission request flow and ADB pairing instructions.

5. **Official Support & Community:**
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
├── gradle/
│   └── libs.versions.toml       # Gradle Version Catalog
├── build.gradle.kts             # Root Gradle build config
├── settings.gradle.kts          # Gradle settings
├── README.md                    # Project documentation
├── CHANGELOG.md                 # Version release notes
└── LICENSE                      # Open-source license
```

---

## 🔒 Permissions Used

The application adheres to the principle of least privilege:

| Permission | Purpose |
|------------|---------|
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

### 1. Build Debug APK
```bash
gradle assembleDebug
```
Output artifact: `app/build/outputs/apk/debug/app-debug.apk`

### 2. Build Release APK
To sign with your custom developer keystore, configure the environment variables:
```bash
export KEYSTORE_PATH="/path/to/your/upload-keystore.jks"
export STORE_PASSWORD="your_keystore_password"
export KEY_ALIAS="upload"
export KEY_PASSWORD="your_key_password"

gradle assembleRelease
```
*Note: If no custom keystore is provided, Gradle will safely sign the release build using the local debug keystore for development/testing convenience.*

Output artifact: `app/build/outputs/apk/release/app-release.apk`

### 3. Install APK via ADB
```bash
adb install -r app/build/outputs/apk/release/app-release.apk
```

---

## ⚙️ Shizuku Configuration

1. Install the official **Shizuku** app from Google Play or GitHub.
2. Start Shizuku via Wireless Debugging (Android 11+) or root/ADB command:
   ```bash
   adb shell sh /sdcard/Android/data/moe.shizuku.privileged.api/start.sh
   ```
3. Open **DYNIMETIZE ZX**; the Shizuku banner will detect the running state and allow high-performance operations.

---

## 🔧 Basic Troubleshooting

- **Build error about Keystore:** Ensure `KEYSTORE_PATH` points to a valid `.jks` file or unset it to use the default fallback key.
- **Haptic feedback not working:** Check device Settings > Sound & Vibration > Touch Feedback to ensure vibration is globally enabled.
- **Sociabuzz or WhatsApp links not opening:** Ensure a web browser or WhatsApp application is installed on the target device.

---

## 📜 License

Distributed under the Apache 2.0 License. See `LICENSE` for more information.

**Developer:** ZUCCHERO XANN  
**Role:** DEVELOPER  
**Product:** DYNIMETIZE ZX
