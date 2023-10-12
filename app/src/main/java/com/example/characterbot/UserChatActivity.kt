package com.example.characterbot

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import android.text.InputType
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.core.view.GravityCompat
import com.example.characterbot.databinding.ActivityUserChatBinding
import com.google.firebase.firestore.FirebaseFirestore

class UserChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUserChatBinding
    private lateinit var adapter: ArrayAdapter<String>
    private val usersList = mutableListOf<String>()
    private val firestore = FirebaseFirestore.getInstance()
    private val usersCollection = firestore.collection("users")
    private var currentUserName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityUserChatBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        binding.toolbarLayout.toolbarTitle.text = getString(R.string.chat_with_users)

        setSupportActionBar(binding.toolbarLayout.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(false)
        supportActionBar?.setDisplayShowHomeEnabled(false)

        val drawerLayout = binding.userDrawerLayout
        val toggle = ActionBarDrawerToggle(
            this,
            drawerLayout,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        toggle.isDrawerIndicatorEnabled = false

        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, usersList)
        binding.usersListView.adapter = adapter

        binding.navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.chatWithBot -> {
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.chatWithUsers -> {
                    drawerLayout.closeDrawer(GravityCompat.START)
                    true  // it's the current page, so no action is needed
                }
                else -> false
            }
        }

        binding.toolbarLayout.toolbarIcon.setOnClickListener {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START)
            } else {
                drawerLayout.openDrawer(GravityCompat.START)
            }
        }

        binding.usersListView.setOnItemClickListener { _, _, position, _ ->
            val selectedUser = usersList[position]
            val intent = Intent(this, ChatActivity::class.java)
            intent.putExtra("userName", selectedUser)
            startActivity(intent)
        }

        binding.addUserFab.setOnClickListener {
            showAddUserDialog()
        }

        promptForUsername()
    }

    private fun fetchUsers() {
        usersCollection.get().addOnSuccessListener { querySnapshot ->
            for (document in querySnapshot) {
                val user = document.getString("name")
                if (user != null && user != currentUserName) usersList.add(user)
            }
            adapter.notifyDataSetChanged()
        }
    }

    private fun promptForUsername() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Enter your Username")

        val input = EditText(this)
        input.inputType = InputType.TYPE_CLASS_TEXT
        builder.setView(input)

        builder.setPositiveButton("OK") { _, _ ->
            val userName = input.text.toString()
            if (userName.isNotBlank()) {
                usersCollection.whereEqualTo("name", userName).get()
                    .addOnSuccessListener { querySnapshot ->
                        if (querySnapshot.isEmpty) {
                            // username does not exist in Firestore
                            Toast.makeText(this, "Username not found", Toast.LENGTH_SHORT).show()
                            promptForUsername() // Prompt again
                        } else {
                            // username exists so proceed and fetch users except it
                            currentUserName = userName
                            fetchUsers()
                        }
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Error checking username", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(this, "Username cannot be blank", Toast.LENGTH_SHORT).show()
                promptForUsername() // Prompt again
            }
        }
        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.cancel()
            finish() // cl activity if user cancels
        }

        builder.show()
    }

    private fun showAddUserDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Add User")

        val input = EditText(this)
        input.inputType = InputType.TYPE_CLASS_TEXT
        builder.setView(input)

        builder.setPositiveButton("OK") { _, _ ->
            val userName = input.text.toString()
            if (userName.isNotBlank()) {
                // checkin if user already exists before adding
                usersCollection.whereEqualTo("name", userName).get().addOnSuccessListener { querySnapshot ->
                    if (querySnapshot.isEmpty) {
                        usersList.add(userName)
                        adapter.notifyDataSetChanged()  // Update the ListView

                        // store user to Firestore
                        val user = hashMapOf("name" to userName)
                        usersCollection.add(user)

                        Toast.makeText(this, "User Added: $userName", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Username already exists", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(this, "Username cannot be blank", Toast.LENGTH_SHORT).show()
            }
        }
        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }

        builder.show()
    }
}
