# Changelog

All notable changes to the **DYNIMETIZE ZX** project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

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
