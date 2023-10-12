package com.example.characterbot

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast

class ChatActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        val tvUserName = findViewById<TextView>(R.id.tvUserName)

        val userName = intent.getStringExtra("userName")
        if (userName != null) {
            supportActionBar?.title = userName
            tvUserName.text = userName
        } else {
            Toast.makeText(this, "User not found!", Toast.LENGTH_SHORT).show()
            finish()
        }

        val backButton: ImageButton = findViewById(R.id.backButton)
        backButton.setOnClickListener {
            finish()
        }
    }
}
