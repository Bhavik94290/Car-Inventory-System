package com.kata.dealership.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ResetPasswordRequest {
    @NotBlank @Email private String email;

    @NotBlank
    @Pattern(regexp = "^\\d{6}$", message = "Code must be 6 digits")
    private String otp;

    @NotBlank
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9\\s]).{8,}$",
            message = "Password must be at least 8 characters and include an uppercase letter, a lowercase letter, a digit, and a symbol"
    )
    private String newPassword;
}
