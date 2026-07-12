package com.kata.dealership.service;

import com.kata.dealership.exception.EmailSendException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String fromAddress;

    public void sendPasswordResetOtp(String toEmail, String otp) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");
            helper.setFrom(fromAddress);
            helper.setTo(toEmail);
            helper.setSubject("Your password reset code");
            helper.setText(
                    "Your one-time password reset code is: " + otp + "\n\n"
                            + "This code expires in 10 minutes. If you didn't request this, you can ignore this email.",
                    false);
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new EmailSendException("Could not send the password reset email", e);
        }
    }
}
