# DYNIMETIZE ZX - In-Game Floating HUD Enhancement & Release APK Plan

A comprehensive engineering plan to fix bugs, optimize the In-Game Floating HUD overlay and quick action tools, compile verified production release APKs, and update project documentation.

## User Review & Critical Decisions

> [!IMPORTANT]
> The following user preferences were confirmed during clarification and govern this implementation:

- **Primary Focus**: Fix bugs, build verified release APK, and update documentation.
- **Priority Feature Area**: In-game Floating HUD overlay and quick action tools.
- **Build Target**: Compile fresh, verified Release APK (`releases/DYNIMETIZE_ZX-v2.2.0-release.apk` and `releases/app-release.apk`) with signature verification and size validation.
- **Overlay Enhancements**:
  - Integrate instant Crosshair toggle alongside the existing 8 quick actions in the HUD.
  - Implement bidirectional slider synchronization for AudioManager volume and initial system screen brightness detection.
  - Fix boundary clamping in `OverlayService` using screen metrics to prevent the floating trigger bubble from sliding off-screen in landscape gaming sessions.
  - Add orientation change listener so the HUD smoothly repositions when switching between portrait and landscape gaming modes.

---

## 1. Overview & Core Concept

**DYNIMETIZE ZX** is a gaming utility and system telemetry dashboard engineered for Android mobile gamers. Its signature feature is the **In-Game Floating HUD Overlay**—a low-overhead, hardware-accelerated overlay running via an Android Foreground Service with `SYSTEM_ALERT_WINDOW`.

This iteration focuses on:
1. **In-Game Floating HUD Hardening & Upgrades**:
   - Enhancing quick action tools (RAM Boost, Extreme Governor, 240Hz Touch, FPS Stabilizer, Torch, Crosshair quick launch, DND, Shizuku).
   - Dynamic bidirectional volume & brightness tracking.
   - Screen-bounded edge-snapping and rotation-safe drag coordinates for landscape games (Free Fire, PUBG, Call of Duty Mobile).
2. **Bug Fixes**:
   - Resolve slider state de-sync between system audio/brightness changes and Compose HUD state.
   - Prevent unbounded overlay drag coordinates causing the trigger bubble to get lost off-screen.
   - Smooth lifecycle cleanup when toggling services or navigating back into the main app.
3. **Build & Release Packaging**:
   - Execute clean Gradle build for `assembleRelease` with Scheme v2 developer signing fallback.
   - Produce and verify `releases/DYNIMETIZE_ZX-v2.2.0-release.apk` and `releases/app-release.apk`.
   - Validate APK checksums, file sizes, and distribution readiness.
4. **Documentation**:
   - Update `README.md` and release notes with updated HUD capabilities, quick action shortcuts, and installation steps.

---

## 2. User Experience & Visual Design

### Key User Flows

1. **Floating Trigger Bubble Interaction**:
   - In collapsed mode, a sleek pill-shaped trigger (`66.dp` x `48.dp`) sits unobtrusively on the edge of the screen displaying real-time FPS and gaming status.
   - Draggable along screen boundaries with smooth drag physics. Tapping triggers a tactile haptic pulse and expands the full gaming cockpit.
2. **Expanded HUD Cockpit**:
   - **Left Wing (Performance Tools)**: RAM Boost (one-tap real JVM memory trim), Extreme Governor toggle, 240Hz Touch response lock, FPS Stabilizer toggle.
   - **Center Cockpit (Telemetry Core)**: Prominent 38sp digital FPS counter (Choreographer sampled), RAM usage bar and detailed breakdown, CPU load percentage, device battery level, thermal temperature (°C), and real-time network ping (ms).
   - **Right Wing (Utility Tools)**: Torch toggle, Crosshair overlay trigger, Game Space quick launcher, DND gaming focus mode, Shizuku status indicator.
   - **Bottom Control Deck**: Direct brightness and volume sliders with live track gradient, Status bar, Minimize button, and Exit HUD button.

### Visual Styling & Theme Tokens

- **Aesthetic Direction**: High-tech Cyberpunk & Luxury Dark Gaming theme with amber/gold accents (`#FFB300`, `#F59E0B`), deep charcoal translucent surfaces (`#F2090B10`), and neon telemetry indicators (Cyan `#67E8F9` for temperature, Emerald `#86EFAC` for battery, Ruby `#EF4444` for alerts).
- **Typography**: Monospace tabular numbers for FPS and telemetry metrics (`FontFamily.Monospace`) to prevent jitter during fast updates; bold sans-serif display typography for feature labels.
- **Feedback & Motion**: Haptic vibration feedback on all tool activations; smooth scale and alpha transitions (`AnimatedVisibility`, `scaleIn`/`fadeOut`) between collapsed trigger bubble and expanded cockpit.

---

## 3. Key Product Decisions & Trade-Offs

- **Crosshair Integration into HUD**:
  - *Decision*: Add a direct Crosshair quick action tile inside the expanded HUD panel that communicates with `CrosshairService`.
  - *Why*: Competitive FPS gamers need to toggle and test crosshairs without closing their games or leaving the overlay.
