package com.example.characterbot

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
//import android.text.Html
import android.widget.ArrayAdapter
import com.example.characterbot.databinding.ActivityMainBinding
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val firestore = FirebaseFirestore.getInstance()
    private val messagesCollection = firestore.collection("messages")
    private var character: ChatCharacter = ChatCharacter("Nick", R.style.Nick) // Replace with your default character

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val characters = listOf(
            ChatCharacter("Nick", R.style.Nick),
            ChatCharacter("Mike", R.style.Mike)
            // Add more characters as needed
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
                    characters[characterSpinner.selectedItemPosition] // Initialize character based on the selected character

                // Display user message as "user: message"
                val userMessageText = "user: $message"
                chatText.append(userMessageText)
                chatText.append("\n")

                // Generate a chatbot reply (you can replace this with more advanced logic)
                val chatbotReply = generateBasicChatbotReply(message)

                // Store the user's message in Firestore
                hashMapOf(
                    "sender" to "user",
                    "text" to message
                )
                //messagesCollection.add(userMessage)

                // Display the character's reply
                val characterReplyText = "${character.name}: $chatbotReply"
                chatText.append(characterReplyText)
                chatText.append("\n")

                chatInput.text.clear()
            }
        }



        // Listen for new chat messages from Firestore
        messagesCollection.addSnapshotListener { snapshot, exception ->
            if (exception != null) {
                // Handle the error
                return@addSnapshotListener
            }

            // Process new messages
            snapshot?.documents?.forEach { document ->
                val sender = document.getString("sender")
                val text = document.getString("text")

                if (sender == "user") {
                    // Display user message as "user: message"
                    chatText.append("user: $text\n")
                } else {
                    // Generate a basic chatbot reply (you can replace this with more advanced logic)
                    val chatbotReply = generateBasicChatbotReply(text)

                    // Display the character's reply as "character: reply"
                        chatText.append("$chatbotReply\n")
                }

                // Delete the message document to avoid processing it again
                document.reference.delete()
            }
        }
    }
        private fun generateBasicChatbotReply(userMessage: String?): String {
        // Implement your chatbot logic here (e.g., predefined responses)
        return "Thank you for your message: \"$userMessage\". This is a basic chatbot reply."
    }
}
