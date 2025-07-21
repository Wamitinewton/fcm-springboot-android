# Android FCM Implementation Guide

## Table of Contents
1. [Project Setup](#project-setup)
2. [Firebase Configuration](#firebase-configuration)
3. [Dependencies](#dependencies)
4. [Permissions](#permissions)
5. [FCM Service Implementation](#fcm-service-implementation)
6. [Main Activity Implementation](#main-activity-implementation)
7. [UI Implementation with Jetpack Compose](#ui-implementation)
8. [Testing the Implementation](#testing)

## Project Setup

### 1. Create New Android Project
Start by creating a new Android project with the following specifications:

```kotlin
// Minimum SDK: API 24 (Android 7.0)
// Target SDK: API 36
// Language: Kotlin
// Build System: Gradle (Kotlin DSL)
```

### 2. Project Structure
```
client/
├── app/
│   ├── src/main/
│   │   ├── java/com/newton/fcm_client/
│   │   │   ├── MainActivity.kt
│   │   │   ├── MyFirebaseMessagingService.kt
│   │   │   └── ui/theme/
│   │   ├── res/
│   │   └── AndroidManifest.xml
│   ├── build.gradle.kts
│   └── google-services.json
├── build.gradle.kts
└── settings.gradle.kts
```

## Firebase Configuration

### 1. Create Firebase Project
1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Create a new project
3. Add Android app to your project
4. Download `google-services.json` and place it in `app/` directory

### 2. google-services.json Structure
```json
{
  "project_info": {
    "project_number": "YOUR_PROJECT_NUMBER",
    "project_id": "YOUR_PROJECT_ID",
    "storage_bucket": "YOUR_PROJECT_ID.firebasestorage.app"
  },
  "client": [
    {
      "client_info": {
        "mobilesdk_app_id": "YOUR_APP_ID",
        "android_client_info": {
          "package_name": "com.newton.fcm_client"
        }
      },
      "api_key": [
        {
          "current_key": "YOUR_API_KEY"
        }
      ]
    }
  ]
}
```

## Dependencies

### 1. Project-level build.gradle.kts
```kotlin
// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    id("com.google.gms.google-services") version "4.4.3" apply false
}
```

### 2. Module-level build.gradle.kts
```kotlin
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.newton.fcm_client"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.newton.fcm_client"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    
    kotlinOptions {
        jvmTarget = "11"
    }
    
    buildFeatures {
        compose = true
    }
}

dependencies {
    // Core Android dependencies
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    
    // Compose BOM and UI
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    
    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation("com.google.firebase:firebase-messaging")
    
    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
```

### 3. gradle/libs.versions.toml
```toml
[versions]
agp = "8.11.1"
firebaseBom = "33.16.0"
kotlin = "2.0.21"
coreKtx = "1.16.0"
junit = "4.13.2"
junitVersion = "1.2.1"
espressoCore = "3.6.1"
lifecycleRuntimeKtx = "2.9.1"
activityCompose = "1.10.1"
composeBom = "2024.09.00"

[libraries]
androidx-core-ktx = { group = "androidx.core", name = "core-ktx", version.ref = "coreKtx" }
firebase-bom = { module = "com.google.firebase:firebase-bom", version.ref = "firebaseBom" }
junit = { group = "junit", name = "junit", version.ref = "junit" }
androidx-junit = { group = "androidx.test.ext", name = "junit", version.ref = "junitVersion" }
androidx-espresso-core = { group = "androidx.test.espresso", name = "espresso-core", version.ref = "espressoCore" }
androidx-lifecycle-runtime-ktx = { group = "androidx.lifecycle", name = "lifecycle-runtime-ktx", version.ref = "lifecycleRuntimeKtx" }
androidx-activity-compose = { group = "androidx.activity", name = "activity-compose", version.ref = "activityCompose" }
androidx-compose-bom = { group = "androidx.compose", name = "compose-bom", version.ref = "composeBom" }
androidx-ui = { group = "androidx.compose.ui", name = "ui" }
androidx-ui-graphics = { group = "androidx.compose.ui", name = "ui-graphics" }
androidx-ui-tooling = { group = "androidx.compose.ui", name = "ui-tooling" }
androidx-ui-tooling-preview = { group = "androidx.compose.ui", name = "ui-tooling-preview" }
androidx-ui-test-manifest = { group = "androidx.compose.ui", name = "ui-test-manifest" }
androidx-ui-test-junit4 = { group = "androidx.compose.ui", name = "ui-test-junit4" }
androidx-material3 = { group = "androidx.compose.material3", name = "material3" }

[plugins]
android-application = { id = "com.android.application", version.ref = "agp" }
kotlin-android = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }
kotlin-compose = { id = "org.jetbrains.kotlin.plugin.compose", version.ref = "kotlin" }
```

## Permissions

### AndroidManifest.xml
```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools">

    <!-- Essential permissions for FCM -->
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
    <uses-permission android:name="android.permission.VIBRATE" />

    <application
        android:allowBackup="true"
        android:dataExtractionRules="@xml/data_extraction_rules"
        android:fullBackupContent="@xml/backup_rules"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:theme="@style/Theme.Fcmclient">
        
        <!-- Main Activity -->
        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:label="@string/app_name"
            android:theme="@style/Theme.Fcmclient">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>

        <!-- FCM Service -->
        <service
            android:name=".MyFirebaseMessagingService"
            android:exported="false">
            <intent-filter>
                <action android:name="com.google.firebase.MESSAGING_EVENT" />
            </intent-filter>
        </service>
    </application>
</manifest>
```

## FCM Service Implementation

### MyFirebaseMessagingService.kt
```kotlin
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

    /**
     * Called when message is received.
     * This handles both notification messages and data messages.
     */
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        Log.d(TAG, "From: ${message.from}")
        Log.d(TAG, "Message data: ${message.data}")

        // Handle notification payload
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

        // Handle data payload
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

    /**
     * Called when a new FCM token is generated.
     * This happens on app startup and when tokens are refreshed.
     */
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "Refreshed token: $token")
        
        // Send token to your server here if needed
        // sendTokenToServer(token)
    }

    /**
     * Creates notification channel for Android O and above
     */
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

    /**
     * Loads image from URL and shows notification with image
     */
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

    /**
     * Downloads image from URL and returns bitmap
     */
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

    /**
     * Shows notification with optional image
     */
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

        // Add image if available
        bitmap?.let {
            notificationBuilder.setLargeIcon(it)
                .setStyle(
                    NotificationCompat.BigPictureStyle()
                        .bigPicture(it)
                        .bigLargeIcon(null as Bitmap?)
                )
        }

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Trigger vibration
        triggerVibration()

        // Show notification with unique ID
        notificationManager.notify(System.currentTimeMillis().toInt(), notificationBuilder.build())

        Log.d(TAG, "Notification displayed: $title - $body ${if (bitmap != null) "with image" else "without image"}")
    }

    /**
     * Triggers device vibration
     */
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
```

## Main Activity Implementation

### MainActivity.kt
```kotlin
package com.newton.fcm_client

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.FirebaseMessaging
import com.newton.fcm_client.ui.theme.FcmclientTheme
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class MainActivity : ComponentActivity() {
    private val TAG = "FCM_CLIENT"

    // Permission launcher for POST_NOTIFICATIONS
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Log.d(TAG, "Notification permission granted")
        } else {
            Log.w(TAG, "Notification permission denied")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Request notification permission for Android 13+
        requestNotificationPermission()

        setContent {
            FcmclientTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    FCMTokenScreen(
                        modifier = Modifier.padding(innerPadding),
                        onTokenFetch = { fetchFCMToken() }
                    )
                }
            }
        }
    }

    /**
     * Requests notification permission for Android 13 (API 33) and above
     */
    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    Log.d(TAG, "Notification permission already granted")
                }
                else -> {
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }
    }

    /**
     * Fetches FCM token from Firebase
     */
    private suspend fun fetchFCMToken(): String? {
        return try {
            val token = FirebaseMessaging.getInstance().token.await()
            Log.i(TAG, "FCM Token: $token")
            token
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fetch FCM token", e)
            null
        }
    }
}

/**
 * Utility function to copy text to clipboard
 */
private fun copyToClipboard(context: Context, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("FCM Token", text)
    clipboard.setPrimaryClip(clip)
}
```

## UI Implementation

### FCMTokenScreen Composable
```kotlin
@Composable
fun FCMTokenScreen(
    modifier: Modifier = Modifier,
    onTokenFetch: suspend () -> String?
) {
    var token by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Fetch token on first composition
    LaunchedEffect(Unit) {
        isLoading = true
        error = null
        try {
            token = onTokenFetch()
            if (token == null) {
                error = "Failed to fetch token"
            }
        } catch (e: Exception) {
            error = "Error: ${e.message}"
        } finally {
            isLoading = false
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "FCM Token",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )

        // Loading indicator
        if (isLoading) {
            CircularProgressIndicator()
            Text(
                text = "Fetching token...",
                style = MaterialTheme.typography.bodyMedium
            )
        }

        // Error display
        error?.let { errorMessage ->
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Text(
                    text = errorMessage,
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        // Token display
        token?.let { fcmToken ->
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Your FCM Token:",
                        style = MaterialTheme.typography.labelLarge
                    )

                    SelectionContainer {
                        Text(
                            text = fcmToken,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = FontFamily.Monospace
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // Copy button
            Button(
                onClick = {
                    copyToClipboard(context, fcmToken)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Copy Token")
            }
        }

        // Refresh button
        OutlinedButton(
            onClick = {
                coroutineScope.launch {
                    isLoading = true
                    error = null
                    try {
                        token = onTokenFetch()
                        if (token == null) {
                            error = "Failed to fetch token"
                        }
                    } catch (e: Exception) {
                        error = "Error: ${e.message}"
                    } finally {
                        isLoading = false
                    }
                }
            },
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Refresh Token")
        }
    }
}
```

## Testing the Implementation

### 1. Run the Application
1. Build and run the app on a physical device or emulator
2. Grant notification permissions when prompted
3. Copy the FCM token displayed in the app

### 2. Test with Firebase Console
1. Go to Firebase Console → Cloud Messaging
2. Create a new campaign
3. Paste your FCM token
4. Send test notification

### 3. Test Different Notification Types
```kotlin
// Test simple notification (no image)
// Use the copied token in your server implementation

// Test notification with image
// Include imageUrl in your server request

// Test data-only messages
// Send only data payload without notification payload
```

### 4. Debugging Tips
```kotlin
// Check logs for these tags:
// - "FCM_CLIENT" (MainActivity logs)
// - "FCMService" (Service logs)

// Common issues:
// 1. Missing google-services.json
// 2. Wrong package name in Firebase configuration
// 3. Missing notification permissions on Android 13+
// 4. Network connectivity issues
```

## Key Learning Points

1. **FCM Token**: Unique identifier for each app installation
2. **Notification vs Data Messages**: Different handling approaches
3. **Permissions**: Critical for Android 13+ devices
4. **Image Handling**: Requires coroutines for network operations
5. **Notification Channels**: Required for Android O+
6. **Service Lifecycle**: Automatic handling by Firebase SDK

## Next Steps
- Integrate with your Spring Boot server
- Implement topic subscriptions
- Add notification action buttons
- Handle deep linking from notifications
- Implement notification analytics