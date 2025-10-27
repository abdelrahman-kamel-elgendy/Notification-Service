package com.e_commerce.notification_service.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.e_commerce.notification_service.dto.NotificationResponse;
import com.e_commerce.notification_service.services.NotificationService;

@RestController
@RequestMapping("/api/notifications/email")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @PostMapping("/password-reset")
    public ResponseEntity<NotificationResponse> sendPasswordResetEmail(
            @RequestParam String email,
            @RequestParam String name,
            @RequestParam String resetUrl) {
        return ResponseEntity.ok(notificationService.sendPasswordResetEmail(email, name, resetUrl));
    }

    @PostMapping("/welcome")
    public ResponseEntity<NotificationResponse> sendWelcomeEmail(
            @RequestParam String email, @RequestParam String name) {
        return ResponseEntity.ok(notificationService.sendWelcomeEmail(email, name));
    }

    @PostMapping("/account-locked")
    public ResponseEntity<NotificationResponse> sendAccountLockedEmail(
            @RequestParam String email,
            @RequestParam String name,
            @RequestParam String unlockLink) {
        return ResponseEntity.ok(notificationService.sendAccountLockedEmail(email, name, unlockLink));
    }

    @PostMapping("/email-verification")
    public ResponseEntity<NotificationResponse> sendEmailVerification(
            @RequestParam String to,
            @RequestParam String userName,
            @RequestParam String verificationUrl) {
        return ResponseEntity.ok(notificationService.sendEmailVerificationEmail(to, userName, verificationUrl));
    }
}