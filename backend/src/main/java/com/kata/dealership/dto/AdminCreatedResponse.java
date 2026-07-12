package com.kata.dealership.dto;

import lombok.*;

// No token here — unlike AuthResponse, this is returned to the *calling*
// admin who created the account, not to the new admin logging in themselves.
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AdminCreatedResponse {
    private String name;
    private String email;
    private String role;
}
