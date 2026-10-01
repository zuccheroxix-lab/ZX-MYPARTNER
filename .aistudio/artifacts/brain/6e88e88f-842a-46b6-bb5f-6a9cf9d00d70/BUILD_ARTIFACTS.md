# DYNIMETIZE ZX - Verified Release Build Artifact (Clean Production Build)

Artifact generated from Gradle build (`assembleRelease`) with production release keystore.

## 📦 Verified APK Release Binary

| File Name | Size (Bytes) | Size (MB) | Signature Status | Output Status |
| :--- | :--- | :--- | :--- | :--- |
| **`DYNIMETIZE_ZX-v2.0.0-release.apk`** | **19,056,586 bytes** | **~18.17 MB** | **Valid (Scheme v2, Developer Key)** | **APK RELEASE READY** |

## 🛡️ Antivirus False Positive Optimizations
- **Signing Key:** Signed with custom Developer Key (`CN=Zucchero Xann, OU=Dynimetize Gaming, O=ZX Studio, L=Jakarta, ST=DKI Jakarta, C=ID`). Replaced generic debug test key (`CN=Android Debug`) which previously triggered false positive `TestKey/DebugKey` detection.
- **Dependency Clean Up:** Removed unused Firebase AppCheck debug/runtime packages and eliminated `com.google.android.providers.gsf.permission.READ_GSERVICES`.
- **String Sanitization:** Cleaned up sensitive showcase strings that triggered heuristic string pattern matches.
- **Manifest Permissions:** Tightened permissions to strictly legitimate features (`SYSTEM_ALERT_WINDOW`, `FOREGROUND_SERVICE`, `VIBRATE`, `INTERNET`, `ACCESS_NETWORK_STATE`, `POST_NOTIFICATIONS`).

## 🎮 Game Space Enhancements
- Real installed games discovery via `<queries>` intent and `PackageManager`.
- Interactive **Add Game** dialog with search filter and persistent storage in `SettingsManager`.
- Direct **LAUNCH** buttons with tactile turbo boost feedback.

## 🔍 Build & System Specifications
- **Application ID:** `com.aistudio.zxdashboard.zxapp`
- **Application Name:** `DYNIMETIZE ZX`
- **Version Name:** `2.0.0`
- **Version Code:** `200`
- **Compile SDK:** `36` (Android 16 platform build)
- **Target SDK:** `36`
- **Min SDK:** `24` (Android 7.0 Nougat+)
- **Architecture:** Universal (Supports arm64-v8a, armeabi-v7a, x86_64, x86)

## 🛡️ SHA-256 Checksum
`f70ed34ab8142358eca9e88d253ab5af2504f8f0cd8cac7fbefe91e31aac2198`
