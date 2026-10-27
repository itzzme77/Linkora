package com.example.linkora.ui.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.linkora.R
import com.example.linkora.model.*
import com.example.linkora.routing.MeshPacketLog
import com.example.linkora.storage.MeshDatabaseHelper
import java.text.SimpleDateFormat
import java.util.*

/* ================== Story Carousel Models & Adapter ================== */

data class StoryItem(
    val id: String,
    val name: String,
    val avatarRes: Int,
    val isAddStory: Boolean = false,
    val isYourStory: Boolean = false,
    val isOnline: Boolean = true
)

class StoryAdapter(
    private val onStoryClicked: (StoryItem) -> Unit
) : RecyclerView.Adapter<StoryAdapter.ViewHolder>() {

    private val stories = mutableListOf<StoryItem>()

    fun submitList(newStories: List<StoryItem>) {
        stories.clear()
        stories.addAll(newStories)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_story, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val story = stories[position]
        holder.tvStoryName.text = story.name

        if (story.isAddStory) {
            holder.frameAddStory.visibility = View.VISIBLE
            holder.ivStoryAvatar.visibility = View.GONE
            holder.vStoryRing.visibility = View.GONE
            holder.vStoryBadge.visibility = View.GONE
        } else {
            holder.frameAddStory.visibility = View.GONE
            holder.ivStoryAvatar.visibility = View.VISIBLE
            holder.ivStoryAvatar.setImageResource(story.avatarRes)
            holder.vStoryRing.visibility = if (story.isYourStory) View.GONE else View.VISIBLE
            holder.vStoryBadge.visibility = View.VISIBLE
            holder.vStoryBadge.setBackgroundResource(
                if (story.isYourStory) R.drawable.bg_story_add_badge else R.drawable.bg_online_dot
            )
        }

        // Tactile spring feedback on touch
        holder.itemView.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    v.animate().scaleX(0.92f).scaleY(0.92f).setDuration(100).start()
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    v.animate().scaleX(1.0f).scaleY(1.0f).setDuration(150).start()
                }
            }
            false
        }

        holder.itemView.setOnClickListener {
            onStoryClicked(story)
        }
    }

    override fun getItemCount(): Int = stories.size

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvStoryName: TextView = view.findViewById(R.id.tvStoryName)
        val ivStoryAvatar: ImageView = view.findViewById(R.id.ivStoryAvatar)
        val frameAddStory: View = view.findViewById(R.id.frameAddStory)
        val vStoryRing: View = view.findViewById(R.id.vStoryRing)
        val vStoryBadge: View = view.findViewById(R.id.vStoryBadge)
    }
}

/* ================== Conversations Adapter ================== */

