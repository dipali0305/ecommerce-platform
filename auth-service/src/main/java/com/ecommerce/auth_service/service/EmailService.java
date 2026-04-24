package com.ecommerce.auth_service.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmailService {
    @Value("${spring.mail.username}")
    private String adminEmail;

    private final JavaMailSender javaMailSender;

    @Async
    public void sendPasswordResetEmail(String toEmail, String token, long expiryMinutes) {

        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setTo(toEmail);
        mail.setFrom(adminEmail);
        mail.setSubject("Reset Your Password");

        mail.setText("""
            Hello,

            You requested to reset your password.

            Use the below token to reset your password:

            Token: %s

            Call the reset password API with this token and your new password.

            This token will expire in %d minutes.

            """.formatted(token, expiryMinutes));

        javaMailSender.send(mail);

        log.info("Password reset email sent to {}", toEmail);

    }
}
