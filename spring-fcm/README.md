# Spring Boot FCM Server Implementation Guide

## Table of Contents
1. [Project Setup](#project-setup)
2. [Dependencies and Configuration](#dependencies-and-configuration)
3. [Firebase Admin SDK Setup](#firebase-admin-sdk-setup)
4. [DTOs Implementation](#dtos-implementation)
5. [FCM Service Implementation](#fcm-service-implementation)
6. [Controller Implementation](#controller-implementation)
7. [Exception Handling](#exception-handling)
8. [Testing the Implementation](#testing)

## Project Setup

### 1. Create Spring Boot Project
Start with Spring Initializr or create manually with the following structure:

```
spring-fcm/
├── src/main/
│   ├── java/com/newton/springfcm/
│   │   ├── SpringFcmApplication.java
│   │   ├── FCMInitializer.java
│   │   ├── controller/
│   │   │   └── TestController.java
│   │   ├── dto/
│   │   │   ├── NotificationRequest.java
│   │   │   └── NotificationResponse.java
│   │   ├── exception/
│   │   │   ├── CustomizedResponseEntityExceptionHandler.java
│   │   │   └── ErrorDetails.java
│   │   └── service/
│   │       └── FCMService.java
│   └── resources/
│       ├── application.properties
│       └── client.json (Firebase service account key)
├── pom.xml
└── mvnw, mvnw.cmd (Maven wrapper)
```

### 2. Project Specifications
```xml
<!-- Java Version: 17 -->
<!-- Spring Boot Version: 3.5.3 -->
<!-- Packaging: JAR -->
<!-- Port: 8081 -->
```

## Dependencies and Configuration

### 1. pom.xml
```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" 
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
         https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.5.3</version>
        <relativePath/>
    </parent>
    
    <groupId>com.newton</groupId>
    <artifactId>spring-fcm</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <name>spring-fcm</name>
    <description>Spring Boot FCM Server Implementation</description>
    
    <properties>
        <java.version>17</java.version>
    </properties>
    
    <dependencies>
        <!-- Spring Boot Web Starter -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>

        <!-- Lombok for reducing boilerplate code -->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
        
        <!-- Firebase Admin SDK -->
        <dependency>
            <groupId>com.google.firebase</groupId>
            <artifactId>firebase-admin</artifactId>
            <version>9.5.0</version>
        </dependency>
        
        <!-- Spring Boot Test Starter -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <!-- Maven Compiler Plugin with Lombok -->
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <configuration>
                    <annotationProcessorPaths>
                        <path>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                        </path>
                    </annotationProcessorPaths>
                </configuration>
            </plugin>
            
            <!-- Spring Boot Maven Plugin -->
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <configuration>
                    <excludes>
                        <exclude>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                        </exclude>
                    </excludes>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

### 2. application.properties
```properties
# Application Configuration
spring.application.name=spring-fcm
server.port=8081

# Firebase Configuration
app.firebase-configuration-file=client.json

# Optional: Logging Configuration
logging.level.com.newton.springfcm=DEBUG
logging.level.com.google.firebase=INFO
```

## Firebase Admin SDK Setup

### 1. Generate Service Account Key
1. Go to Firebase Console → Project Settings → Service Accounts
2. Click "Generate new private key"
3. Download the JSON file and rename it to `client.json`
4. Place it in `src/main/resources/` directory

### 2. FCMInitializer.java
```java
package com.newton.springfcm;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;

/**
 * Initializes Firebase Admin SDK on application startup
 */
@Service
@Slf4j
public class FCMInitializer {

    @Value("${app.firebase-configuration-file}")
    private String firebaseConfigPath;

    /**
     * Initializes Firebase App with service account credentials
     * This method runs after dependency injection is complete
     */
    @PostConstruct
    public void initialize() {
        try {
            // Build Firebase options with credentials from service account key
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(
                            new ClassPathResource(firebaseConfigPath).getInputStream()))
                    .build();

            // Initialize Firebase App if not already initialized
            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
                log.info("Firebase application initialized successfully");
            } else {
                log.info("Firebase application already initialized");
            }
        } catch (IOException e) {
            log.error("Failed to initialize Firebase: {}", e.getMessage());
            throw new RuntimeException("Firebase initialization failed", e);
        }
    }
}
```

### 3. SpringFcmApplication.java
```java
package com.newton.springfcm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main Spring Boot Application Class
 */
@SpringBootApplication
public class SpringFcmApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringFcmApplication.class, args);
    }
}
```

## DTOs Implementation

### 1. NotificationRequest.java
```java
package com.newton.springfcm.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * DTO for incoming notification requests
 */
@Data
@AllArgsConstructor
public class NotificationRequest {
    
    /**
     * Title of the notification
     */
    private String title;
    
    /**
     * Body text of the notification
     */
    private String body;
    
    /**
     * Topic for categorizing notifications
     */
    private String topic;
    
    /**
     * FCM token of the target device
     */
    private String token;
    
    /**
     * Optional image URL for rich notifications
     */
    private String imageUrl;
}
```

### 2. NotificationResponse.java
```java
package com.newton.springfcm.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * DTO for notification response
 */
@Data
@AllArgsConstructor
public class NotificationResponse {
    
    /**
     * HTTP status code
     */
    private int status;
    
    /**
     * Response message
     */
    private String message;
}
```

## FCM Service Implementation

### FCMService.java
```java
package com.newton.springfcm.service;

import com.google.firebase.messaging.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.newton.springfcm.dto.NotificationRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * Service class for Firebase Cloud Messaging operations
 */
@Service
@Slf4j
public class FCMService {

    /**
     * Sends a message to a specific FCM token
     * @param request NotificationRequest containing message details
     * @throws InterruptedException if the operation is interrupted
     * @throws ExecutionException if the operation fails
     */
    public void sendMessageToToken(NotificationRequest request)
            throws InterruptedException, ExecutionException {
        
        // Build the FCM message
        Message message = getPreconfiguredMessageToken(request);
        
        // Pretty print the message for debugging
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String jsonOutput = gson.toJson(message);
        
        // Send the message and get response
        String response = sendAndGetResponse(message);
        
        log.info("Sent message to token. Device token: {}, Response: {}, Message: {}", 
                request.getToken(), response, jsonOutput);
    }

    /**
     * Configures Android-specific notification settings
     * @param topic notification topic for categorization
     * @return AndroidConfig with optimized settings
     */
    private AndroidConfig getAndroidConfig(String topic) {
        return AndroidConfig.builder()
                .setTtl(Duration.ofMinutes(2).toMillis()) // Time to live: 2 minutes
                .setCollapseKey(topic) // Collapse multiple notifications with same key
                .setPriority(AndroidConfig.Priority.HIGH) // High priority for immediate delivery
                .setNotification(AndroidNotification.builder()
                        .setTag(topic) // Notification tag for grouping
                        .setSound("default") // Use default notification sound
                        .setPriority(AndroidNotification.Priority.HIGH) // High priority
                        .setVisibility(AndroidNotification.Visibility.PUBLIC) // Visible on lock screen
                        .setChannelId("fcm_default_channel") // Match Android app channel ID
                        .build())
                .build();
    }

    /**
     * Sends message using Firebase Admin SDK and returns response
     * @param message FCM Message to send
     * @return String response from Firebase
     * @throws InterruptedException if operation is interrupted
     * @throws ExecutionException if operation fails
     */
    private String sendAndGetResponse(Message message) 
            throws InterruptedException, ExecutionException {
        return FirebaseMessaging.getInstance().sendAsync(message).get();
    }

    /**
     * Configures iOS-specific notification settings (APNs)
     * @param topic notification topic for categorization
     * @return ApnsConfig with iOS-specific settings
     */
    private ApnsConfig getApnsConfig(String topic) {
        return ApnsConfig.builder()
                .setAps(Aps.builder()
                        .setCategory(topic) // iOS notification category
                        .setThreadId(topic) // Thread ID for grouping
                        .setSound("default") // Use default iOS sound
                        .build())
                .build();
    }

    /**
     * Creates a preconfigured message for a specific token
     * @param request NotificationRequest with message details
     * @return Message configured for the target token
     */
    private Message getPreconfiguredMessageToken(NotificationRequest request) {
        return getPreconfiguredMessageBuilder(request)
                .setToken(request.getToken()) // Set target device token
                .build();
    }

    /**
     * Builds a message with all platform-specific configurations
     * @param request NotificationRequest with message details
     * @return Message.Builder with all configurations applied
     */
    private Message.Builder getPreconfiguredMessageBuilder(NotificationRequest request) {
        // Get platform-specific configurations
        AndroidConfig androidConfig = getAndroidConfig(request.getTopic());
        ApnsConfig apnsConfig = getApnsConfig(request.getTopic());

        // Build notification payload
        Notification.Builder notificationBuilder = Notification.builder()
                .setTitle(request.getTitle())
                .setBody(request.getBody());

        // Add image if provided
        if (request.getImageUrl() != null && !request.getImageUrl().trim().isEmpty()) {
            notificationBuilder.setImage(request.getImageUrl());
        }

        Notification notification = notificationBuilder.build();

        // Build data payload (always sent, even with notification payload)
        Map<String, String> data = new HashMap<>();
        data.put("title", request.getTitle());
        data.put("body", request.getBody());
        
        // Add optional fields to data payload
        if (request.getImageUrl() != null && !request.getImageUrl().trim().isEmpty()) {
            data.put("image", request.getImageUrl());
        }
        if (request.getTopic() != null) {
            data.put("topic", request.getTopic());
        }

        // Build and return the complete message
        return Message.builder()
                .setApnsConfig(apnsConfig) // iOS configuration
                .setAndroidConfig(androidConfig) // Android configuration
                .setNotification(notification) // Notification payload
                .putAllData(data); // Data payload
    }
}
```

## Controller Implementation

### TestController.java
```java
package com.newton.springfcm.controller;

import com.newton.springfcm.dto.NotificationRequest;
import com.newton.springfcm.dto.NotificationResponse;
import com.newton.springfcm.service.FCMService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.ExecutionException;

/**
 * REST Controller for testing FCM functionality
 */
@RestController
@RequestMapping("/test")
public class TestController {

    @Autowired
    private FCMService fcmService;

    /**
     * Sends a simple notification without image
     * 
     * Example: POST http://localhost:8081/test/simple-notification?token=YOUR_FCM_TOKEN
     * 
     * @param token FCM token of the target device
     * @return ResponseEntity with success/error message
     * @throws ExecutionException if FCM operation fails
     * @throws InterruptedException if operation is interrupted
     */
    @PostMapping("/simple-notification")
    public ResponseEntity<?> sendSimpleNotification(@RequestParam String token) 
            throws ExecutionException, InterruptedException {
        
        NotificationRequest request = new NotificationRequest(
                "Test Notification",
                "This is a simple test notification with sound and vibration!",
                "test",
                token,
                null // No image
        );

        fcmService.sendMessageToToken(request);
        
        return new ResponseEntity<>(
                new NotificationResponse(HttpStatus.OK.value(), "Simple notification sent."), 
                HttpStatus.OK
        );
    }

    /**
     * Sends a notification with an image
     * 
     * Example: POST http://localhost:8081/test/image-notification?token=YOUR_FCM_TOKEN
     * 
     * @param token FCM token of the target device
     * @return ResponseEntity with success/error message
     * @throws ExecutionException if FCM operation fails
     * @throws InterruptedException if operation is interrupted
     */
    @PostMapping("/image-notification")
    public ResponseEntity<?> sendImageNotification(@RequestParam String token) 
            throws ExecutionException, InterruptedException {
        
        NotificationRequest request = new NotificationRequest(
                "Image Notification",
                "Check out this awesome image!",
                "image_test",
                token,
                "https://picsum.photos/400/300" // Random image from Lorem Picsum
        );

        fcmService.sendMessageToToken(request);
        
        return new ResponseEntity<>(
                new NotificationResponse(HttpStatus.OK.value(), "Image notification sent."), 
                HttpStatus.OK
        );
    }

    /**
     * Sends an e-commerce style notification with product image
     * 
     * Example: POST http://localhost:8081/test/ecommerce-notification?token=YOUR_FCM_TOKEN
     * 
     * @param token FCM token of the target device
     * @return ResponseEntity with success/error message
     * @throws ExecutionException if FCM operation fails
     * @throws InterruptedException if operation is interrupted
     */
    @PostMapping("/ecommerce-notification")
    public ResponseEntity<?> sendEcommerceNotification(@RequestParam String token) 
            throws ExecutionException, InterruptedException {
        
        NotificationRequest request = new NotificationRequest(
                "🛒 New Deal Available!",
                "50% OFF on Electronics - Limited Time Offer!",
                "ecommerce",
                token,
                "https://images.unsplash.com/photo-1441986300917-64674bd600d8?w=400&h=300&fit=crop"
        );

        fcmService.sendMessageToToken(request);
        
        return new ResponseEntity<>(
                new NotificationResponse(HttpStatus.OK.value(), "E-commerce notification sent."), 
                HttpStatus.OK
        );
    }

    /**
     * Sends a custom notification with request body
     * 
     * Example: POST http://localhost:8081/test/custom-notification
     * Content-Type: application/json
     * {
     *   "title": "Custom Title",
     *   "body": "Custom message body",
     *   "topic": "custom",
     *   "token": "YOUR_FCM_TOKEN",
     *   "imageUrl": "https://example.com/image.jpg"
     * }
     * 
     * @param request NotificationRequest with custom details
     * @return ResponseEntity with success/error message
     * @throws ExecutionException if FCM operation fails
     * @throws InterruptedException if operation is interrupted
     */
    @PostMapping("/custom-notification")
    public ResponseEntity<?> sendCustomNotification(@RequestBody NotificationRequest request) 
            throws ExecutionException, InterruptedException {
        
        fcmService.sendMessageToToken(request);
        
        return new ResponseEntity<>(
                new NotificationResponse(HttpStatus.OK.value(), "Custom notification sent."), 
                HttpStatus.OK
        );
    }
}
```

## Exception Handling

### 1. ErrorDetails.java
```java
package com.newton.springfcm.exception;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * DTO for error response details
 */
@Data
@AllArgsConstructor
public class ErrorDetails {
    
    /**
     * Timestamp when error occurred
     */
    LocalDateTime timestamp;
    
    /**
     * Error message
     */
    String message;
    
    /**
     * Additional error details
     */
    String details;
}
```

### 2. CustomizedResponseEntityExceptionHandler.java
```java
package com.newton.springfcm.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.LocalDateTime;

/**
 * Global exception handler for the application
 */
@ControllerAdvice
public class CustomizedResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {

    /**
     * Handles all unhandled exceptions
     * @param ex Exception that occurred
     * @param request Web request details
     * @return ResponseEntity with error details
     */
    @ExceptionHandler(Exception.class)
    public final ResponseEntity<ErrorDetails> handleAllException(Exception ex, WebRequest request) {
        
        String message = ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage();
        
        ErrorDetails errorDetails = new ErrorDetails(
                LocalDateTime.now(),
                message,
                ex.getLocalizedMessage()
        );
        
        return new ResponseEntity<>(errorDetails, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Handles Firebase-specific exceptions
     * @param ex Firebase exception
     * @param request Web request details
     * @return ResponseEntity with error details
     */
    @ExceptionHandler(com.google.firebase.messaging.FirebaseMessagingException.class)
    public final ResponseEntity<ErrorDetails> handleFirebaseException(
            com.google.firebase.messaging.FirebaseMessagingException ex, WebRequest request) {
        
        ErrorDetails errorDetails = new ErrorDetails(
                LocalDateTime.now(),
                "Firebase messaging error: " + ex.getMessage(),
                ex.getErrorCode()
        );
        
        return new ResponseEntity<>(errorDetails, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles illegal argument exceptions
     * @param ex IllegalArgumentException
     * @param request Web request details
     * @return ResponseEntity with error details
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public final ResponseEntity<ErrorDetails> handleIllegalArgumentException(
            IllegalArgumentException ex, WebRequest request) {
        
        ErrorDetails errorDetails = new ErrorDetails(
                LocalDateTime.now(),
                "Invalid request: " + ex.getMessage(),
                request.getDescription(false)
        );
        
        return new ResponseEntity<>(errorDetails, HttpStatus.BAD_REQUEST);
    }
}
```

## Testing the Implementation

### 1. Running the Application
```bash
# Using Maven Wrapper
./mvnw spring-boot:run

# Or using Maven directly
mvn spring-boot:run

# Application will start on http://localhost:8081
```

### 2. Testing with cURL

#### Simple Notification
```bash
curl -X POST "http://localhost:8081/test/simple-notification?token=YOUR_FCM_TOKEN" \
     -H "Content-Type: application/json"
```

#### Image Notification
```bash
curl -X POST "http://localhost:8081/test/image-notification?token=YOUR_FCM_TOKEN" \
     -H "Content-Type: application/json"
```

#### E-commerce Notification
```bash
curl -X POST "http://localhost:8081/test/ecommerce-notification?token=YOUR_FCM_TOKEN" \
     -H "Content-Type: application/json"
```

#### Custom Notification
```bash
curl -X POST "http://localhost:8081/test/custom-notification" \
     -H "Content-Type: application/json" \
     -d '{
       "title": "Custom Notification",
       "body": "This is a custom message with image",
       "topic": "custom",
       "token": "YOUR_FCM_TOKEN",
       "imageUrl": "https://picsum.photos/400/300"
     }'
```

### 3. Testing with Postman

#### Collection Setup
1. Create new collection "FCM Testing"
2. Set base URL variable: `{{base_url}}` = `http://localhost:8081`
3. Set token variable: `{{fcm_token}}` = `YOUR_ACTUAL_FCM_TOKEN`

#### Request Examples
```json
// POST {{base_url}}/test/custom-notification
{
    "title": "Breaking News! 📰",
    "body": "Important update available. Tap to read more.",
    "topic": "news",
    "token": "{{fcm_token}}",
    "imageUrl": "https://images.unsplash.com/photo-1504711434969-e33886168f5c?w=400&h=300&fit=crop"
}
```

### 4. Verifying Success
Check the application logs for:
```
INFO  c.n.s.service.FCMService - Sent message to token. Device token: exxxxx, Response: projects/your-project/messages/xxxxxx
```


## Key Learning Points

1. **Firebase Admin SDK**: Server-side SDK for administrative operations
2. **Service Account Key**: Required for authentication with Firebase
3. **Message Structure**: Notification payload vs data payload differences
4. **Platform-specific Config**: Android and iOS have different requirements
5. **Asynchronous Operations**: FCM operations are non-blocking
6. **Error Handling**: Proper exception handling for production use
7. **Token Management**: FCM tokens can change and become invalid