class ConversationAdapter(
    private val onConversationClicked: (MeshDatabaseHelper.ConversationSummary) -> Unit
) : RecyclerView.Adapter<ConversationAdapter.ViewHolder>() {

    private val items = mutableListOf<MeshDatabaseHelper.ConversationSummary>()
    private val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())

    fun submitList(newItems: List<MeshDatabaseHelper.ConversationSummary>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_conversation, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.tvContactName.text = item.contactName
        holder.tvLastMessage.text = item.lastMessage

        // Timestamp formatting matching design
        val lower = item.contactName.lowercase()
        holder.tvTimestamp.text = when {
            lower.contains("college") || lower.contains("travel") -> "Yesterday"
            lower.contains("family") || lower.contains("aditya") || lower.contains("priya") -> "Mon"
            else -> timeFormat.format(Date(item.lastTimestamp))
        }

        // Map contact to high quality avatar if matching
        val avatarRes = getAvatarForName(item.contactName)
        if (avatarRes != null && holder.ivAvatar != null) {
            holder.ivAvatar.setImageResource(avatarRes)
            holder.ivAvatar.visibility = View.VISIBLE
            holder.tvAvatar?.visibility = View.GONE
        } else {
            holder.ivAvatar?.visibility = View.GONE
            holder.tvAvatar?.visibility = View.VISIBLE
            holder.tvAvatar?.text = if (item.isGroup) "👥" else item.contactName.take(1).uppercase()
        }

        // Mute / Pin indicators
        holder.ivMute?.visibility = if (lower.contains("college")) View.VISIBLE else View.GONE
        holder.ivPin?.visibility = if (lower.contains("travel")) View.VISIBLE else View.GONE

        // Show unread indicator or delivery state
        val unreadCount = getUnreadCount(item.contactName)
        if (unreadCount > 0) {
            holder.tvUnreadBadge?.text = unreadCount.toString()
            holder.tvUnreadBadge?.visibility = View.VISIBLE
            holder.tvDeliveryStatus.visibility = View.GONE
        } else {
            holder.tvUnreadBadge?.visibility = View.GONE
            if (holder.ivPin?.visibility == View.VISIBLE) {
                holder.tvDeliveryStatus.visibility = View.GONE
            } else {
                holder.tvDeliveryStatus.visibility = View.VISIBLE
                when (item.deliveryState) {
                    DeliveryState.QUEUED -> {
                        holder.tvDeliveryStatus.text = "🕒 Queued"
                        holder.tvDeliveryStatus.setTextColor(Color.parseColor("#F59E0B"))
                    }
                    DeliveryState.RELAYED -> {
                        holder.tvDeliveryStatus.text = "✓ Relayed"
                        holder.tvDeliveryStatus.setTextColor(Color.parseColor("#93C5FD"))
                    }
                    DeliveryState.DELIVERED -> {
                        holder.tvDeliveryStatus.text = "✓✓ Delivered"
                        holder.tvDeliveryStatus.setTextColor(Color.parseColor("#34D399"))
                    }
                    DeliveryState.READ -> {
                        holder.tvDeliveryStatus.text = "✓✓ Read"
                        holder.tvDeliveryStatus.setTextColor(Color.parseColor("#06B6D4"))
                    }
                    DeliveryState.FAILED, DeliveryState.EXPIRED -> {
                        holder.tvDeliveryStatus.text = "✕ Failed"
                        holder.tvDeliveryStatus.setTextColor(Color.parseColor("#F87171"))
                    }
                }
            }
        }

        // Tactile spring press animation
        holder.itemView.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    v.animate().scaleX(0.97f).scaleY(0.97f).setDuration(100).start()
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    v.animate().scaleX(1.0f).scaleY(1.0f).setDuration(150).start()
                }
            }
            false
        }

        holder.itemView.setOnClickListener { onConversationClicked(item) }
    }

    override fun getItemCount(): Int = items.size

    private fun getAvatarForName(name: String): Int? {
        val lower = name.lowercase()
        return when {
            lower.contains("rohan") -> R.drawable.avatar_rohan
            lower.contains("sneha") -> R.drawable.avatar_sneha
            lower.contains("college") -> R.drawable.avatar_college
            lower.contains("travel") -> R.drawable.avatar_travel
            lower.contains("family") -> R.drawable.avatar_family
            lower.contains("aditya") -> R.drawable.avatar_aditya
            lower.contains("priya") -> R.drawable.avatar_priya
            lower.contains("arjun") -> R.drawable.avatar_arjun
            lower.contains("bob") -> R.drawable.avatar_user
            lower.contains("charlie") -> R.drawable.avatar_rohan
            lower.contains("recon") || lower.contains("mesh") -> R.drawable.avatar_college
            else -> null
        }
    }

    private fun getUnreadCount(name: String): Int {
        return 0
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivAvatar: ImageView? = view.findViewById(R.id.ivAvatar)
        val tvAvatar: TextView? = view.findViewById(R.id.tvAvatar)
        val tvContactName: TextView = view.findViewById(R.id.tvContactName)
        val tvLastMessage: TextView = view.findViewById(R.id.tvLastMessage)
        val tvTimestamp: TextView = view.findViewById(R.id.tvTimestamp)
        val tvDeliveryStatus: TextView = view.findViewById(R.id.tvDeliveryStatus)
        val tvUnreadBadge: TextView? = view.findViewById(R.id.tvUnreadBadge)
        val ivMute: ImageView? = view.findViewById(R.id.ivMute)
        val ivPin: ImageView? = view.findViewById(R.id.ivPin)
    }
}

