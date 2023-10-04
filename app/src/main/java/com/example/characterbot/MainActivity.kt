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
    private var character: ChatCharacter = ChatCharacter("Nick", R.style.Nick) //default character

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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

                // stores the user's message in Firestore
                hashMapOf(
                    "sender" to "user",
                    "text" to message
                )
                //messagesCollection.add(userMessage)

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
                return@addSnapshotListener //error handling
            }

            // if any new messages to be processed
            snapshot?.documents?.forEach { document ->
                val sender = document.getString("sender")
                val text = document.getString("text")

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
        // the idea is to train an LLM or simply use GPT4's api to make the bot reply in the style of the selected character
        return "Thank you for your message: \"$userMessage\". This is a basic chatbot reply."
    }
}
