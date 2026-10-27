# Linkora: Comprehensive Feature Availability Audit & Gap Analysis

> **Generated:** October 20, 2026  
> **Application:** Linkora — Decentralized Offline Mesh Messenger & Caller  
> **Platform:** Android (Jetpack Compose, Kotlin, SQLite, Android Keystore, Nearby Connections, NFC HCE)

---

## 📊 Executive Summary

| Category | Total Features | Available (✅) | Partially Available (⚠️) | Not Available (❌) |
| :--- | :---: | :---: | :---: | :---: |
| **1. Basic Messaging Features** | 20 | 11 | 3 | 6 |
| **2. Voice and Video Calling** | 10 | 2 | 0 | 8 |
| **3. NFC and Device Connectivity** | 10 | 7 | 0 | 3 |
| **4. Privacy and Security** | 14 | 2 | 2 | 10 |
| **5. iOS-Style UI and Animations** | 16 | 11 | 3 | 2 |
| **6. Chat Customization** | 10 | 0 | 0 | 10 |
| **7. Group Chat Features** | 10 | 1 | 0 | 9 |
| **8. Status and Social Features** | 10 | 1 | 4 | 5 |
| **9. AI-Powered Features** | 10 | 0 | 0 | 10 |
| **10. File Sharing and Productivity** | 10 | 3 | 1 | 6 |
| **11. Search and Organization** | 10 | 3 | 2 | 5 |
| **12. Advanced Features** | 10 | 3 | 0 | 7 |
| **13. Unique Linkora Features** | 15 | 7 | 3 | 5 |
| **TOTALS** | **155** | **51 (32.9%)** | **18 (11.6%)** | **86 (55.5%)** |

---

## 1. Basic Messaging Features

| # | Feature | Status | Current Codebase State & Evidence | What is Missing / Needed |
| :--- | :--- | :---: | :--- | :--- |
| **1** | **One-to-One Chat** | ✅ Available | `ConversationScreen.kt`, `MeshDatabaseHelper.kt`, pairwise AES-256-GCM routing in `MeshRouter.kt`. | Fully functional. |
| **2** | **Group Chat** | ✅ Available | `GroupChat` model, `groups` SQLite table, `GROUP_TEXT` & `GROUP_INVITE` in `MeshEnvelope.kt`. | Fully functional offline group messaging. |
| **3** | **Real-Time Messaging** | ✅ Available | Peer-to-peer Wi-Fi Direct and BLE mesh sockets in `NearbyMeshTransport.kt`. | Instant delivery when peers are in wireless range. |
| **4** | **Text Messages** | ✅ Available | Full UTF-8 text transmission with encryption and bubble rendering. | Fully functional. |
| **5** | **Voice Messages** | ⚠️ Partial | Live recording timer, audio waveforms, `MessageType.AUDIO` file chunking, and intent playback exist. | Inline waveform audio scrubber/player inside message bubble. |
| **6** | **Image Sharing** | ✅ Available | Image picker, chunked transfer (`FileChunkPayload`), thumbnail preview in `ConversationScreen.kt`. | Fully functional. |
| **7** | **Video Sharing** | ✅ Available | Video picker, chunking, video play badge, and intent playback launch. | Inline video player with controls. |
| **8** | **Document and File Sharing** | ✅ Available | Document picker, file metadata, chunking, opening via `FileProvider`. | Fully functional. |
| **9** | **Emoji Support** | ✅ Available | Full UTF-8 emoji support in Compose `BasicTextField` and bubble rendering. | Fully functional. |
| **10** | **GIF and Sticker Support** | ❌ Not Available | No sticker pack picker, animated WebP/Lottie renderer, or Giphy/Tenor SDK. | Sticker database, animated sticker renderer, and picker drawer. |
| **11** | **Message Reply** | ✅ Available | Swipe-to-reply gesture with spring bounce, reply banner, and quoted preview bubble. | Fully functional. |
| **12** | **Message Forwarding** | ❌ Not Available | Context menu has Copy and Delete, but no Forward option. | Forward dialog with contact/group picker and envelope re-encryption. |
| **13** | **Message Editing** | ❌ Not Available | Database messages are immutable; no `is_edited` field or `MESSAGE_EDIT` wire envelope. | Protocol envelope type, database migration for edit history, and "(edited)" label. |
| **14** | **Message Deletion** | ✅ Available | Local deletion supported via `deleteMessage()` and `clearChat()`. | Note: Remote "Delete for Everyone" mesh broadcast packet is not implemented. |
| **15** | **Copy Message** | ✅ Available | Long-press context menu copies text to system `ClipboardManager`. | Fully functional. |
| **16** | **Pin Messages** | ❌ Not Available | Chat conversations can be pinned in list, but pinning a message to the chat header is missing. | `is_pinned` column in `messages` table and pinned message header bar in `ConversationScreen`. |
| **17** | **Starred Messages** | ❌ Not Available | No `is_starred` column in `messages` table. | Star action in context menu, database column, and "Starred Messages" view. |
| **18** | **Message Reactions** | ✅ Available | Long-press floating glass reaction bar (❤️, 👍, 😂, 😮, 😢, 🙏) with spring scale animation. | Fully functional locally (remote reaction packet broadcast can be added). |
| **19** | **Typing Indicators** | ⚠️ Partial | UI has animated 3-dot typing indicator component, but no wire broadcast packet. | Mesh envelope `TYPING_STATUS` with 3-second debounce broadcast. |
| **20** | **Read Receipts** | ⚠️ Partial | Delivery status supported (Queued, Relayed = 1 check, Delivered = 2 checks). | Explicit "Read/Seen" blue checkmark wire acknowledgment packet. |