- **Screen Boundary Clamping via WindowMetrics**:
  - *Decision*: Dynamically fetch current display bounds (`WindowManager.currentWindowMetrics` on API 30+, fallback to `DisplayMetrics`) in `OverlayService` to restrict `triggerX` and `triggerY`.
  - *Why*: In landscape games, dragging near screen edges previously caused bubbles to slide out of view.
- **Bidirectional Volume & Brightness Sync**:
  - *Decision*: Use `LaunchedEffect(metrics.volumePercent)` inside `GamingHudPanel` to keep local slider states synchronized with system volume changes.
  - *Why*: If the player presses physical hardware volume keys during gameplay, the HUD slider updates immediately.
- **Reproducible Release Build Workflow**:
  - *Decision*: Use the automated `build_apks.sh` script invoking `gradle assembleRelease` to populate `releases/` and mirror into download locations.
  - *Why*: Guarantees signed, verified release artifacts ready for immediate download and deployment.

---

## 4. Technical Architecture & Data Strategy

```
┌────────────────────────────────────────────────────────────────────────┐
│                        ANDROID SYSTEM RUNTIME                          │
│  ┌──────────────────────┐  ┌──────────────────┐  ┌──────────────────┐  │
│  │ Choreographer (FPS)  │  │ ActivityManager  │  │ Battery / Audio  │  │
│  └──────────┬───────────┘  └────────┬─────────┘  └────────┬─────────┘  │
└─────────────┼───────────────────────┼─────────────────────┼────────────┘
              ▼                       ▼                     ▼
┌────────────────────────────────────────────────────────────────────────┐
│                          HudTelemetryTracker                           │
│  - Real-time 1Hz metrics sampling without mock fabrication             │
│  - RAM trim via System.gc() & real MemoryInfo delta measurement        │
│  - Network socket ping to 8.8.8.8:53 with timeout protection           │
└─────────────────────────────────┬──────────────────────────────────────┘
                                  ▼
┌────────────────────────────────────────────────────────────────────────┐
│                             OverlayService                             │
│  - SYSTEM_ALERT_WINDOW Manager with TYPE_APPLICATION_OVERLAY           │
│  - Clamped coordinate calculation (Screen Width/Height safe bounds)   │
│  - Dynamic WindowManager layout updates (Collapsed ↔ Expanded)         │
└─────────────────────────────────┬──────────────────────────────────────┘
                                  ▼
┌────────────────────────────────────────────────────────────────────────┐
│                     Jetpack Compose HUD Interface                      │
│  ┌─────────────────────────────┐   ┌────────────────────────────────┐  │
│  │    FloatingTriggerBubble    │   │         GamingHudPanel         │  │
│  │  - Live FPS badge           │   │  - Left Tools (Boost, Gov,...) │  │
│  │  - Edge-clamped dragging    │   │  - Center Telemetry Core       │  │
│  │  - Quick tap to expand      │   │  - Right Tools (Crosshair,...) │  │
│  │                             │   │  - Volume & Brightness Deck    │  │
│  └─────────────────────────────┘   └────────────────────────────────┘  │
└────────────────────────────────────────────────────────────────────────┘
```

### Component & State Mapping

| Component | State / Event | Handler Implementation |
|---|---|---|
| `FloatingTriggerBubble` | Drag gesture `(dx, dy)` | Updates `triggerX`, `triggerY` with screen boundary clamping |
| `FloatingTriggerBubble` | Click | Invokes `toggleExpandState(true)` with full-screen translucent scrim |
| `GamingHudPanel` | `RAM BOOST` click | Calls `HudTelemetryTracker.triggerRamTrim()`, displays toast with freed MB |
| `GamingHudPanel` | `CROSSHAIR` click | Toggles `CrosshairService` active state and updates HUD indicator |
| `GamingHudPanel` | `EXTREME` click | Toggles extreme governor preference with tactile haptic confirmation |
| `GamingHudPanel` | `Volume` slider | Calls `telemetryTracker.setMusicVolume(pct)` and updates system stream |
| `GamingHudPanel` | `Brightness` slider | Updates `WindowManager.LayoutParams.screenBrightness` on the overlay window |
| `OverlayService` | Close HUD | Stops foreground service, removes views from `WindowManager`, resets state |

---

## 5. Execution Steps

1. **Step 1: Overlay & HUD Refinements**:
   - Update `OverlayService.kt` with robust display boundary clamping for portrait & landscape orientations.
   - Add Crosshair toggle handling in `OverlayService.kt` and `GamingHudView.kt`.
   - Update `GamingHudView.kt` with bidirectional volume sync, brightness initial state, and Crosshair quick action tile.
2. **Step 2: Build Verification (`compile_applet`)**:
   - Run compilation to verify all Compose components, imports, and services compile cleanly without errors.
3. **Step 3: Release Packaging & Verification**:
   - Run `gradle assembleRelease` (via `build_apks.sh` or direct Gradle task) to generate `releases/DYNIMETIZE_ZX-v2.2.0-release.apk` and `releases/app-release.apk`.
   - Validate file size and SHA-256 integrity.
4. **Step 4: Documentation**:
   - Update `README.md` with the enhanced HUD features, quick action tools, and verified release APK status.
