# Linkora — Remaining Work & Feature Implementation Roadmap

> **File:** `REMAINING_WORK.md`  
> **Status:** Active Reference & Handover Document  
> **Last Updated:** October 20, 2026  
> **Target Platform:** Android (Kotlin, Jetpack Compose, 100% Offline P2P Mesh Architecture)  
> **Verification Baseline:** Running Android Emulator (Pixel 10 Pro XL / 1344x2992, 480 DPI, Android API 35/36)

---

## 📊 High-Level Progress Overview

| Metric | Status | Details |
| :--- | :---: | :--- |
| **Total Features in Master List** | **155** | Across 13 architectural categories |
| **Completed & Verified Features** | **63** | Implemented, compiled, verified on live simulator |
| **Remaining Features to Implement** | **92** | Ready for implementation in prioritized batches |
| **Database Schema** | **v5** | Supports messages, media, starred, pinned, reactions, edit, read, call logs, groups, archives |
| **UI/UX Aesthetics** | **Liquid Glass** | iOS-inspired translucent blur, spring physics animations, fluid responsive insets |

---

## 🎯 Completed in Recent Sessions (Verified & Operational)

1. **Feature #12: Message Forwarding** — Modal dialog with quote preview, search filter, and contact/group selection chips.
2. **Feature #13: Message Editing** — In-place text editing with `(edited)` indicator and top `EditingBanner`.
3. **Feature #14: Message Deletion for Everyone** — Liquid glass confirmation dialog with instant DB removal.
4. **Feature #15: Copy Message** — Clipboard copy action with haptic pulse and toast.
5. **Feature #16: Pin Messages in Chat** — Pin action, cyan pin icon on bubble, top banner with animated scroll-to-item.
6. **Feature #17: Starred Messages** — Gold star icon (`#FBBF24`), toggle action in reaction bar, persistent SQLite storage.
7. **Feature #18: Message Reactions** — Floating emoji reaction popup, corner pill badge on bubble, tap to remove, persistent in DB.
8. **Feature #19: Typing Indicators** — Real-time animated 3-dot jumping cyan bubble (`TypingIndicatorBubble`) & header status.
9. **Feature #20: Read Receipts** — `DeliveryState.READ` double cyan ticks; auto-marks conversation messages upon opening.
10. **Feature #26: Call History & Logs** — Dedicated "Calls" bottom navigation tab with incoming/outgoing/missed icons, redial, and clear logs.
11. **Feature #45: App Lock with Biometrics** — Android Keyguard biometric/PIN lock screen with secure settings toggle.
12. **Feature #126: Archive Chats** — Archived filter pill in Chats tab with long-press conversation archive/unarchive.
13. **Audio Engine Restoration** — Restored classic telephone ringtone cadence (`TONE_SUP_RINGTONE`) and vibration for incoming calls.

---

## 🚀 Prioritized Batches for Next Sessions

### 🔹 Batch 3: Enhanced Messaging & Media (Immediate Next Priority)
- [ ] **Feature #9: Emoji Picker & Keyboard Integration** — Full categorised emoji picker sheet inside `ConversationScreen`.
- [ ] **Feature #10: GIF & Sticker Support** — Offline GIF previewer and sticker pack management.
- [ ] **Feature #5: Voice Messages (Enhanced)** — Audio waveform visualizer playback for recorded voice notes.
- [ ] **Feature #8: In-App Document Viewer** — PDF and text file previewer within the chat context without leaving the app.
- [ ] **Feature #11: Threaded Message Replies (UI Polish)** — Visual reply connector lines linking reply quote to the original message.

### 🔹 Batch 4: Calling & Multimedia Enhancements
- [ ] **Feature #22: Peer-to-Peer Video Calling** — Local CameraX video capture frame streaming alongside the existing voice engine.
- [ ] **Feature #25: Picture-in-Picture Calling** — System Android PiP mode when minimizing Linkora during an active call.
- [ ] **Feature #27: In-Call Mute & Speakerphone Polish** — Proximity sensor integration to turn off display when held to ear.
- [ ] **Feature #28: Screen Sharing** — MediaProjection API frame capture streamed over Wi-Fi Direct.
- [ ] **Feature #30: Background Blur for Video** — Real-time segmentation effect on local camera stream.

### 🔹 Batch 5: Privacy, Security & Disappearing Messages
- [ ] **Feature #41: Disappearing Messages / Ephemeral Timers** — Self-destruct countdown timer per message (5m, 1h, 24h, 7d).
- [ ] **Feature #42: Anti-Screenshot Warning / Protection** — `FLAG_SECURE` window toggle and local screenshot detection alert.
- [ ] **Feature #43: End-to-End Key Verification QR Scan Flow** — Comparing 12-digit safety numbers via side-by-side QR scan.
- [ ] **Feature #44: Panic / Duress Mode** — Decoy PIN entering alternative decoy chat database.
- [ ] **Feature #46: Incognito / Stealth App Icon** — Dynamic launcher activity alias switching Linkora icon to Calculator/Notes.

### 🔹 Batch 6: Mesh Routing, Offline Sync & Diagnostics
- [ ] **Feature #71: Mesh Hop Count & Route Path Visualizer** — Interactive node map showing intermediate relays for a delivered message.
- [ ] **Feature #72: Store-and-Forward Relay Diagnostics** — Relay queue inspector displaying packets buffered in flash storage.
- [ ] **Feature #73: Bluetooth LE RSSI Range Meter** — Real-time signal strength meter for nearby nodes.
- [ ] **Feature #74: Wi-Fi Aware Direct Peering** — NAN (Neighbor Awareness Networking) transport for high-bandwidth media sharing.

### 🔹 Batch 7: Customization & Liquid Glass Theming
- [ ] **Feature #81: Custom Chat Wallpapers & Blur Depth** — Gallery picture wallpaper with customizable blur intensity.
- [ ] **Feature #82: Dynamic Material You & Theme Accents** — Selecting custom accent palettes (Electric Cyan, Emerald, Amethyst, Sunset).
- [ ] **Feature #83: Custom Notification Ringtone Picker** — Allowing user to choose between Classic Bell, Marimba, Modern Chime, or Silent.

---

## 🛠️ Instructions for Resuming Next Session

1. **Verify Emulator Status:**
   ```bash
   /Users/upadesh/Library/Android/sdk/platform-tools/adb devices
   ```
   Ensure `emulator-5554` (`Pixel_10_Pro_XL`) is connected.

2. **Check Current Record Files:**
   - [`FEATURES_IMPLEMENTATION_RECORD.md`](file:///Users/upadesh/AndroidStudioProjects/Linkora/FEATURES_IMPLEMENTATION_RECORD.md) — Live audit log of completed features.
   - [`FEATURES_AVAILABILITY_ANALYSIS.md`](file:///Users/upadesh/AndroidStudioProjects/Linkora/FEATURES_AVAILABILITY_ANALYSIS.md) — Master 155-feature audit matrix.

3. **Compilation & Deployment Rule:**
   - **Never run** `./gradlew test` or `connectedCheck`.
   - Build and install debug APK directly:
     ```bash
     ./gradlew installDebug
     ```
   - Launch app on simulator:
     ```bash
     /Users/upadesh/Library/Android/sdk/platform-tools/adb shell am start -n com.example.linkora/.ui.MainActivity
     ```

4. **Pick Up from Batch 3:**
   - Start with **Feature #9 (Emoji Picker Sheet)** or **Feature #41 (Disappearing Messages)**.
   - Maintain the record in `FEATURES_IMPLEMENTATION_RECORD.md` as each feature is completed.
