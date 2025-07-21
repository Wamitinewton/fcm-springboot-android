package com.newton.fcm_client

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.firebase.messaging.FirebaseMessaging
import com.newton.fcm_client.ui.theme.FcmclientTheme
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class MainActivity : ComponentActivity() {
    private val TAG = "FCM_CLIENT"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
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

        if (isLoading) {
            CircularProgressIndicator()
            Text(
                text = "Fetching token...",
                style = MaterialTheme.typography.bodyMedium
            )
        }

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

private fun copyToClipboard(context: Context, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("FCM Token", text)
    clipboard.setPrimaryClip(clip)
}

@Preview(showBackground = true)
@Composable
fun FCMTokenScreenPreview() {
    FcmclientTheme {
        FCMTokenScreen(
            onTokenFetch = {
                "fakeTokenForPreviewPurposes123456789abcdefghijklmnopqrstuvwxyz"
            }
        )
    }
}