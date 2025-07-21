# Firebase Cloud Messaging (FCM) Integration Project

A complete implementation of Firebase Cloud Messaging with Android client and Spring Boot server.

## Project Structure

```
fcm-integration/
├── client/                 # Android Application (Kotlin + Jetpack Compose)
├── spring-fcm/            # Spring Boot Server (Java)
├── docs/                  # Documentation
│   ├── android-guide.md   # Detailed Android implementation guide
│   └── spring-guide.md    # Detailed Spring Boot implementation guide
└── README.md             # This file
```

## Overview

This project demonstrates how to implement Firebase Cloud Messaging (FCM) push notifications with:

- **Android Client**: Receives and displays notifications with support for images, sound, and vibration
- **Spring Boot Server**: Sends notifications to Android devices via Firebase Admin SDK

## Features

### Android Client
- ✅ FCM token generation and display
- ✅ Notification reception with custom service
- ✅ Image notifications support
- ✅ Sound and vibration
- ✅ Modern UI with Jetpack Compose
- ✅ Notification permissions handling (Android 13+)

### Spring Boot Server
- ✅ Firebase Admin SDK integration
- ✅ REST API for sending notifications
- ✅ Support for simple and image notifications
- ✅ Platform-specific configurations (Android/iOS)
- ✅ Global exception handling
- ✅ Multiple notification types (simple, image, e-commerce)

## Quick Start

### Prerequisites
- Android Studio (latest version)
- Java 17+
- Firebase project with FCM enabled
- Maven 3.6+

### 1. Setup Firebase Project
1. Create a Firebase project at [Firebase Console](https://console.firebase.google.com/)
2. Add an Android app to your project
3. Download `google-services.json` for Android
4. Generate service account key for server

### 2. Run Android Client
```bash
cd client
# Open in Android Studio and run
# Or use command line:
./gradlew installDebug
```

### 3. Run Spring Boot Server
```bash
cd spring-fcm
# Place your service account key as src/main/resources/client.json
./mvnw spring-boot:run
# Server starts at http://localhost:8081
```

### 4. Test the Integration
1. Get FCM token from Android app
2. Send test notification:
```bash
curl -X POST "http://localhost:8081/test/simple-notification?token=YOUR_FCM_TOKEN"
```

## API Endpoints

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/test/simple-notification` | POST | Send simple notification |
| `/test/image-notification` | POST | Send notification with image |
| `/test/ecommerce-notification` | POST | Send e-commerce style notification |
| `/test/custom-notification` | POST | Send custom notification (JSON body) |

## Configuration

### Android Configuration
- Package name: `com.newton.fcm_client`
- Min SDK: 24 (Android 7.0)
- Target SDK: 36

### Server Configuration
- Port: 8081
- Firebase config: `src/main/resources/client.json`
- Spring Boot version: 3.5.3

## Documentation

For detailed implementation guides:
- 📱 [Android Implementation Guide](docs/android-guide.md)
- 🚀 [Spring Boot Implementation Guide](docs/spring-guide.md)

## Technology Stack

### Android
- Kotlin
- Jetpack Compose
- Firebase Messaging SDK
- Material Design 3

### Server
- Java 17
- Spring Boot 3.5.3
- Firebase Admin SDK
- Maven
- Lombok

## Support

This project is designed for educational purposes to demonstrate FCM integration patterns and best practices.

## License

MIT License - feel free to use this project for learning and teaching purposes.
