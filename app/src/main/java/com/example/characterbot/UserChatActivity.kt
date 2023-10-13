package com.example.characterbot

import android.annotation.SuppressLint
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

    @SuppressLint("SetTextI18n")
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
            val chatRoomId = getChatRoomId(currentUserName!!, selectedUser)
            val intent = Intent(this, ChatActivity::class.java)
            intent.putExtra("chatRoomId", chatRoomId)
            intent.putExtra("selectedUserName", selectedUser)
            intent.putExtra("currentUserName", currentUserName)
            startActivity(intent)
        }



        binding.addUserFab.setOnClickListener {
            showAddUserDialog()
        }

        currentUserName = getUserNameFromPreferences()
        binding.greetingTextView.text = " Have a great day, $currentUserName! "

        if (currentUserName == null) {
            promptForUsername()
        } else {
            fetchUsers()
        }
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

    @SuppressLint("SetTextI18n")
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
                            binding.greetingTextView.text = "  Have a great day, $currentUserName! " //spaced for padding
                            saveUserNameToPreferences(userName)
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


    //  method to generate consistent chatroom ID
    private fun getChatRoomId(userId1: String, userId2: String): String {
        return if (userId1 < userId2) "$userId1-$userId2" else "$userId2-$userId1"
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

    // to save the username to SharedPreferences, its some storage mech in android where the data remains in it even after closing the app
    // unless the app cache is cleared or app is uninstalled
    private fun saveUserNameToPreferences(userName: String) {
        val sharedPreferences = getSharedPreferences("characterbot_prefs", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("user_name", userName)
        editor.apply()
    }

    // to retrieve the username from SharedPreferences
    private fun getUserNameFromPreferences(): String? {
        val sharedPreferences = getSharedPreferences("characterbot_prefs", MODE_PRIVATE)
        return sharedPreferences.getString("user_name", null)
    }

}
