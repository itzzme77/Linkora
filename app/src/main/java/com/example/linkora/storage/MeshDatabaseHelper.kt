package com.example.linkora.storage

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.linkora.model.*

/**
 * SQLite Database Helper for Linkora Offline Mesh.
 * Handles persistent storage of:
 * 1. Messages and delivery status
 * 2. Verified contacts and cryptographic public keys
 * 3. Store-and-forward relay queue
 * 4. Deduplication records
 */
class MeshDatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS messages (
                message_id TEXT PRIMARY KEY,
                conversation_id TEXT NOT NULL,
                sender_id TEXT NOT NULL,
                recipient_id TEXT NOT NULL,
                content TEXT NOT NULL,
                encrypted_payload TEXT NOT NULL,
                created_at INTEGER NOT NULL,
                delivery_state TEXT NOT NULL,
                is_outgoing INTEGER NOT NULL,
                hop_count INTEGER NOT NULL DEFAULT 0,
                message_type TEXT NOT NULL DEFAULT 'TEXT',
                media_path TEXT,
                media_size INTEGER NOT NULL DEFAULT 0,
                mime_type TEXT,
                is_starred INTEGER NOT NULL DEFAULT 0,
                is_pinned INTEGER NOT NULL DEFAULT 0,
                reaction TEXT,
                is_edited INTEGER NOT NULL DEFAULT 0
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS contacts (
                identity_id TEXT PRIMARY KEY,
                username TEXT NOT NULL,
                public_key TEXT NOT NULL,
                verification_state TEXT NOT NULL,
                safety_fingerprint TEXT NOT NULL,
                last_seen INTEGER NOT NULL
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS relay_queue (
                message_id TEXT PRIMARY KEY,
                hop_limit INTEGER NOT NULL,
                next_attempt_at INTEGER NOT NULL,
                attempt_count INTEGER NOT NULL DEFAULT 0,
                expiry_at INTEGER NOT NULL,
                envelope_json TEXT NOT NULL
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS processed_messages (
                message_id TEXT PRIMARY KEY,
                processed_at INTEGER NOT NULL
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS groups (
                group_id TEXT PRIMARY KEY,
                name TEXT NOT NULL,
                creator_id TEXT NOT NULL,
                group_key_base64 TEXT NOT NULL,
                member_ids TEXT NOT NULL,
                created_at INTEGER NOT NULL
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS call_logs (
                call_id TEXT PRIMARY KEY,
                peer_id TEXT NOT NULL,
                peer_name TEXT NOT NULL,
                direction TEXT NOT NULL,
                duration_seconds INTEGER NOT NULL DEFAULT 0,
                timestamp INTEGER NOT NULL,
                end_reason TEXT
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS archived_conversations (
                conversation_id TEXT PRIMARY KEY,
                archived_at INTEGER NOT NULL
            )
            """.trimIndent()
        )

        db.execSQL("CREATE INDEX IF NOT EXISTS idx_messages_conv ON messages(conversation_id)")
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_relay_expiry ON relay_queue(expiry_at)")
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_call_logs_time ON call_logs(timestamp)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS groups (
                    group_id TEXT PRIMARY KEY,
                    name TEXT NOT NULL,
                    creator_id TEXT NOT NULL,
                    group_key_base64 TEXT NOT NULL,
                    member_ids TEXT NOT NULL,
                    created_at INTEGER NOT NULL
                )
                """.trimIndent()
            )
        }
        if (oldVersion < 3) {
            try {
                db.execSQL("ALTER TABLE messages ADD COLUMN message_type TEXT NOT NULL DEFAULT 'TEXT'")
            } catch (ignored: Exception) {}
            try {
                db.execSQL("ALTER TABLE messages ADD COLUMN media_path TEXT")
            } catch (ignored: Exception) {}
            try {
                db.execSQL("ALTER TABLE messages ADD COLUMN media_size INTEGER NOT NULL DEFAULT 0")
            } catch (ignored: Exception) {}
            try {
                db.execSQL("ALTER TABLE messages ADD COLUMN mime_type TEXT")
            } catch (ignored: Exception) {}
        }
        if (oldVersion < 4) {
            try {
                db.execSQL("ALTER TABLE messages ADD COLUMN is_starred INTEGER NOT NULL DEFAULT 0")
            } catch (ignored: Exception) {}
            try {
                db.execSQL("ALTER TABLE messages ADD COLUMN is_pinned INTEGER NOT NULL DEFAULT 0")
            } catch (ignored: Exception) {}
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS call_logs (
                    call_id TEXT PRIMARY KEY,
                    peer_id TEXT NOT NULL,
                    peer_name TEXT NOT NULL,
                    direction TEXT NOT NULL,
                    duration_seconds INTEGER NOT NULL DEFAULT 0,
                    timestamp INTEGER NOT NULL,
                    end_reason TEXT
                )
                """.trimIndent()
            )
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS archived_conversations (
                    conversation_id TEXT PRIMARY KEY,
                    archived_at INTEGER NOT NULL
                )
                """.trimIndent()
            )
        }
        if (oldVersion < 5) {
            try {
                db.execSQL("ALTER TABLE messages ADD COLUMN reaction TEXT")
            } catch (ignored: Exception) {}
            try {
                db.execSQL("ALTER TABLE messages ADD COLUMN is_edited INTEGER NOT NULL DEFAULT 0")
            } catch (ignored: Exception) {}
        }
    }

    /* ---------------- Messages ---------------- */

    @Synchronized
    fun insertOrUpdateMessage(message: ChatMessage) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("message_id", message.messageId)
            put("conversation_id", message.conversationId)
            put("sender_id", message.senderId)
            put("recipient_id", message.recipientId)
            put("content", message.content)
            put("encrypted_payload", message.encryptedPayload)
            put("created_at", message.createdAt)
            put("delivery_state", message.deliveryState.name)
            put("is_outgoing", if (message.isOutgoing) 1 else 0)
            put("hop_count", message.hopCount)
            put("message_type", message.messageType.name)
            put("media_path", message.mediaPath)
            put("media_size", message.mediaSize)
            put("mime_type", message.mimeType)
            put("is_starred", if (message.isStarred) 1 else 0)
            put("is_pinned", if (message.isPinned) 1 else 0)
            put("reaction", message.reaction)
            put("is_edited", if (message.isEdited) 1 else 0)
        }
        db.insertWithOnConflict(
            "messages",
            null,
            values,
            SQLiteDatabase.CONFLICT_REPLACE
        )
    }

    @Synchronized
    fun updateDeliveryState(messageId: String, newState: DeliveryState) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("delivery_state", newState.name)
        }
        db.update("messages", values, "message_id = ?", arrayOf(messageId))
    }

    @Synchronized
    fun updateMessageReaction(messageId: String, reaction: String?) {
        val db = writableDatabase
        val values = ContentValues().apply {
            if (reaction != null) {
                put("reaction", reaction)
            } else {
                putNull("reaction")
            }
        }
        db.update("messages", values, "message_id = ?", arrayOf(messageId))
    }

    @Synchronized
    fun editMessageContent(messageId: String, newContent: String) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("content", newContent)
            put("is_edited", 1)
        }
        db.update("messages", values, "message_id = ?", arrayOf(messageId))
    }

    @Synchronized
    fun markConversationAsRead(conversationId: String) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("delivery_state", DeliveryState.READ.name)
        }
        db.update("messages", values, "conversation_id = ? AND is_outgoing = 0", arrayOf(conversationId))
    }

    private fun cursorToChatMessage(cursor: android.database.Cursor): ChatMessage {
        val typeIdx = cursor.getColumnIndex("message_type")
        val pathIdx = cursor.getColumnIndex("media_path")
        val sizeIdx = cursor.getColumnIndex("media_size")
        val mimeIdx = cursor.getColumnIndex("mime_type")
        val starredIdx = cursor.getColumnIndex("is_starred")
        val pinnedIdx = cursor.getColumnIndex("is_pinned")
        val reactionIdx = cursor.getColumnIndex("reaction")
        val editedIdx = cursor.getColumnIndex("is_edited")

        val msgType = if (typeIdx != -1 && !cursor.isNull(typeIdx)) {
            try { MessageType.valueOf(cursor.getString(typeIdx)) } catch (e: Exception) { MessageType.TEXT }
        } else MessageType.TEXT

        val mediaPath = if (pathIdx != -1 && !cursor.isNull(pathIdx)) cursor.getString(pathIdx) else null
        val mediaSize = if (sizeIdx != -1 && !cursor.isNull(sizeIdx)) cursor.getLong(sizeIdx) else 0L
        val mimeType = if (mimeIdx != -1 && !cursor.isNull(mimeIdx)) cursor.getString(mimeIdx) else null
        val isStarred = if (starredIdx != -1 && !cursor.isNull(starredIdx)) cursor.getInt(starredIdx) == 1 else false
        val isPinned = if (pinnedIdx != -1 && !cursor.isNull(pinnedIdx)) cursor.getInt(pinnedIdx) == 1 else false
        val reaction = if (reactionIdx != -1 && !cursor.isNull(reactionIdx)) cursor.getString(reactionIdx) else null
        val isEdited = if (editedIdx != -1 && !cursor.isNull(editedIdx)) cursor.getInt(editedIdx) == 1 else false

        return ChatMessage(
            messageId = cursor.getString(cursor.getColumnIndexOrThrow("message_id")),
            conversationId = cursor.getString(cursor.getColumnIndexOrThrow("conversation_id")),
            senderId = cursor.getString(cursor.getColumnIndexOrThrow("sender_id")),
            recipientId = cursor.getString(cursor.getColumnIndexOrThrow("recipient_id")),
            content = cursor.getString(cursor.getColumnIndexOrThrow("content")),
            encryptedPayload = cursor.getString(cursor.getColumnIndexOrThrow("encrypted_payload")),
            createdAt = cursor.getLong(cursor.getColumnIndexOrThrow("created_at")),
            deliveryState = DeliveryState.valueOf(cursor.getString(cursor.getColumnIndexOrThrow("delivery_state"))),
            isOutgoing = cursor.getInt(cursor.getColumnIndexOrThrow("is_outgoing")) == 1,
            hopCount = cursor.getInt(cursor.getColumnIndexOrThrow("hop_count")),
            messageType = msgType,
            mediaPath = mediaPath,
            mediaSize = mediaSize,
            mimeType = mimeType,
            isStarred = isStarred,
            isPinned = isPinned,
            reaction = reaction,
            isEdited = isEdited
        )
    }

    @Synchronized
    fun getMessagesForConversation(conversationId: String): List<ChatMessage> {
        val list = mutableListOf<ChatMessage>()
        val db = readableDatabase
        val cursor = db.query(
            "messages",
            null,
            "conversation_id = ?",
            arrayOf(conversationId),
            null,
            null,
            "created_at ASC"
        )
        cursor.use {
            while (it.moveToNext()) {
                list.add(cursorToChatMessage(it))
            }
        }
        return list
    }

    @Synchronized
    fun toggleMessageStarred(messageId: String, isStarred: Boolean) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("is_starred", if (isStarred) 1 else 0)
        }
        db.update("messages", values, "message_id = ?", arrayOf(messageId))
    }

    @Synchronized
    fun toggleMessagePinned(messageId: String, isPinned: Boolean) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("is_pinned", if (isPinned) 1 else 0)
        }
        db.update("messages", values, "message_id = ?", arrayOf(messageId))
    }

    @Synchronized
    fun getStarredMessages(): List<ChatMessage> {
        val list = mutableListOf<ChatMessage>()
        val db = readableDatabase
        val cursor = db.query(
            "messages",
            null,
            "is_starred = 1",
            null,
            null,
            null,
            "created_at DESC"
        )
        cursor.use {
            while (it.moveToNext()) {
                list.add(cursorToChatMessage(it))
            }
        }
        return list
    }

    @Synchronized
    fun getPinnedMessagesForConversation(conversationId: String): List<ChatMessage> {
        val list = mutableListOf<ChatMessage>()
        val db = readableDatabase
        val cursor = db.query(
            "messages",
            null,
            "conversation_id = ? AND is_pinned = 1",
            arrayOf(conversationId),
            null,
            null,
            "created_at DESC"
        )
        cursor.use {
            while (it.moveToNext()) {
                list.add(cursorToChatMessage(it))
            }
        }
        return list
    }

    data class ConversationSummary(
        val conversationId: String,
        val contactName: String,
        val lastMessage: String,
        val lastTimestamp: Long,
        val deliveryState: DeliveryState,
        val isGroup: Boolean = false,
        val isArchived: Boolean = false
    )

    @Synchronized
    fun getAllConversations(): List<ConversationSummary> {
        val list = mutableListOf<ConversationSummary>()
        val db = readableDatabase
        // Get most recent message for each conversation (contact or group)
        val query = """
            SELECT m.conversation_id, m.content, m.created_at, m.delivery_state,
                   COALESCE(g.name, c.username, m.conversation_id) as contact_name,
                   CASE WHEN g.group_id IS NOT NULL THEN 1 ELSE 0 END as is_group,
                   CASE WHEN ac.conversation_id IS NOT NULL THEN 1 ELSE 0 END as is_archived
            FROM messages m
            LEFT JOIN contacts c ON m.conversation_id = c.identity_id
            LEFT JOIN groups g ON m.conversation_id = g.group_id
            LEFT JOIN archived_conversations ac ON m.conversation_id = ac.conversation_id
            WHERE m.created_at = (
                SELECT MAX(created_at) FROM messages WHERE conversation_id = m.conversation_id
            )
            ORDER BY m.created_at DESC
        """.trimIndent()

        val cursor = db.rawQuery(query, null)
        cursor.use {
            while (it.moveToNext()) {
                val archivedIdx = it.getColumnIndex("is_archived")
                val isArchived = if (archivedIdx != -1 && !it.isNull(archivedIdx)) it.getInt(archivedIdx) == 1 else false
                list.add(
                    ConversationSummary(
                        conversationId = it.getString(it.getColumnIndexOrThrow("conversation_id")),
                        contactName = it.getString(it.getColumnIndexOrThrow("contact_name")),
                        lastMessage = it.getString(it.getColumnIndexOrThrow("content")),
                        lastTimestamp = it.getLong(it.getColumnIndexOrThrow("created_at")),
                        deliveryState = DeliveryState.valueOf(it.getString(it.getColumnIndexOrThrow("delivery_state"))),
                        isGroup = it.getInt(it.getColumnIndexOrThrow("is_group")) == 1,
                        isArchived = isArchived
                    )
                )
            }
        }
        return list
    }

    /* ---------------- Call Logs ---------------- */

    @Synchronized
    fun insertCallLog(log: CallLogEntry) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("call_id", log.callId)
            put("peer_id", log.peerId)
            put("peer_name", log.peerName)
            put("direction", log.direction.name)
            put("duration_seconds", log.durationSeconds)
            put("timestamp", log.timestamp)
            put("end_reason", log.endReason)
        }
        db.insertWithOnConflict("call_logs", null, values, SQLiteDatabase.CONFLICT_REPLACE)
    }

    @Synchronized
    fun getCallLogs(): List<CallLogEntry> {
        val list = mutableListOf<CallLogEntry>()
        val db = readableDatabase
        val cursor = db.query(
            "call_logs",
            null,
            null,
            null,
            null,
            null,
            "timestamp DESC"
        )
        cursor.use {
            while (it.moveToNext()) {
                val directionStr = it.getString(it.getColumnIndexOrThrow("direction"))
                val dir = try { CallDirection.valueOf(directionStr) } catch (e: Exception) { CallDirection.INCOMING }
                val reasonIdx = it.getColumnIndex("end_reason")
                val endReason = if (reasonIdx != -1 && !it.isNull(reasonIdx)) it.getString(reasonIdx) else null
                list.add(
                    CallLogEntry(
                        callId = it.getString(it.getColumnIndexOrThrow("call_id")),
                        peerId = it.getString(it.getColumnIndexOrThrow("peer_id")),
                        peerName = it.getString(it.getColumnIndexOrThrow("peer_name")),
                        direction = dir,
                        durationSeconds = it.getInt(it.getColumnIndexOrThrow("duration_seconds")),
                        timestamp = it.getLong(it.getColumnIndexOrThrow("timestamp")),
                        endReason = endReason
                    )
                )
            }
        }
        return list
    }

    @Synchronized
    fun clearCallLogs() {
        val db = writableDatabase
        db.delete("call_logs", null, null)
    }

    /* ---------------- Archived Conversations ---------------- */

    @Synchronized
    fun archiveConversation(conversationId: String) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("conversation_id", conversationId)
            put("archived_at", System.currentTimeMillis())
        }
        db.insertWithOnConflict("archived_conversations", null, values, SQLiteDatabase.CONFLICT_REPLACE)
    }

    @Synchronized
    fun unarchiveConversation(conversationId: String) {
        val db = writableDatabase
        db.delete("archived_conversations", "conversation_id = ?", arrayOf(conversationId))
    }

    @Synchronized
    fun isConversationArchived(conversationId: String): Boolean {
        val db = readableDatabase
        val cursor = db.query(
            "archived_conversations",
            arrayOf("conversation_id"),
            "conversation_id = ?",
            arrayOf(conversationId),
            null,
            null,
            null
        )
        cursor.use {
            return it.moveToFirst()
        }
    }

    /* ---------------- Contacts ---------------- */

    @Synchronized
    fun insertOrUpdateContact(contact: Contact) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("identity_id", contact.identityId)
            put("username", contact.username)
            put("public_key", contact.publicKeyBase64)
            put("verification_state", contact.verificationState.name)
            put("safety_fingerprint", contact.safetyFingerprint)
            put("last_seen", contact.lastSeen)
        }
        db.insertWithOnConflict(
            "contacts",
            null,
            values,
            SQLiteDatabase.CONFLICT_REPLACE
        )
    }

    @Synchronized
    fun getContact(identityId: String): Contact? {
        val db = readableDatabase
        val cursor = db.query(
            "contacts",
            null,
            "identity_id = ?",
            arrayOf(identityId),
            null,
            null,
            null
        )
        cursor.use {
            if (it.moveToFirst()) {
                return Contact(
                    identityId = it.getString(it.getColumnIndexOrThrow("identity_id")),
                    username = it.getString(it.getColumnIndexOrThrow("username")),
                    publicKeyBase64 = it.getString(it.getColumnIndexOrThrow("public_key")),
                    verificationState = VerificationState.valueOf(it.getString(it.getColumnIndexOrThrow("verification_state"))),
                    safetyFingerprint = it.getString(it.getColumnIndexOrThrow("safety_fingerprint")),
                    lastSeen = it.getLong(it.getColumnIndexOrThrow("last_seen"))
                )
            }
        }
        return null
    }

    @Synchronized
    fun getAllContacts(): List<Contact> {
        val list = mutableListOf<Contact>()
        val db = readableDatabase
        val cursor = db.query("contacts", null, null, null, null, null, "last_seen DESC")
        cursor.use {
            while (it.moveToNext()) {
                list.add(
                    Contact(
                        identityId = it.getString(it.getColumnIndexOrThrow("identity_id")),
                        username = it.getString(it.getColumnIndexOrThrow("username")),
                        publicKeyBase64 = it.getString(it.getColumnIndexOrThrow("public_key")),
                        verificationState = VerificationState.valueOf(it.getString(it.getColumnIndexOrThrow("verification_state"))),
                        safetyFingerprint = it.getString(it.getColumnIndexOrThrow("safety_fingerprint")),
                        lastSeen = it.getLong(it.getColumnIndexOrThrow("last_seen"))
                    )
                )
            }
        }
        return list
    }

    /**
     * Returns only verified friends (paired via QR/NFC/ID) or contacts with active message history.
     * Anonymous mesh relay nodes do not clutter the user's friend list.
     */
    @Synchronized
    fun getFriends(): List<Contact> {
        val list = mutableListOf<Contact>()
        val db = readableDatabase
        val query = """
            SELECT * FROM contacts 
            WHERE verification_state = 'VERIFIED' 
               OR identity_id IN (SELECT DISTINCT conversation_id FROM messages)
            ORDER BY last_seen DESC
        """.trimIndent()
        val cursor = db.rawQuery(query, null)
        cursor.use {
            while (it.moveToNext()) {
                list.add(
                    Contact(
                        identityId = it.getString(it.getColumnIndexOrThrow("identity_id")),
                        username = it.getString(it.getColumnIndexOrThrow("username")),
                        publicKeyBase64 = it.getString(it.getColumnIndexOrThrow("public_key")),
                        verificationState = VerificationState.valueOf(it.getString(it.getColumnIndexOrThrow("verification_state"))),
                        safetyFingerprint = it.getString(it.getColumnIndexOrThrow("safety_fingerprint")),
                        lastSeen = it.getLong(it.getColumnIndexOrThrow("last_seen"))
                    )
                )
            }
        }
        return list
    }

    @Synchronized
    fun deleteMessage(messageId: String) {
        val db = writableDatabase
        db.delete("messages", "message_id = ?", arrayOf(messageId))
    }

    @Synchronized
    fun deleteConversation(conversationId: String) {
        val db = writableDatabase
        db.delete("messages", "conversation_id = ?", arrayOf(conversationId))
    }

    @Synchronized
    fun deleteContact(identityId: String) {
        val db = writableDatabase
        db.delete("contacts", "identity_id = ?", arrayOf(identityId))
    }

    @Synchronized
    fun deleteGroup(groupId: String) {
        val db = writableDatabase
        db.delete("groups", "group_id = ?", arrayOf(groupId))
    }

    /**
     * Purges all mock / demo seed data and conversations so only real user data remains.
     */
    @Synchronized
    fun purgeDemoData() {
        val db = writableDatabase
        db.beginTransaction()
        try {
            // 1. Delete seed messages
            db.delete("messages", "message_id LIKE 'msg_seed_%' OR encrypted_payload = 'seed' OR content LIKE '%Hello Charlie!%'", null)

            // 2. Delete demo contacts by ID or username
            val demoContactIds = arrayOf(
                "usr_rohan_patil",
                "usr_sneha_sharma",
                "usr_college_group",
                "usr_travel_buddies",
                "usr_family",
                "usr_aditya",
                "usr_priya_deshmukh",
                "usr_arjun"
            )
            val placeholders = demoContactIds.joinToString(",") { "?" }
            db.delete("contacts", "identity_id IN ($placeholders)", demoContactIds)
            db.delete("contacts", "username IN ('Rohan Patil', 'Sneha Sharma', 'College Group', 'Travel Buddies', 'Family', 'Aditya', 'Priya Deshmukh', 'Arjun', 'Charlie')", null)

            // 3. Delete demo groups
            val demoGroupIds = arrayOf(
                "usr_college_group",
                "usr_travel_buddies",
                "usr_family"
            )
            val groupPlaceholders = demoGroupIds.joinToString(",") { "?" }
            db.delete("groups", "group_id IN ($groupPlaceholders)", demoGroupIds)
            db.delete("groups", "name IN ('College Group', 'Travel Buddies', 'Family')", null)

            // 4. Delete messages belonging to demo conversation ids
            db.delete("messages", "conversation_id IN ($placeholders)", demoContactIds)

            // 5. Delete demo/mock contacts and messages like Sarah Jenkins
            db.delete("contacts", "identity_id LIKE 'usr_airdrop_%' OR identity_id = 'usr_sarah_sim' OR username = 'Sarah Jenkins'", null)
            db.delete("messages", "conversation_id LIKE 'usr_airdrop_%' OR conversation_id = 'usr_sarah_sim' OR sender_id = 'usr_sarah_sim'", null)
            db.delete("call_logs", "peer_id = 'usr_sarah_sim' OR peer_name = 'Sarah Jenkins'", null)

            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    /* ---------------- Deduplication ---------------- */

    @Synchronized
    fun isMessageProcessed(messageId: String): Boolean {
        val db = readableDatabase
        val cursor = db.query(
            "processed_messages",
            arrayOf("message_id"),
            "message_id = ?",
            arrayOf(messageId),
            null,
            null,
            null
        )
        val exists = cursor.count > 0
        cursor.close()
        return exists
    }

    @Synchronized
    fun markMessageProcessed(messageId: String) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("message_id", messageId)
            put("processed_at", System.currentTimeMillis())
        }
        db.insertWithOnConflict(
            "processed_messages",
            null,
            values,
            SQLiteDatabase.CONFLICT_IGNORE
        )
    }

    /* ---------------- Relay Queue ---------------- */

    @Synchronized
    fun addToRelayQueue(item: RelayQueueItem) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("message_id", item.messageId)
            put("hop_limit", item.hopLimit)
            put("next_attempt_at", item.nextAttemptAt)
            put("attempt_count", item.attemptCount)
            put("expiry_at", item.expiryAt)
            put("envelope_json", item.envelopeJson)
        }
        db.insertWithOnConflict(
            "relay_queue",
            null,
            values,
            SQLiteDatabase.CONFLICT_REPLACE
        )
    }

    @Synchronized
    fun removeFromRelayQueue(messageId: String) {
        val db = writableDatabase
        db.delete("relay_queue", "message_id = ?", arrayOf(messageId))
    }

    @Synchronized
    fun getPendingRelayQueue(maxCount: Int = 50): List<RelayQueueItem> {
        val list = mutableListOf<RelayQueueItem>()
        val db = readableDatabase
        val now = System.currentTimeMillis()
        val cursor = db.query(
            "relay_queue",
            null,
            "expiry_at > ? AND next_attempt_at <= ?",
            arrayOf(now.toString(), now.toString()),
            null,
            null,
            "next_attempt_at ASC",
            maxCount.toString()
        )
        cursor.use {
            while (it.moveToNext()) {
                list.add(
                    RelayQueueItem(
                        messageId = it.getString(it.getColumnIndexOrThrow("message_id")),
                        hopLimit = it.getInt(it.getColumnIndexOrThrow("hop_limit")),
                        nextAttemptAt = it.getLong(it.getColumnIndexOrThrow("next_attempt_at")),
                        attemptCount = it.getInt(it.getColumnIndexOrThrow("attempt_count")),
                        expiryAt = it.getLong(it.getColumnIndexOrThrow("expiry_at")),
                        envelopeJson = it.getString(it.getColumnIndexOrThrow("envelope_json"))
                    )
                )
            }
        }
        return list
    }

    @Synchronized
    fun pruneExpiredRelayQueue() {
        val db = writableDatabase
        db.delete("relay_queue", "expiry_at < ?", arrayOf(System.currentTimeMillis().toString()))
    }

    /* ---------------- Groups ---------------- */

    @Synchronized
    fun insertOrUpdateGroup(group: GroupChat) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("group_id", group.groupId)
            put("name", group.name)
            put("creator_id", group.creatorId)
            put("group_key_base64", group.groupKeyBase64)
            put("member_ids", group.memberIds.joinToString(","))
            put("created_at", group.createdAt)
        }
        db.insertWithOnConflict(
            "groups",
            null,
            values,
            SQLiteDatabase.CONFLICT_REPLACE
        )
    }

    @Synchronized
    fun getGroup(groupId: String): GroupChat? {
        val db = readableDatabase
        val cursor = db.query(
            "groups",
            null,
            "group_id = ?",
            arrayOf(groupId),
            null,
            null,
            null
        )
        cursor.use {
            if (it.moveToFirst()) {
                val memberIdsStr = it.getString(it.getColumnIndexOrThrow("member_ids"))
                return GroupChat(
                    groupId = it.getString(it.getColumnIndexOrThrow("group_id")),
                    name = it.getString(it.getColumnIndexOrThrow("name")),
                    creatorId = it.getString(it.getColumnIndexOrThrow("creator_id")),
                    groupKeyBase64 = it.getString(it.getColumnIndexOrThrow("group_key_base64")),
                    memberIds = if (memberIdsStr.isBlank()) emptyList() else memberIdsStr.split(","),
                    createdAt = it.getLong(it.getColumnIndexOrThrow("created_at"))
                )
            }
        }
        return null
    }

    @Synchronized
    fun getAllGroups(): List<GroupChat> {
        val list = mutableListOf<GroupChat>()
        val db = readableDatabase
        val cursor = db.query("groups", null, null, null, null, null, "created_at DESC")
        cursor.use {
            while (it.moveToNext()) {
                val memberIdsStr = it.getString(it.getColumnIndexOrThrow("member_ids"))
                list.add(
                    GroupChat(
                        groupId = it.getString(it.getColumnIndexOrThrow("group_id")),
                        name = it.getString(it.getColumnIndexOrThrow("name")),
                        creatorId = it.getString(it.getColumnIndexOrThrow("creator_id")),
                        groupKeyBase64 = it.getString(it.getColumnIndexOrThrow("group_key_base64")),
                        memberIds = if (memberIdsStr.isBlank()) emptyList() else memberIdsStr.split(","),
                        createdAt = it.getLong(it.getColumnIndexOrThrow("created_at"))
                    )
                )
            }
        }
        return list
    }

    companion object {
        private const val DATABASE_NAME = "linkora_mesh.db"
        private const val DATABASE_VERSION = 5
    }
}
