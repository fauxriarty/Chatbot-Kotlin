package com.example.characterbot

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.text.Html
import android.widget.ArrayAdapter
import com.example.characterbot.databinding.ActivityMainBinding
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

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
        val characterAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, characters.map { it.name })
        characterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        characterSpinner.adapter = characterAdapter

        val chatText = binding.chatText
        val chatInput = binding.chatInput
        val sendButton = binding.sendButton

        sendButton.setOnClickListener {
            val message = chatInput.text.toString()
            if (message.isNotEmpty()) {
                val character = characters[characterSpinner.selectedItemPosition]
                val styledMessage = "<span class=\"${resources.getResourceEntryName(character.style)}\">$message</span>"
                chatText.append(Html.fromHtml(styledMessage, Html.FROM_HTML_MODE_LEGACY))
                chatText.append("\n")
                chatInput.text.clear()
            }
        }
    }

}