---

## 2. Voice and Video Calling

| # | Feature | Status | Current Codebase State & Evidence | What is Missing / Needed |
| :--- | :--- | :---: | :--- | :--- |
| **21** | **One-to-One Voice Calls** | ✅ Available | `MeshCallManager.kt`, `CallSoundEngine.kt`, `CallScreen.kt` with telco tones and live audio frames. | Fully functional 1-to-1 offline voice calling. |
| **22** | **Video Calls** | ❌ Not Available | Audio-only in `CallScreen.kt`. No camera capture or video stream encoding. | CameraX / WebRTC video capture, H.264 video frame chunking, and dual video viewfinders. |
| **23** | **Group Voice Calls** | ❌ Not Available | `MeshCallManager` handles only pairwise calls between 2 peers. | Multi-peer signaling mesh, audio mixer, and multi-user grid UI. |
| **24** | **Group Video Calls** | ❌ Not Available | No group calling or video calling architecture. | Multi-party video grid, mesh bandwidth adaptation, and video stream mixing. |
| **25** | **Picture-in-Picture Calling** | ❌ Not Available | No `enterPictureInPictureMode()` or floating call overlay window. | Android PiP lifecycle integration and mini call overlay layout. |
| **26** | **Call History** | ❌ Not Available | Calls are ephemeral in memory; no `call_logs` table in SQLite. | Database table for call logs (incoming/outgoing/missed/duration) and Calls tab. |
| **27** | **Mute and Speaker Controls** | ✅ Available | Interactive mute mic and toggle speakerphone buttons in `CallScreen.kt`. | Fully functional. |
| **28** | **Screen Sharing** | ❌ Not Available | No `MediaProjectionManager` capture service. | Android Screen Capture API, VP8/H.264 encoder, and peer display surface. |
| **29** | **Call Recording (with consent)** | ❌ Not Available | No audio recording pipeline for active call streams. | Local PCM/AAC audio file recorder with consent tone notification. |
| **30** | **Background Blur During Video Calls**| ❌ Not Available | Video calls do not exist; no ML segmentation. | ML Kit Selfie Segmentation and OpenGL shader background blur. |

---

## 3. NFC and Device Connectivity

| # | Feature | Status | Current Codebase State & Evidence | What is Missing / Needed |
| :--- | :--- | :---: | :--- | :--- |
| **31** | **NFC Tap-to-Connect** | ✅ Available | `LinkoraHceService.kt`, AID `F0010203040506`, mutual APDU exchange on tap. | Fully functional mutual 2-way handshake. |
| **32** | **Nearby Device Discovery** | ✅ Available | `NearbyMeshTransport.kt` with BLE + Wi-Fi Direct advertising and discovery. | Fully functional. |
| **33** | **Animated Device Connection** | ✅ Available | `NfcContactShareDialog.kt` AirDrop / Liquid Glass animation with two floating phones and spring physics. | Fully functional. |
| **34** | **QR Code Device Pairing** | ✅ Available | `QrCodeUtil.kt` (ZXing QR generation) and `ContactVerificationDialog.kt` (camera scan). | Fully functional. |
| **35** | **Quick Contact Sharing** | ✅ Available | Instant cryptographic contact card exchange via NFC APDU and QR. | Fully functional. |
| **36** | **Nearby File Sharing** | ✅ Available | `FileChunkPayload` streaming over direct Wi-Fi Direct and multi-hop mesh. | Fully functional. |
| **37** | **Device-to-Device Handoff** | ❌ Not Available | No session handoff between devices. | Local network handoff protocol to transfer active chat/call to a second device. |
| **38** | **Multi-Device Login** | ❌ Not Available | Serverless, single-device architecture; private keys are tied to device hardware Keystore. | Secondary device key pairing protocol (Signal-style multi-device sync). |
| **39** | **Linked Devices Management** | ❌ Not Available | No linked devices screen or companion management. | Paired devices list with revoke permissions action. |
| **40** | **Connection Status Indicator** | ✅ Available | Dynamic status pill showing connected mesh peers count, Wi-Fi Direct state, and offline status. | Fully functional. |

