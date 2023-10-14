package com.example.characterbot

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

data class Message(
    val text: String = "",
    val sender: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

class ChatActivity : AppCompatActivity() {

    private lateinit var firestore: FirebaseFirestore
    private var chatRoomId: String? = null
    private lateinit var messageAdapter: MessageAdapter
    private lateinit var messagesList: MutableList<Message>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        firestore = FirebaseFirestore.getInstance()
        messagesList = mutableListOf()

        val rvMessages = findViewById<RecyclerView>(R.id.recyclerViewChat)
        val etMessage = findViewById<EditText>(R.id.editTextMessage)
        val btnSend = findViewById<ImageButton>(R.id.buttonSend)

        messageAdapter = MessageAdapter(messagesList)
        rvMessages.adapter = messageAdapter
        rvMessages.layoutManager = LinearLayoutManager(this)

        chatRoomId = intent.getStringExtra("chatRoomId")
        if (chatRoomId == null) {
            Toast.makeText(this, "Chatroom error!", Toast.LENGTH_SHORT).show()
            finish()
        }

        val tvUserName = findViewById<TextView>(R.id.tvUserName)
        val selectedUserName = intent.getStringExtra("selectedUserName")
        val currentUserName = intent.getStringExtra("currentUserName")


        if (selectedUserName != null) {
            supportActionBar?.title = selectedUserName
            tvUserName.text = selectedUserName
        } else {
            Toast.makeText(this, "User not found!", Toast.LENGTH_SHORT).show()
            finish()
        }


        val backButton: ImageButton = findViewById(R.id.backButton)
        backButton.setOnClickListener {
            finish()
        }

        btnSend.setOnClickListener {
            val text = etMessage.text.toString()
            if (text.isNotBlank()) {
                sendMessage(text, currentUserName ?: "Unknown User") // Use the currentUserName as the sender
                etMessage.text.clear()
            }
        }

        fetchMessages()
    }
    private fun fetchUserTokenAndSendNotification(username: String, title: String, body: String) {
        println("Fetching token for user: $username")  // Log the username you're trying to fetch the token for

        firestore.collection("users").whereEqualTo("name", username).limit(1).get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    val userDocument = querySnapshot.documents[0]
                    val token = userDocument.getString("fcmToken")
                    println("Fetched token for user $username: $token")  // Log the token you fetched

                    if (token != null) {
                        NotificationUtil.sendNotification(token, title, body)
                    } else {
                        Toast.makeText(this, "Failed to send notification. Token not found.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    println("User not found in Firestore: $username")  // Log if no user is found
                }
            }.addOnFailureListener { exception ->
                println("Error fetching token for user $username: ${exception.message}")  // Log any errors encountered
            }
    }



    @SuppressLint("NotifyDataSetChanged")
    private fun fetchMessages() {
        val messagesCollection = firestore.collection("chatRooms").document(chatRoomId!!).collection("messages")
        messagesCollection.orderBy("timestamp", Query.Direction.ASCENDING).addSnapshotListener { querySnapshot, error ->
            if (error != null) {
                Toast.makeText(this, "Failed to load messages!", Toast.LENGTH_SHORT).show()
                return@addSnapshotListener
            }

            messagesList.clear()
            for (document in querySnapshot!!.documents) {
                val message = document.toObject(Message::class.java)
                if (message != null) {
                    messagesList.add(message)
                }
            }
            messageAdapter.notifyDataSetChanged()
            findViewById<RecyclerView>(R.id.recyclerViewChat).scrollToPosition(messagesList.size - 1)
        }
    }

    private fun sendMessage(text: String, sender: String) {
        val message = Message(text, sender)
        val messagesCollection = firestore.collection("chatRooms").document(chatRoomId!!).collection("messages")
        messagesCollection.add(message).addOnSuccessListener {
            // Message was added successfully.
            val recipient = intent.getStringExtra("selectedUserName")
            if (recipient != null) {
                fetchUserTokenAndSendNotification(recipient, "New Message", "$sender: $text")
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Failed to send message!", Toast.LENGTH_SHORT).show()
        }
    }

}

class MessageAdapter(private val messages: List<Message>) : RecyclerView.Adapter<MessageAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvText: TextView = view.findViewById(R.id.tvText)
        val tvSender: TextView = view.findViewById(R.id.tvSender)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_message, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val message = messages[position]
        holder.tvText.text = message.text
        holder.tvSender.text = message.sender
    }

    override fun getItemCount() = messages.size
}
