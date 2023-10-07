package com.example.characterbot

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import android.widget.ArrayAdapter
import com.example.characterbot.databinding.ActivityMainBinding
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val firestore = FirebaseFirestore.getInstance()
    private val messagesCollection = firestore.collection("messages")
    private var character: ChatCharacter = ChatCharacter("Nick", R.style.Nick) //default character

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val characters = listOf(
            ChatCharacter("Nick", R.style.Nick),
            ChatCharacter("Mike", R.style.Mike)
            // can add more characters
        )

        val characterSpinner = binding.characterSpinner
        val characterAdapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_item, characters.map { it.name })
        characterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        characterSpinner.adapter = characterAdapter

        val chatText = binding.chatText
        val chatInput = binding.chatInput
        val sendButton = binding.sendButton

        sendButton.setOnClickListener {
            val message = chatInput.text.toString()
            if (message.isNotEmpty()) {
                character = characters[characterSpinner.selectedItemPosition]

                // displays the user message as "user: message"
                val userMessageText = "user: $message"
                chatText.append(userMessageText)
                chatText.append("\n")

                // creates a map to represent the user's message data
                val userMessageData = hashMapOf(
                    "sender" to "user",
                    "text" to message,
                    "timestamp" to System.currentTimeMillis()
                )

                // store the user's message in Firestore
                messagesCollection.add(userMessageData)

                // Wait for a short moment before storing the bot's message
                chatInput.postDelayed({
                    val chatbotReply = generateBasicChatbotReply(message)
                    val characterReplyText = "${character.name}: $chatbotReply"
                    chatText.append(characterReplyText)
                    chatText.append("\n")

                    // creates a map to represent the bot's message data
                    val botMessageData = hashMapOf(
                        "sender" to character.name, // Store the character's name here
                        "text" to chatbotReply,
                        "timestamp" to System.currentTimeMillis()
                    )

                    // store the bot's reply in Firestore
                    messagesCollection.add(botMessageData)
                }, 50) // 50 milliseconds delay

                chatInput.text.clear()
            }
        }


// listens for unread chat messages from Firestore
        messagesCollection
            .orderBy("timestamp")
            .addSnapshotListener { snapshot, exception ->
                if (exception != null) {
                    Log.e("FirestoreError", "Error fetching chat messages", exception)
                    return@addSnapshotListener
                }

                chatText.text = "" // Clear the chat text to avoid duplicates

                snapshot?.documents?.forEach { document ->
                    val sender = document.getString("sender") ?: return@forEach
                    val text = document.getString("text") ?: return@forEach
                    chatText.append("$sender: $text\n")
                }

            }

    }


    private fun generateBasicChatbotReply(userMessage: String?): String {
        userMessage?.let { msg ->
            when (character.name) {
                "Nick" -> {
                    when {
                        msg.contains("hello", true) -> return "Hey. What do you need, huh?"
                        msg.contains("how are you", true) -> return "Better before you asked. Just kidding... or am I? Anyway, what's up?"
                        msg.contains("thank you", true) -> return "Alright, enough with the feelings. But, yeah, you're welcome or whatever."
                        msg.contains("what's up", true) -> return "You know, just figuring life out, one mistake at a time. You?"
                        msg.contains("stressed", true) -> return "Stress is my middle name. Actually, it's not, but it might as well be. What's got you wound up?"
                        msg.contains("anxious", true) -> return "Anxiety? I get anxious about having anxiety. So, spill it."
                        else -> return "Look, I'm not great with... words, or feelings. Or... much really. But talk to me."
                    }
                }

                "Mike" -> {
                    when {
                        msg.contains("hello", true) -> return "That's what she said! But seriously, how can I help you today?"
                        msg.contains("how are you", true) -> return "Best mood ever, thanks to my 'World's Best Boss' mug. But enough about me!"
                        msg.contains("thank you", true) -> return "Every time you say thanks, an angel gets its wings! You're welcome."
                        msg.contains("need advice", true) -> return "Advice is like beets. Some people love 'em, some people don't. What's on your mind?"
                        msg.contains("feeling down", true) -> return "If I've learned one thing, it's that the Dundies can lift anyone's spirits! What's been going on?"
                        msg.contains("struggling", true) -> return "I once burned my foot on a George Foreman Grill, so I get it. Life's tough. Talk to me."
                        else -> return "In the wise, paraphrased words of Michael Scott, sometimes I start a sentence, and I don't even know where it's going. But I'll always listen to you."
                    }
                }

                else -> return "Thank you for your message: \"$userMessage\". This is a basic chatbot reply."
            }
        }
        return "I'm sorry, I didn't understand that."
    }

}