---

## 4. Privacy and Security

| # | Feature | Status | Current Codebase State & Evidence | What is Missing / Needed |
| :--- | :--- | :---: | :--- | :--- |
| **41** | **End-to-End Encryption** | ✅ Available | `CryptoManager.kt` using X25519 ECDH + AES-256-GCM + Ed25519 signatures. | Fully functional military-grade cryptography. |
| **42** | **Disappearing Messages** | ⚠️ Partial | Mesh envelopes have TTL (`expiresAt`) and relay queue auto-purge, but no in-chat visual timer. | Per-chat disappearing timer (e.g. 24h, 7d) with automatic SQLite record deletion. |
| **43** | **View-Once Media** | ❌ Not Available | All received media files remain in app storage until explicitly deleted. | `is_view_once` flag, single-tap viewer, and immediate file shredding on close. |
| **44** | **Chat Lock** | ❌ Not Available | All chats are accessible once inside the app. | Per-conversation biometric or PIN lock screen. |
| **45** | **App Lock with Biometrics** | ❌ Not Available | App opens directly without authentication. | `androidx.biometric.BiometricPrompt` on cold start and background resume. |
| **46** | **Screenshot Notifications** | ❌ Not Available | No screenshot detection or `FLAG_SECURE` protection. | Android 14 `ScreenCaptureCallback` or `WindowManager.LayoutParams.FLAG_SECURE`. |
| **47** | **Two-Factor Authentication** | ❌ Not Available | Cloud 2FA is inapplicable (serverless), and no local master PIN exists. | Local 6-digit master PIN challenge protecting private key exports. |
| **48** | **Block and Report Users** | ❌ Not Available | Incoming packets from any peer are processed if verified. | `blocked_users` SQLite table, packet filter in `MeshRouter`, and "Block" button. |
| **49** | **Privacy Controls** | ⚠️ Partial | Cryptographic keys can be verified, but no granular profile visibility toggles. | Privacy settings page (hide avatar, hide last seen, read receipts toggle). |
| **50** | **Encrypted Backups** | ❌ Not Available | No backup/export feature for the database. | AES-GCM encrypted database export file with password derivation (PBKDF2/Argon2). |
| **51** | **Message Forwarding Restrictions** | ❌ Not Available | No forwarding restrictions. | "Restrict Forwarding" flag preventing message re-sharing or copying. |
| **52** | **Hidden Chats** | ❌ Not Available | All conversations appear in the main chat list. | Secret gesture/passcode to reveal a hidden chats folder. |
| **53** | **Incognito Chat Mode** | ❌ Not Available | All messages are stored in SQLite. | Ephemeral RAM-only session mode where messages are never written to disk. |
| **54** | **Safety Number or Key Verification** | ✅ Available | 60-digit hexadecimal fingerprint comparison and QR code cryptographic verification. | Fully functional. |

---

## 5. iOS-Style UI and Animations

| # | Feature | Status | Current Codebase State & Evidence | What is Missing / Needed |
| :--- | :--- | :---: | :--- | :--- |
| **55** | **Liquid Glass Interface** | ✅ Available | `LocalGlassColors`, frosted acrylic translucent panels, specular highlight borders. | Fully functional. |
| **56** | **Animated Glass Navigation Bar** | ✅ Available | `GlassBottomBar.kt` with floating glass pill, bouncing icons, and glowing indicator. | Fully functional. |
| **57** | **Smooth Screen Transitions** | ✅ Available | `AnimatedVisibility`, slide/fade transitions, and spring physics. | Fully functional. |
| **58** | **Interactive Swipe-to-Reply** | ✅ Available | Horizontal drag with spring resistance, haptic feedback trigger, and reply preview. | Fully functional. |
| **59** | **Spring-Based Button Animations** | ✅ Available | Interactive scale press bounce using `Animatable` and `Spring.DampingRatioMediumBouncy`. | Fully functional. |
| **60** | **Animated Message Bubbles** | ✅ Available | Spring appearance, glass refraction borders, sender-specific tint. | Fully functional. |
| **61** | **Dynamic Island-Style Activity Indicator** | ✅ Available | `GlassNotificationBanner.kt` and floating active call status pill. | Fully functional. |
| **62** | **Haptic Feedback** | ✅ Available | `LocalHapticFeedback` on taps, reactions, swipe-to-reply, and NFC touch. | Fully functional. |
| **63** | **Animated Emoji Reactions** | ✅ Available | Spring scale pop-in for reaction bar and flying reaction badge on bubble. | Fully functional. |
| **64** | **Floating Glass Action Buttons** | ✅ Available | Glass FABs with blur, subtle gradients, and glowing borders. | Fully functional. |
| **65** | **Interactive NFC Connection Animation** | ✅ Available | Apple AirDrop / iOS Liquid Glass animation with two interactive smartphones and concentric waves. | Fully functional. |
| **66** | **Animated Chat Backgrounds** | ⚠️ Partial | Ambient blurred background image with glass overlay. | Live OpenGL or Canvas procedural particle/gradient shader background. |
| **67** | **Custom App Themes** | ⚠️ Partial | Light, Dark, System, and Liquid Glass presets exist in `ThemeManager`. | In-app theme builder to customize accents, blur strength, and glass tint. |
| **68** | **Dark and Light Modes** | ✅ Available | High-contrast Dark and Light glass palettes in `ThemeManager.kt`. | Fully functional. |
| **69** | **Dynamic Wallpaper Effects** | ⚠️ Partial | Wallpaper with blur render effects. | Parallax gyroscope motion effect on wallpaper tilt. |
| **70** | **Smooth Skeleton Loading** | ❌ Not Available | Standard progress bars or instant loads; no shimmer skeleton layout. | Shimmer effect composable modifier for chat list and profile placeholders. |

