# Linkora: Media & File Sharing (Images, PDFs, Audio) Architecture & Roadmap

This document outlines how to expand Linkora from sending text-only messages to supporting **Images**, **PDF documents**, **Voice notes / Audio**, and other attachments over an offline peer-to-peer mesh network.

---

## 1. Overview & Core Challenges in Offline Mesh

Linkora is designed to work **100% offline** without central servers or internet access using **Google Nearby Connections (Wi-Fi Direct / BLE)** and **multi-hop store-and-forward mesh routing**.

Transferring rich media (photos: 1–5 MB, PDFs: 1–20 MB) introduces specific technical challenges:

| Challenge | Why it matters | Solution |
| :--- | :--- | :--- |
| **Mesh Envelope Size Limit** | `MeshEnvelope` currently caps payloads at 64 KB (`encryptedPayload.length > 65536`) to protect mesh relay buffers. | **Hybrid Payload Architecture**: Chunk large files or use Direct Peer `Payload.Type.FILE` when directly connected. |
| **Hop-by-Hop Relay Flooding** | Flooding a 10 MB PDF across 8 intermediate relay phones would deplete battery and storage. | **Thumbnails over Mesh + On-Demand Chunks** OR **Direct P2P Transfers** with metadata announcements. |
| **End-to-End Encryption (E2EE)** | Files must remain private; relay devices must never view the image or PDF contents. | **AES-256-GCM File Encryption** using derived symmetric file keys. |

---

## 2. Technical Architectures: Two Viable Approaches

### Approach A: Direct P2P File Transfer (Fastest & Recommended for Nearby Contacts)
Google Nearby Connections natively supports `Payload.Type.FILE` and `Payload.Type.STREAM` alongside `Payload.Type.BYTES`.

1. **Direct Connection**: When two devices are in direct range (Wi-Fi Aware / Wi-Fi Direct), transfer speeds reach **10–30 MB/s**.
2. **Transfer Workflow**:
   - Sender picks an image or PDF using Android's `ActivityResultContracts.GetContent()`.
   - App encrypts file with an ephemeral AES-256 key: `encrypted_file.bin`.
   - Sender sends an encrypted **File Header Message** via `Payload.Type.BYTES` containing:
     - `fileName`, `fileSize`, `mimeType`, `fileKey`, `fileHash` (SHA-256).
   - Sender invokes `connectionsClient.sendPayload(endpointId, Payload.fromFile(encryptedFile))`.
   - Receiver receives file in background, validates SHA-256, decrypts with `fileKey`, and updates UI.

---

### Approach B: Chunked Store-and-Forward Mesh (For Multi-Hop Relays)
When recipient is not directly connected and messages hop through intermediate phones:

1. **Image Compression**:
   - Compress images down to **WebP (100–300 KB)**.
   - Generate a 5 KB ultra-compressed micro-thumbnail.
2. **Chunking Mechanism**:
   - Split encrypted file into **32 KB or 48 KB chunks**.
   - Wrap each chunk into a standard `MeshEnvelope` with `type = EnvelopeType.FILE_CHUNK`:
     ```json
     {
       "fileId": "file_8f9a2b",
       "chunkIndex": 0,
       "totalChunks": 4,
       "sha256": "...",
       "chunkData": "base64EncodedBytes..."
     }
     ```
3. **Reassembly**:
   - Recipient device collects all chunks in SQLite / disk storage.
   - Once all chunks arrive, verify checksum, decrypt, and display in chat timeline.

---

## 3. Data Model Changes

### 1. Update `EnvelopeType` in `MeshEnvelope.kt`
```kotlin
enum class EnvelopeType {
    TEXT,
    DELIVERY_RECEIPT,
    IDENTITY_ANNOUNCE,
    PING,
    GROUP_TEXT,
    GROUP_INVITE,
    MEDIA_ANNOUNCEMENT, // Metadata: fileName, size, mimeType, key
    FILE_CHUNK          // Chunked binary payload
}
```

