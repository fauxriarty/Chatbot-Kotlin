package com.example.characterbot

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import com.example.characterbot.databinding.ActivityUserChatBinding


class UserChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUserChatBinding

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

    }


}