---

## 6. Chat Customization

| # | Feature | Status | Current Codebase State & Evidence | What is Missing / Needed |
| :--- | :--- | :---: | :--- | :--- |
| **71** | **Custom Chat Wallpapers** | ❌ Not Available | Wallpaper is currently global across the app. | Per-conversation wallpaper picker and database storage. |
| **72** | **Per-Chat Themes** | ❌ Not Available | Theme applies globally to the entire application. | Theme selector inside Conversation details. |
| **73** | **Custom Message Colors** | ❌ Not Available | Message bubble colors are locked to global theme tokens. | Bubble color picker (emerald, purple, ocean, sunset, titanium). |
| **74** | **Custom App Icons** | ❌ Not Available | Single default app icon in `AndroidManifest.xml`. | Android `<activity-alias>` tags for alternate launcher icons. |
| **75** | **Font Size Customization** | ❌ Not Available | Typography uses fixed `sp` values in `LinkoraTypography.kt`. | Font scale slider in Settings (Small, Normal, Large, Extra Large). |
| **76** | **Chat Bubble Styles** | ❌ Not Available | Single signature Liquid Glass curved bubble. | Bubble style presets (Liquid Glass, Minimal, Rounded Pill, Sharp Edge). |
| **77** | **Animated Wallpapers** | ❌ Not Available | Static backgrounds only. | Animated looping MP4 or Canvas shader chat background. |
| **78** | **Personalized Notification Sounds**| ❌ Not Available | Default system notification tone. | Sound picker allowing selection of custom notification audio files. |
| **79** | **Custom Ringtones** | ❌ Not Available | System default ringtone via `RingtoneManager`. | Custom incoming call ringtone picker per contact. |
| **80** | **Contact-Specific Themes** | ❌ Not Available | No per-contact visual customization. | Contact theme preference saved in `contacts` table. |

---

## 7. Group Chat Features

| # | Feature | Status | Current Codebase State & Evidence | What is Missing / Needed |
| :--- | :--- | :---: | :--- | :--- |
| **81** | **Group Creation** | ✅ Available | `CreateGroupDialog.kt`, group key generation, and member invite dispatch. | Fully functional. |
| **82** | **Group Admin Controls** | ❌ Not Available | `creatorId` is saved, but no admin privileges (kick, promote, mute members). | Admin role check, kick member packet, and admin badge UI. |
| **83** | **Group Roles and Permissions** | ❌ Not Available | All members currently have identical permissions. | Role schema (Admin, Moderator, Member) and permission flags. |
| **84** | **Group Invite Links** | ❌ Not Available | Invites are sent strictly through pairwise encrypted envelopes to known contacts. | Shareable QR code or deep link (`linkora://join-group?...`). |
| **85** | **Polls and Voting** | ❌ Not Available | No poll message type or vote tallying. | `MessageType.POLL`, voting options schema, and live voting bar UI. |
| **86** | **Group Announcements** | ❌ Not Available | No announcement channel mode. | "Only Admins Can Send" mode for broadcast announcements. |
| **87** | **Group Description** | ❌ Not Available | Groups table only stores `group_id`, `name`, `creator_id`, `group_key_base64`, `member_ids`. | Add `description` column to `groups` table and group info editor. |
| **88** | **Member Mentions** | ❌ Not Available | No `@username` mention detection or popup. | Text watcher for `@`, member autocomplete popup, and mention highlight. |
| **89** | **Group Events and Scheduling** | ❌ Not Available | No group calendar or events feature. | Event creation dialog, event message card, and reminder notification. |
| **90** | **Shared Group Media Gallery** | ❌ Not Available | Media is mixed into chat stream only. | Group Media Gallery screen with tabs for Photos, Videos, Audio, and Files. |