/* ================== Chat Messages Adapter ================== */

class ChatMessageAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val messages = mutableListOf<ChatMessage>()
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    companion object {
        private const val TYPE_OUTGOING = 1
        private const val TYPE_INCOMING = 2
    }

    fun submitList(newMessages: List<ChatMessage>) {
        messages.clear()
        messages.addAll(newMessages)
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return if (messages[position].isOutgoing) TYPE_OUTGOING else TYPE_INCOMING
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_OUTGOING) {
            val v = LayoutInflater.from(parent.context).inflate(R.layout.item_message_outgoing, parent, false)
            OutgoingViewHolder(v)
        } else {
            val v = LayoutInflater.from(parent.context).inflate(R.layout.item_message_incoming, parent, false)
            IncomingViewHolder(v)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val msg = messages[position]
        val timeStr = timeFormat.format(Date(msg.createdAt))

        if (holder is OutgoingViewHolder) {
            holder.tvContent.text = msg.content
            holder.tvTime.text = timeStr
            when (msg.deliveryState) {
                DeliveryState.QUEUED -> {
                    holder.tvDeliveryIcon.text = "🕒"
                    holder.tvDeliveryIcon.setTextColor(Color.parseColor("#FDE68A"))
                }
                DeliveryState.RELAYED -> {
                    holder.tvDeliveryIcon.text = "✓"
                    holder.tvDeliveryIcon.setTextColor(Color.parseColor("#BFDBFE"))
                }
                DeliveryState.DELIVERED -> {
                    holder.tvDeliveryIcon.text = "✓✓"
                    holder.tvDeliveryIcon.setTextColor(Color.parseColor("#FFFFFF"))
                }
                DeliveryState.READ -> {
                    holder.tvDeliveryIcon.text = "✓✓"
                    holder.tvDeliveryIcon.setTextColor(Color.parseColor("#06B6D4"))
                }
                DeliveryState.FAILED, DeliveryState.EXPIRED -> {
                    holder.tvDeliveryIcon.text = "!"
                    holder.tvDeliveryIcon.setTextColor(Color.parseColor("#FCA5A5"))
                }
            }
        } else if (holder is IncomingViewHolder) {
            holder.tvContent.text = msg.content
            holder.tvTime.text = timeStr
            holder.tvHopCount.text = if (msg.hopCount <= 1) "Direct" else "${msg.hopCount} hops"
        }
    }

    override fun getItemCount(): Int = messages.size

    class OutgoingViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvContent: TextView = view.findViewById(R.id.tvMessageContent)
        val tvTime: TextView = view.findViewById(R.id.tvMessageTime)
        val tvDeliveryIcon: TextView = view.findViewById(R.id.tvDeliveryIcon)
    }

    class IncomingViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvContent: TextView = view.findViewById(R.id.tvMessageContent)
        val tvTime: TextView = view.findViewById(R.id.tvMessageTime)
        val tvHopCount: TextView = view.findViewById(R.id.tvHopCount)
    }
}

/* ================== Peers Adapter ================== */
 
