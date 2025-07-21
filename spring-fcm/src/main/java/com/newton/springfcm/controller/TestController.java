package com.newton.springfcm.controller;

import com.newton.springfcm.dto.NotificationRequest;
import com.newton.springfcm.dto.NotificationResponse;
import com.newton.springfcm.service.FCMService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/test")
public class TestController {

    @Autowired
    private FCMService fcmService;

    @PostMapping("/simple-notification")
    public ResponseEntity<?> sendSimpleNotification(@RequestParam String token) throws ExecutionException, InterruptedException {
        NotificationRequest request = new NotificationRequest(
                "Test Notification",
                "This is a simple test notification with sound and vibration!",
                "test",
                token,
                null
        );

        fcmService.sendMessageToToken(request);
        return new ResponseEntity<>(new NotificationResponse(HttpStatus.OK.value(), "Simple notification sent."), HttpStatus.OK);
    }

    @PostMapping("/image-notification")
    public ResponseEntity<?> sendImageNotification(@RequestParam String token) throws ExecutionException, InterruptedException {
        NotificationRequest request = new NotificationRequest(
                "Image Notification",
                "Check out this awesome image!",
                "image_test",
                token,
                "https://picsum.photos/400/300"
        );

        fcmService.sendMessageToToken(request);
        return new ResponseEntity<>(new NotificationResponse(HttpStatus.OK.value(), "Image notification sent."), HttpStatus.OK);
    }

    @PostMapping("/ecommerce-notification")
    public ResponseEntity<?> sendEcommerceNotification(@RequestParam String token) throws ExecutionException, InterruptedException {
        NotificationRequest request = new NotificationRequest(
                "🛒 New Deal Available!",
                "50% OFF on Electronics - Limited Time Offer!",
                "ecommerce",
                token,
                "https://images.unsplash.com/photo-1441986300917-64674bd600d8?w=400&h=300&fit=crop"
        );

        fcmService.sendMessageToToken(request);
        return new ResponseEntity<>(new NotificationResponse(HttpStatus.OK.value(), "E-commerce notification sent."), HttpStatus.OK);
    }
}