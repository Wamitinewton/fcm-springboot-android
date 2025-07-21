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

@Service
@Slf4j
public class FCMService {

    public void sendMessageToToken(NotificationRequest request)
            throws InterruptedException, ExecutionException {
        Message message = getPreconfiguredMessageToken(request);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String jsonOutput = gson.toJson(message);
        String response = sendAndGetResponse(message);
        log.info("Sent message to token. Device token: {}, {} msg {}", request.getToken(), response, jsonOutput);
    }

    private AndroidConfig getAndroidConfig(String topic) {
        return AndroidConfig.builder()
                .setTtl(Duration.ofMinutes(2).toMillis())
                .setCollapseKey(topic)
                .setPriority(AndroidConfig.Priority.HIGH)
                .setNotification(AndroidNotification.builder()
                        .setTag(topic)
                        .setSound("default")
                        .setPriority(AndroidNotification.Priority.HIGH)
                        .setVisibility(AndroidNotification.Visibility.PUBLIC)
                        .setChannelId("fcm_default_channel")
                        .build())
                .build();
    }

    private String sendAndGetResponse(Message message) throws InterruptedException, ExecutionException {
        return FirebaseMessaging.getInstance().sendAsync(message).get();
    }

    private ApnsConfig getApnsConfig(String topic) {
        return ApnsConfig.builder()
                .setAps(Aps.builder()
                        .setCategory(topic)
                        .setThreadId(topic)
                        .setSound("default")
                        .build())
                .build();
    }

    private Message getPreconfiguredMessageToken(NotificationRequest request) {
        return getPreconfiguredMessageBuilder(request)
                .setToken(request.getToken())
                .build();
    }

    private Message.Builder getPreconfiguredMessageBuilder(NotificationRequest request) {
        AndroidConfig androidConfig = getAndroidConfig(request.getTopic());
        ApnsConfig apnsConfig = getApnsConfig(request.getTopic());

        Notification.Builder notificationBuilder = Notification.builder()
                .setTitle(request.getTitle())
                .setBody(request.getBody());

        if (request.getImageUrl() != null && !request.getImageUrl().trim().isEmpty()) {
            notificationBuilder.setImage(request.getImageUrl());
        }

        Notification notification = notificationBuilder.build();

        Map<String, String> data = new HashMap<>();
        data.put("title", request.getTitle());
        data.put("body", request.getBody());
        if (request.getImageUrl() != null && !request.getImageUrl().trim().isEmpty()) {
            data.put("image", request.getImageUrl());
        }
        if (request.getTopic() != null) {
            data.put("topic", request.getTopic());
        }

        return Message.builder()
                .setApnsConfig(apnsConfig)
                .setAndroidConfig(androidConfig)
                .setNotification(notification)
                .putAllData(data);
    }
}