---

## 8. Status and Social Features

| # | Feature | Status | Current Codebase State & Evidence | What is Missing / Needed |
| :--- | :--- | :---: | :--- | :--- |
| **91** | **Stories or Status Updates** | ⚠️ Partial | "Add Story" and "Your Story" circle avatars exist on `ChatsScreen.kt`, but do not open a story viewer. | 24-hour ephemeral story viewer screen with progress bars and touch navigation. |
| **92** | **Text Status** | ❌ Not Available | No text status composer. | Text story composer with custom gradients and fonts. |
| **93** | **Photo and Video Status** | ❌ Not Available | No photo/video story publisher. | Media picker/camera capture with 24-hour broadcast over mesh. |
| **94** | **Status Reactions** | ❌ Not Available | No story reaction mechanism. | Quick emoji reaction and text reply sent to status author's chat. |
| **95** | **Status Privacy Controls** | ❌ Not Available | No status privacy settings. | "All Contacts", "Only Nearby Peers", or custom whitelist. |
| **96** | **Online Status** | ⚠️ Partial | Shows active Wi-Fi Direct peer connection, but no decentralized online presence protocol. | Peer presence heartbeat broadcast with "Online" / "Recently Active" badge. |
| **97** | **Last Seen** | ⚠️ Partial | `last_seen` timestamp tracked in `contacts` table, but no privacy setting to hide it. | Toggle in Settings: "Show My Last Seen" (Everyone / Nobody). |
| **98** | **Custom Profile Pictures** | ⚠️ Partial | Generates colored monogram avatar or uses mock image; no user photo upload. | Profile picture image picker, circular crop tool, and mesh avatar sync. |
| **99** | **User Bios** | ❌ Not Available | Identity only has `displayName` and `identityId`. | Add `bio` field to identity profile and contact cards. |
| **100**| **Contact Discovery** | ✅ Available | Passive wireless peer discovery over BLE/Wi-Fi Direct mesh + NFC tap + QR scan. | Fully functional. |

---

## 9. AI-Powered Features

| # | Feature | Status | Current Codebase State & Evidence | What is Missing / Needed |
| :--- | :--- | :---: | :--- | :--- |
| **101**| **AI Chat Assistant** | ❌ Not Available | No AI chatbot or LLM integration. | Local on-device Gemini Nano / LiteRT (TensorFlow Lite) offline chat assistant. |
| **102**| **Smart Reply Suggestions** | ❌ Not Available | No contextual quick replies. | Google ML Kit Smart Reply SDK (runs 100% on-device offline). |
| **103**| **Message Summarization** | ❌ Not Available | No thread summarization. | Local on-device text summarization model for long group chats. |
| **104**| **Voice Message Transcription** | ❌ Not Available | Voice memos play as audio; no speech-to-text. | Android `SpeechRecognizer` offline language pack or Whisper.tflite transcription. |
| **105**| **Real-Time Message Translation**| ❌ Not Available | Messages remain in original language. | Google ML Kit On-Device Translation (downloads offline language packs). |
| **106**| **AI Writing Assistance** | ❌ Not Available | Standard text entry. | Local AI text rewriting helper (Casual, Formal, Concise). |
| **107**| **Grammar Correction** | ❌ Not Available | Standard Android spellcheck only. | On-device grammar and punctuation corrector chip in composer. |
| **108**| **AI Sticker Generation** | ❌ Not Available | No sticker generator. | On-device diffusion or procedural emoji mashup generator. |
| **109**| **Chat Search Assistant** | ❌ Not Available | Keyword search only. | Semantic vector embeddings for natural language chat search. |
| **110**| **Spam and Scam Detection** | ❌ Not Available | No phishing/spam detection. | On-device heuristic/NLP scam detector flagging suspicious links. |

---

## 10. File Sharing and Productivity