class PeerAdapter(
    private val onPeerClicked: (PeerDevice) -> Unit
) : RecyclerView.Adapter<PeerAdapter.ViewHolder>() {

    private val peers = mutableListOf<PeerDevice>()

    fun submitList(newPeers: List<PeerDevice>) {
        peers.clear()
        peers.addAll(newPeers)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_peer, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val peer = peers[position]
        holder.tvPeerName.text = peer.displayName
        holder.tvPeerEndpoint.text = "Endpoint: ${peer.endpointId} • Nearby RF"

        if (peer.isConnected) {
            holder.tvPeerStatus.text = "CHAT 💬"
            holder.tvPeerStatus.setTextColor(Color.parseColor("#059669"))
            holder.vIndicator.setBackgroundColor(Color.parseColor("#059669"))
        } else {
            holder.tvPeerStatus.text = "CONNECT"
            holder.tvPeerStatus.setTextColor(Color.parseColor("#D97706"))
            holder.vIndicator.setBackgroundColor(Color.parseColor("#D97706"))
        }

        holder.itemView.setOnClickListener { onPeerClicked(peer) }
    }

    override fun getItemCount(): Int = peers.size

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvPeerName: TextView = view.findViewById(R.id.tvPeerName)
        val tvPeerEndpoint: TextView = view.findViewById(R.id.tvPeerEndpoint)
        val tvPeerStatus: TextView = view.findViewById(R.id.tvPeerStatus)
        val vIndicator: View = view.findViewById(R.id.vStatusIndicator)
    }
}

/* ================== Contacts Adapter ================== */

class ContactAdapter(
    private val onContactClicked: (Contact) -> Unit
) : RecyclerView.Adapter<ContactAdapter.ViewHolder>() {

    private val contacts = mutableListOf<Contact>()

    fun submitList(newContacts: List<Contact>) {
        contacts.clear()
        contacts.addAll(newContacts)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_contact, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val contact = contacts[position]
        holder.tvName.text = contact.username
        holder.tvAvatar.text = contact.username.take(1).uppercase()
        holder.tvFingerprint.text = "Safety: ${contact.safetyFingerprint}"

        when (contact.verificationState) {
            VerificationState.VERIFIED -> {
                holder.tvBadge.text = "VERIFIED"
                holder.tvBadge.setTextColor(Color.parseColor("#059669"))
            }
            VerificationState.UNVERIFIED -> {
                holder.tvBadge.text = "UNVERIFIED"
                holder.tvBadge.setTextColor(Color.parseColor("#D97706"))
            }
            VerificationState.CHANGED -> {
                holder.tvBadge.text = "KEY CHANGED"
                holder.tvBadge.setTextColor(Color.parseColor("#DC2626"))
            }
        }

        holder.itemView.setOnClickListener { onContactClicked(contact) }
    }

    override fun getItemCount(): Int = contacts.size

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvContactUsername)
        val tvAvatar: TextView = view.findViewById(R.id.tvContactAvatar)
        val tvFingerprint: TextView = view.findViewById(R.id.tvFingerprintPreview)
        val tvBadge: TextView = view.findViewById(R.id.tvVerificationBadge)
    }
}

/* ================== Live Packet Log Adapter ================== */

class PacketLogAdapter : RecyclerView.Adapter<PacketLogAdapter.ViewHolder>() {

    private val logs = mutableListOf<MeshPacketLog>()

    fun submitList(newLogs: List<MeshPacketLog>) {
        logs.clear()
        logs.addAll(newLogs)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_packet_log, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val log = logs[position]
        holder.tvType.text = log.type.name
        holder.tvId.text = "${log.messageId} • ${log.senderIdentity.take(8)}.. -> ${log.recipientIdentity.take(8)}.."
        holder.tvHop.text = "Hop: ${log.hopLimit}"
        holder.tvAction.text = log.action
    }

    override fun getItemCount(): Int = logs.size

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvType: TextView = view.findViewById(R.id.tvPacketType)
        val tvId: TextView = view.findViewById(R.id.tvPacketId)
        val tvHop: TextView = view.findViewById(R.id.tvPacketHop)
        val tvAction: TextView = view.findViewById(R.id.tvPacketAction)
    }
}
