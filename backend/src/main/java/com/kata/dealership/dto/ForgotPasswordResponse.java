package com.kata.dealership.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ForgotPasswordResponse {
    private String message;

    // No email/SMTP is configured for this project, so the reset token is
    // returned directly (null when the email isn't registered) instead of
    // being emailed. A real deployment would send this via email only and
    // drop it from the response entirely.
    private String resetToken;
}