| # | Feature | Status | Current Codebase State & Evidence | What is Missing / Needed |
| :--- | :--- | :---: | :--- | :--- |
| **111**| **High-Quality Media Sharing** | ✅ Available | Transfers original uncompressed media in raw chunks up to 64KB per envelope. | Fully functional. |
| **112**| **Large File Transfers** | ✅ Available | `FileChunkPayload` reconstructs chunked files with sequential index and reassembly. | Fully functional. |
| **113**| **File Transfer Progress** | ⚠️ Partial | Relay queue tracks chunk counts, but real-time percentage progress bar is basic. | Visual circular/linear progress bar showing X MB / Y MB on the message bubble. |
| **114**| **Cloud File Storage** | ❌ Not Available | *Architectural Decision:* Linkora is 100% serverless, offline, decentralized mesh. | Cloud storage is intentionally omitted to maintain zero-cloud privacy. |
| **115**| **Shared Notes** | ❌ Not Available | No collaborative document editor. | Encrypted peer-to-peer shared notepad synchronized over mesh. |
| **116**| **Message Reminders** | ❌ Not Available | No message reminder mechanism. | Long-press action: "Remind me in 1 hour" with `AlarmManager` notification. |
| **117**| **Scheduled Messages** | ❌ Not Available | Messages are sent immediately to queue. | Send delay scheduler with scheduled delivery time in `relay_queue`. |
| **118**| **Calendar Integration** | ❌ Not Available | No calendar integration. | Event detection and `CalendarContract` integration. |
| **119**| **Location Sharing** | ❌ Not Available | No GPS location sharing. | Google Play Services / FusedLocationProvider static map thumbnail card. |
| **120**| **Live Location Sharing** | ❌ Not Available | No live coordinate streaming. | Ephemeral mesh packet broadcasting live GPS coordinates for 15m/1h/8h. |

---

## 11. Search and Organization

| # | Feature | Status | Current Codebase State & Evidence | What is Missing / Needed |
| :--- | :--- | :---: | :--- | :--- |
| **121**| **Global Message Search** | ⚠️ Partial | Search bar exists on `ChatsScreen`, but filters conversation titles, not message body. | SQLite FTS4/FTS5 full-text search across all messages in `messages` table. |
| **122**| **Search by Contact** | ✅ Available | Search bar on `ContactsScreen` filters contacts by name and identity ID. | Fully functional. |
| **123**| **Search by Date** | ❌ Not Available | No calendar date picker to jump to conversation messages on a specific date. | Date picker dialog in chat header to jump directly to message timestamps. |
| **124**| **Search by Media Type** | ❌ Not Available | No media filters (Photos, Videos, Audio, Docs). | Filter pills in search bar to isolate messages by `message_type`. |
| **125**| **Chat Folders** | ❌ Not Available | Fixed tabs only (All, Unread, Groups, Favorites). | User-created custom folders (Work, Family, Friends) with drag-and-drop. |
| **126**| **Archive Chats** | ❌ Not Available | No `is_archived` database flag or Archived Chats vault. | `is_archived` column in SQLite, swipe-to-archive gesture, and Archived Chats view. |
| **127**| **Mute Conversations** | ⚠️ Partial | UI model has `isMuted` and renders speaker-off icon, but notification suppression is incomplete. | Mute settings dialog (8 hours, 1 week, Always) suppressing system notifications. |
| **128**| **Unread Message Filters** | ✅ Available | "Unread" filter pill on `ChatsScreen` isolates chats with unread messages. | Fully functional. |
| **129**| **Pinned Conversations** | ✅ Available | "Favorites" filter pill and pin badge on `ChatsScreen` for pinned chats. | Fully functional. |
| **130**| **Automatic Spam Filtering** | ❌ Not Available | No quarantine folder for unknown contacts. | Unknown sender quarantine folder where new peer requests require approval. |

---

## 12. Advanced Features

| # | Feature | Status | Current Codebase State & Evidence | What is Missing / Needed |
| :--- | :--- | :---: | :--- | :--- |
| **131**| **Username-Based Messaging** | ✅ Available | Users have display names and cryptographic identities `usr_...`. | Fully functional. |
| **132**| **Contact Synchronization** | ❌ Not Available | No Android device phonebook/contacts sync. | Contact book scanner matching phonebook names to Linkora mesh contacts. |
| **133**| **Offline Message Queue** | ✅ Available | `relay_queue` table in `MeshDatabaseHelper` with store-and-forward persistence. | Fully functional. |
| **134**| **Automatic Message Retry** | ✅ Available | `MeshRouter` retry daemon checks `next_attempt_at` and retries every 15-30s. | Fully functional. |
| **135**| **Cross-Platform Synchronization** | ❌ Not Available | Pure Android app; no iOS/Desktop/Web client. | WebRTC/mesh client for desktop (Electron/Tauri) or iOS port. |
| **136**| **Web and Desktop Applications** | ❌ Not Available | No companion desktop app. | Companion desktop app with local Wi-Fi pairing. |
| **137**| **Message Backup and Restore** | ❌ Not Available | No export/import of SQLite database or encrypted key bundle. | Encrypted file export/import with key recovery phrase. |
| **138**| **Multiple Accounts** | ❌ Not Available | Single identity profile per device. | Account switcher allowing multiple cryptographic identities on one device. |
| **139**| **Custom Notification Rules** | ❌ Not Available | Single global notification channel. | Per-contact notification channels and sound overrides. |
| **140**| **Data Usage Controls** | ❌ Not Available | No cellular/Wi-Fi mesh data consumption meter. | Data usage stats screen with auto-download media toggles. |

