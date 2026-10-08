# Changelog

All notable changes to the **DYNIMETIZE ZX** project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [2.2.0] - 2026-10-08

### Added
- Official **DOWNLOAD APK TERBARU** direct release build (`DYNIMETIZE_ZX-v2.2.0-release.apk`, VersionCode: 220).
- Dedicated in-app update & APK download card in **Settings** and **Home** screens with direct GitHub Release link.
- Real Feedback System with live network transmission to server, real HTTP status code handling (Sending / Sent / Failed / Offline), zero fake status.
- Re-architected 4 Primary Navigation pages:
  - **HOME:** Clean Dashboard, hardware telemetry, quick tool shortcuts, and Shizuku status.
  - **GAME:** Game Space library, Game Profiles, and Sensitivity GameLab.
  - **TOOLS:** DPI Manager, Resolution Manager, DNS Kustom, and Crosshair customization.
  - **SETTINGS:** UI Color accent presets, Shizuku ADB bridge, permissions, and Feedback submission.
- Seamless horizontal swiping across all 4 pages with responsive navigation buttons and predictive back stack handling.

### Changed
- Removed redundant duplicate menus and merged overlapping features into the 4 primary categories.
- Completely removed all fake labels ("LIVE", "REAL-TIME", "LIVE TEST", "REAL-TIME TEST") and simulated touch calibration digitizer scores.
- Preserved all original assets, logos, background videos (`1000487036.mp4`), and visual illustrations.
- Updated automated build pipeline `build_apks.sh` and web distribution portal `server.js` to serve fresh v2.2.0 release artifacts.

---

## [2.0.0] - 2026-09-29

### Added
- Complete rebranding to **DYNIMETIZE ZX**.
- Developer Identity card for **ZUCCHERO XANN** (`ZX-DEV-001`).
- Official Contacts card linking directly to WhatsApp and WhatsApp Channel.
- Official Partners card featuring **LALZ** (`ZX-PARTNER-001`) and **VAXXY** (`ZX-PARTNER-002`) with direct WhatsApp reach-out.
- Support link to Sociabuzz for community backing.
- Shizuku integration banner with dynamic status detection.
- Game Space Library with booster acceleration animations and haptics.
- Plugin Extension management suite with instant driver and network controls.
- Comprehensive hardware telemetry (CPU, GPU, RAM, Battery health & temperature).

### Changed
- Upgraded target SDK to 36 and Android Gradle configurations.
- Modernized vibrator and haptic tactile feedback using `VibratorManager` on Android 12+ (API 31+).
- Refactored ProGuard rules to protect data models and Room entities during release optimization.
- Improved bottom navigation responsiveness and safe edge-to-edge insets.

### Fixed
- Fixed release packaging task configuration when custom signing key is not provided in CI/Dev environments.
- Eliminated deprecated `Context.VIBRATOR_SERVICE` warnings.
- Fixed backstack navigation flow on modal bottom sheets and sub-screens.
