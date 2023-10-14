package com.example.characterbot
import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage



class FirebaseService: FirebaseMessagingService() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        // Extracting the notification title and body.
        val title = message.notification?.title
        val body = message.notification?.body

        showNotification(title, body)
    }

    private fun createNotificationChannel() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val name = "Your Notification Name"
            val descriptionText = "Your Notification Description"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("your_channel_id", name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun showNotification(title: String?, body: String?) {
        // Create a simple notification with title and body
        val builder = NotificationCompat.Builder(this, "your_channel_id")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        val notificationManager = NotificationManagerCompat.from(this)
        notificationManager.notify(1001, builder.build())
    }


    private fun updateTokenInFirestore(token: String) {
        val userName = getLoggedInUserName()
        if (userName != null) {
            FirebaseFirestore.getInstance().collection("users")
                .whereEqualTo("name", userName)
                .get()
                .addOnSuccessListener { documents ->
                    if (!documents.isEmpty) {
                        val userId = documents.documents[0].id
                        FirebaseFirestore.getInstance().collection("users").document(userId)
                            .update("fcmToken", token)
                    }
                }
        }
    }

    private fun getLoggedInUserName(): String? {
        val sharedPreferences = getSharedPreferences("characterbot_prefs", MODE_PRIVATE)
        return sharedPreferences.getString("user_name", null)
    }

    override fun onNewToken(newToken: String) {
        super.onNewToken(newToken)
        updateTokenInFirestore(newToken)
    }
}