---

## 13. Unique Linkora Features

| # | Feature | Status | Current Codebase State & Evidence | What is Missing / Needed |
| :--- | :--- | :---: | :--- | :--- |
| **141**| **NFC Tap-to-Connect** | ✅ Available | Bidirectional 2-way HCE handshake for instant mutual discovery on both devices. | Fully functional. |
| **142**| **Liquid Connect** | ✅ Available | Apple AirDrop / iOS Liquid Glass 60FPS spring animation with two floating phones and ripple. | Fully functional. |
| **143**| **LinkDrop** | ✅ Available | Nearby offline peer-to-peer file, photo, and vCard sharing over Wi-Fi Direct and BLE. | Fully functional. |
| **144**| **Glass Chat** | ✅ Available | Translucent frosted acrylic chat bubbles, customizable themes, blur effects. | Fully functional. |
| **145**| **QuickPair** | ✅ Available | Instant pairing using either NFC tap or cryptographic QR code scanning. | Fully functional. |
| **146**| **Live Presence** | ⚠️ Partial | Detects nearby mesh peers and active connection state, but no rich activity presence. | Activity statuses ("In a Call", "Do Not Disturb", "On Mesh"). |
| **147**| **Smart Handoff** | ❌ Not Available | Single device operation. | Seamless voice call or conversation handoff between linked devices. |
| **148**| **AI Chat Companion** | ❌ Not Available | No on-device AI companion. | Built-in offline AI companion bot for local assistance. |
| **149**| **Private Space** | ❌ Not Available | No biometric-protected hidden vault. | Biometric-locked hidden folder for confidential conversations. |
| **150**| **Theme Studio** | ⚠️ Partial | Theme switching (Light, Dark, System, Liquid Glass) exists, but no granular editor. | Custom theme editor with color picker, glass opacity, and blur radius sliders. |
| **151**| **Universal Search** | ❌ Not Available | No unified search index across messages, files, media, and contacts. | Universal search screen querying contacts, message text, and media index. |
| **152**| **Instant Share** | ✅ Available | Direct zero-hop sharing to immediately adjacent Wi-Fi Direct peers. | Fully functional. |
| **153**| **Conversation Timeline** | ❌ Not Available | No timeline/calendar view of shared links, files, and milestones. | Chronological timeline view of all shared media, documents, and starred moments. |
| **154**| **Smart Notifications** | ⚠️ Partial | `GlassNotificationBanner` in-app HUD with interactive reply/dismiss exists. | Priority classification routing VIP contacts to priority alerts. |
| **155**| **Connection Radar** | ✅ Available | `MeshRadarView.kt` with concentric animated sonar rings, rotating radar sweep, and peer blips. | Fully functional. |

---

## 🎯 Complete Master List of Features NOT Available in Linkora (86 Features)

For quick development planning, here is the complete index of features that are **not currently available**:

### Basic Messaging & Media
- [ ] **#10** GIF and Sticker Support
- [ ] **#12** Message Forwarding
- [ ] **#13** Message Editing
- [ ] **#16** Pin Messages (within chat)
- [ ] **#17** Starred Messages

### Voice, Video & Media Calling
- [ ] **#22** Video Calls
- [ ] **#23** Group Voice Calls
- [ ] **#24** Group Video Calls
- [ ] **#25** Picture-in-Picture Calling
- [ ] **#26** Call History
- [ ] **#28** Screen Sharing
- [ ] **#29** Call Recording (with consent)
- [ ] **#30** Background Blur During Video Calls

### Device Connectivity & Multi-Device
- [ ] **#37** Device-to-Device Handoff
- [ ] **#38** Multi-Device Login
- [ ] **#39** Linked Devices Management

### Privacy & App Security
- [ ] **#43** View-Once Media
- [ ] **#44** Chat Lock
- [ ] **#45** App Lock with Biometrics
- [ ] **#46** Screenshot Notifications
- [ ] **#47** Two-Factor Authentication
- [ ] **#48** Block and Report Users
- [ ] **#50** Encrypted Backups
- [ ] **#51** Message Forwarding Restrictions
- [ ] **#52** Hidden Chats
- [ ] **#53** Incognito Chat Mode

### UI & Animations
- [ ] **#70** Smooth Skeleton Loading

