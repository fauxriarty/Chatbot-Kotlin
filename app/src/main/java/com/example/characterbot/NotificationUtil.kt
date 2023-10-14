package com.example.characterbot

import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

@Suppress("NAME_SHADOWING")
object NotificationUtil {
    private const val FCM_API = "https://fcm.googleapis.com/fcm/send"
    private const val serverKey = "AAAATO5BBMo:APA91bGnKMlHg25C3k_ws0OxJh2YO49Uu82Xoz_AtcGX37iDor7AGhsUgD-1PYW-aPgEPdALbzzCsChJ_VEls311pmLGNT9vGFFw9ykvMVIZrFfa2PwJSq36QlW7fKqSWUbCA_QSwNPe"
    private const val contentType = "application/json"

    // OkHttpClient to send the POST request
    private val client = OkHttpClient()

    fun sendNotification(targetToken: String, title: String, body: String) {
        val notification = JSONObject()
        val notifBody = JSONObject()

        try {
            notifBody.put("title", title)
            notifBody.put("body", body)

            notification.put("to", targetToken)
            notification.put("data", notifBody)

            val body = notification.toString().toRequestBody(contentType.toMediaTypeOrNull())
            val request = Request.Builder()
                .url(FCM_API)
                .post(body)
                .addHeader("Authorization", "key=$serverKey")
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    e.printStackTrace()
                }

                override fun onResponse(call: Call, response: Response) {
                    if (response.isSuccessful) {
                        println("Notification sent successfully")
                    } else {
                        println("Failed to send the notification")
                    }
                }
            })

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
