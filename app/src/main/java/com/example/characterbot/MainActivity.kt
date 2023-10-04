package com.example.characterbot

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
//import android.text.Html
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
                character =
                    characters[characterSpinner.selectedItemPosition] // based on the selected character

                // displays the user message as "user: message"
                val userMessageText = "user: $message"
                chatText.append(userMessageText)
                chatText.append("\n")

                // generates a reply from the bot using a function defined later
                val chatbotReply = generateBasicChatbotReply(message)

                // Create a map to represent the message data
                val messageData = hashMapOf(
                    "sender" to "user",
                    "text" to message,
                    "timestamp" to System.currentTimeMillis(),
                    "isRead" to false,
                    "isLocal" to true
                )

                // Store the user's message in Firestore
                messagesCollection.add(messageData)

                // displays the character's reply
                val characterReplyText = "${character.name}: $chatbotReply"
                chatText.append(characterReplyText)
                chatText.append("\n")

                chatInput.text.clear()
            }
        }


        // listens for new chat messages from Firestore
        messagesCollection.addSnapshotListener { snapshot, exception ->
            if (exception != null) {
                Log.e("FirestoreError", "Error fetching chat messages", exception)
                return@addSnapshotListener //error handling
            }


            // if any new messages to be processed
            snapshot?.documents?.forEach { document ->
                val sender = document.getString("sender") ?: return@forEach
                val text = document.getString("text") ?: return@forEach

                document.reference.update("isRead", true)

                if (sender == "user") {

                    chatText.append("user: $text\n")
                } else {
                    val chatbotReply = generateBasicChatbotReply(text)
                    chatText.append("$chatbotReply\n")
                }

                // deletes the message document to avoid processing it again
                document.reference.delete()
            }
        }
    }

    private fun generateBasicChatbotReply(userMessage: String?): String {
        userMessage?.let { msg ->
            when (character.name) {
                "Nick" -> {
                    when {
                        msg.contains("hello", true) -> return "Hey. What do you want?"
                        msg.contains("how are you", true) -> return "Well, my bank account's a joke, I'm constantly confused, but, you know, just another day."
                        msg.contains("thank you", true) -> return "Yeah, yeah. Don't get all sentimental on me."
                        msg.contains("what's up", true) -> return "Just living my life, one awkward moment at a time."
                        else -> return "Look, sometimes life throws you a curveball, and I have no idea what you just said. Can you help me out?"
                    }
                }

                "Mike" -> {
                    when {
                        msg.contains("hello", true) -> return "That's what she said! Hello!"
                        msg.contains(
                            "how are you",
                            true
                        ) -> return "World's best boss here, always good!"

                        msg.contains(
                            "thank you",
                            true
                        ) -> return "You're the real Scranton Strangler for killing it with kindness!"

                        else -> return "Well, in the wise words of me, Michael Scott, I have no idea what you're talking about."
                    }
                }

                else -> return "Thank you for your message: \"$userMessage\". This is a basic chatbot reply."
            }
        }
        return "I'm sorry, I didn't understand that."
    }
}