### 2. Update `ChatMessage` in `Models.kt`
Extend `ChatMessage` with attachment properties:
```kotlin
enum class MessageType {
    TEXT,
    IMAGE,
    PDF,
    AUDIO,
    FILE
}

data class ChatMessage(
    val messageId: String,
    val conversationId: String,
    val senderId: String,
    val recipientId: String,
    val content: String,               // Text caption or filename
    val encryptedPayload: String,
    val createdAt: Long,
    var deliveryState: DeliveryState,
    val isOutgoing: Boolean,
    var hopCount: Int = 0,
    // --- New Media Fields ---
    val messageType: MessageType = MessageType.TEXT,
    val mediaUri: String? = null,      // Local file path on device
    val mediaSize: Long = 0L,          // File size in bytes
    val mimeType: String? = null,      // e.g. "image/jpeg", "application/pdf"
    val transferProgress: Float = 1.0f // 0.0 to 1.0 for progress bar
)
```

### 3. Update SQLite Database Schema (`MeshDatabaseHelper.kt`)
Add columns to `messages` table:
```sql
ALTER TABLE messages ADD COLUMN message_type TEXT DEFAULT 'TEXT';
ALTER TABLE messages ADD COLUMN media_path TEXT DEFAULT NULL;
ALTER TABLE messages ADD COLUMN media_size INTEGER DEFAULT 0;
ALTER TABLE messages ADD COLUMN mime_type TEXT DEFAULT NULL;
```

---

## 4. UI & User Experience Enhancements

### 1. Attachment Action Sheet in `ConversationScreen.kt`
Add an attachment `+` (Paperclip) button next to the input field in `MessageComposer`:
- 📸 **Camera**: Capture photo directly.
- 🖼️ **Gallery / Photo**: Select JPG, PNG, WEBP.
- 📄 **Document**: Select PDF or text documents via Android Document Picker.
- 🎤 **Voice Note**: Already integrated waveform voice recording.

### 2. Rich Chat Bubbles in `MessageBubbleItem`
- **Images**: Render with rounded corners, aspect ratio preservation, click to view full screen.
- **PDFs**: Render a document card showing:
  - Red PDF icon
  - Document Title & Page Count / File Size (e.g., `Report.pdf • 2.4 MB`)
  - "Open Document" intent button using Android `Intent.ACTION_VIEW`.
- **Progress Bar**: Show transfer percentage and animated spinner during sending/receiving.

---

## 5. Security & Encryption Flow for Files

1. **Key Generation**: For every file transfer, generate an ephemeral 256-bit AES key:
   ```kotlin
   val fileKey = CryptoEngine.generateRandomKey()
   ```
2. **File Encryption**:
   - Stream file from disk through `CipherOutputStream` with `AES/GCM/NoPadding`.
   - Prevent out-of-memory errors by processing in 64 KB buffers.
3. **Key Encapsulation**:
   - Encrypt `fileKey` using the recipient's EC Public Key (EC P-256 + ECDH + HKDF).
   - Only the designated recipient can decrypt `fileKey` and the file data.
4. **Integrity Check**:
   - Calculate SHA-256 hash of original file to verify after decryption.

---

## 6. Implementation Milestones

- [ ] **Phase 1: Direct Image Sharing (P2P)**
  - Integrate Android Photo Picker (`ActivityResultContracts.PickVisualMedia`).
  - Send via `Payload.fromFile` on direct Nearby connections.
  - Display images in Compose using Coil or native Bitmap decoding.
- [ ] **Phase 2: PDF & Document Sharing**
  - Integrate Storage Access Framework (`ActivityResultContracts.OpenDocument`).
  - Implement Document preview bubble and external PDF viewer launch.
- [ ] **Phase 3: Multi-Hop Mesh Chunking**
  - Add `FILE_CHUNK` envelope handler in `MeshRouter`.
  - Store incoming chunks in temporary cache directory until complete.
  - Notify UI on 100% assembly.