### Chat Customization & Themes
- [ ] **#71** Custom Chat Wallpapers
- [ ] **#72** Per-Chat Themes
- [ ] **#73** Custom Message Colors
- [ ] **#74** Custom App Icons
- [ ] **#75** Font Size Customization
- [ ] **#76** Chat Bubble Styles
- [ ] **#77** Animated Wallpapers
- [ ] **#78** Personalized Notification Sounds
- [ ] **#79** Custom Ringtones
- [ ] **#80** Contact-Specific Themes

### Group Chat Features
- [ ] **#82** Group Admin Controls
- [ ] **#83** Group Roles and Permissions
- [ ] **#84** Group Invite Links
- [ ] **#85** Polls and Voting
- [ ] **#86** Group Announcements
- [ ] **#87** Group Description
- [ ] **#88** Member Mentions
- [ ] **#89** Group Events and Scheduling
- [ ] **#90** Shared Group Media Gallery

### Status & Social
- [ ] **#92** Text Status
- [ ] **#93** Photo and Video Status
- [ ] **#94** Status Reactions
- [ ] **#95** Status Privacy Controls
- [ ] **#99** User Bios

### AI-Powered Features (All 10 Missing)
- [ ] **#101** AI Chat Assistant
- [ ] **#102** Smart Reply Suggestions
- [ ] **#103** Message Summarization
- [ ] **#104** Voice Message Transcription
- [ ] **#105** Real-Time Message Translation
- [ ] **#106** AI Writing Assistance
- [ ] **#107** Grammar Correction
- [ ] **#108** AI Sticker Generation
- [ ] **#109** Chat Search Assistant
- [ ] **#110** Spam and Scam Detection

### File Sharing & Productivity
- [ ] **#114** Cloud File Storage *(Intentionally omitted due to zero-cloud architecture)*
- [ ] **#115** Shared Notes
- [ ] **#116** Message Reminders
- [ ] **#117** Scheduled Messages
- [ ] **#118** Calendar Integration
- [ ] **#119** Location Sharing
- [ ] **#120** Live Location Sharing

### Search & Organization
- [ ] **#123** Search by Date
- [ ] **#124** Search by Media Type
- [ ] **#125** Chat Folders
- [ ] **#126** Archive Chats
- [ ] **#130** Automatic Spam Filtering

### Advanced Infrastructure
- [ ] **#132** Contact Synchronization
- [ ] **#135** Cross-Platform Synchronization
- [ ] **#136** Web and Desktop Applications
- [ ] **#137** Message Backup and Restore
- [ ] **#138** Multiple Accounts
- [ ] **#139** Custom Notification Rules
- [ ] **#140** Data Usage Controls

### Unique Linkora Features
- [ ] **#147** Smart Handoff
- [ ] **#148** AI Chat Companion
- [ ] **#149** Private Space
- [ ] **#151** Universal Search
- [ ] **#153** Conversation Timeline

---

## 🚀 Recommended Implementation Roadmap

To systematically elevate Linkora from its strong offline mesh foundation to feature parity with modern flagship apps, here is the recommended phased implementation plan:

### Phase 1: High-Impact Core Messaging Polish (Fast Wins)
1. **Message Forwarding (#12)** & **Starred Messages (#17)**: Add SQLite columns and context menu actions.
2. **Global Full-Text Search (#121)** & **Search by Media Type (#124)**: Upgrade SQLite with FTS5 table index.
3. **Archive Chats (#126)**: Add `is_archived` column and swipe-to-archive gesture in `ChatsScreen`.
4. **App Lock with Biometrics (#45)**: Add `BiometricPrompt` on app launch.

### Phase 2: Media, Status & Audio Enhancements
1. **Call History (#26)**: Log incoming/outgoing/missed calls in a new `call_logs` table.
2. **24-Hour Ephemeral Stories (#91, #92, #93)**: Connect the existing Story avatars on `ChatsScreen` to a full-screen story viewer and status composer.
3. **In-Bubble Audio Player (#5)**: Add interactive waveform seeking and playback controls.

### Phase 3: On-Device AI Features (100% Offline via ML Kit)
1. **Smart Reply Suggestions (#102)**: Integrate Google ML Kit Smart Reply SDK (no internet required).
2. **Real-Time Translation (#105)**: Integrate Google ML Kit On-Device Translation.
3. **Voice Message Transcription (#104)**: Integrate on-device speech-to-text.

### Phase 4: Advanced Security & Privacy
1. **Private Space / Chat Lock (#44, #49)**: Biometric gate for specific sensitive conversations.
2. **Encrypted Backups (#50, #137)**: Encrypted JSON/SQLite export file protected with a 24-word BIP39 seed phrase.
3. **Block Contacts (#48)**: Local blacklist table dropping packets before processing.
