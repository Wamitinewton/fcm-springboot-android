package com.newton.fcm_client

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.RingtoneManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

class MyFirebaseMessagingService: FirebaseMessagingService() {

    private val TAG = "FCMService"
    private val CHANNEL_ID = "fcm_default_channel"

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        Log.d(TAG, "From: ${message.from}")
        Log.d(TAG, "Message data: ${message.data}")

        message.notification?.let {
            Log.d(TAG, "Message Notification Body: ${it.body}")
            Log.d(TAG, "Message Notification Title: ${it.title}")
            Log.d(TAG, "Message Notification Image: ${it.imageUrl}")

            val imageUrl = message.data["image"] ?: it.imageUrl?.toString()

            if (imageUrl != null) {
                loadImageAndShowNotification(
                    title = it.title ?: "FCM Notification",
                    body = it.body ?: "",
                    imageUrl = imageUrl
                )
            } else {
                showNotification(
                    title = it.title ?: "FCM Notification",
                    body = it.body ?: "",
                    bitmap = null
                )
            }
        }

        if (message.data.isNotEmpty()) {
            val title = message.data["title"] ?: "Data Message"
            val body = message.data["body"] ?: "You have a new message"
            val imageUrl = message.data["image"]

            if (imageUrl != null) {
                loadImageAndShowNotification(title, body, imageUrl)
            } else {
                showNotification(title, body, null)
            }
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "Refreshed token: $token")
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "FCM Default Channel"
            val descriptionText = "Default channel for FCM notifications"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 1000, 500, 1000)
                setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION), null)
            }

            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)

            Log.d(TAG, "Notification channel created with sound and vibration")
        }
    }

    private fun loadImageAndShowNotification(title: String, body: String, imageUrl: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val bitmap = downloadImage(imageUrl)
                withContext(Dispatchers.Main) {
                    showNotification(title, body, bitmap)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load image: $imageUrl", e)
                withContext(Dispatchers.Main) {
                    showNotification(title, body, null)
                }
            }
        }
    }

    private suspend fun downloadImage(imageUrl: String): Bitmap? {
        return withContext(Dispatchers.IO) {
            try {
                val url = URL(imageUrl)
                val connection = url.openConnection() as HttpURLConnection
                connection.doInput = true
                connection.connect()
                val inputStream: InputStream = connection.inputStream
                BitmapFactory.decodeStream(inputStream)
            } catch (e: Exception) {
                Log.e(TAG, "Error downloading image", e)
                null
            }
        }
    }

    private fun showNotification(title: String, body: String, bitmap: Bitmap?) {
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }

        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)

        bitmap?.let {
            notificationBuilder.setLargeIcon(it)
                .setStyle(
                    NotificationCompat.BigPictureStyle()
                        .bigPicture(it)
                        .bigLargeIcon(null as Bitmap?)
                )
        }

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        triggerVibration()

        notificationManager.notify(System.currentTimeMillis().toInt(), notificationBuilder.build())

        Log.d(TAG, "Notification displayed: $title - $body ${if (bitmap != null) "with image" else "without image"}")
    }

    private fun triggerVibration() {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val vibrationEffect = VibrationEffect.createWaveform(
                longArrayOf(0, 500, 200, 500),
                -1
            )
            vibrator.vibrate(vibrationEffect)
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(longArrayOf(0, 500, 200, 500), -1)
        }
    }
}