package com.kata.dealership.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "users")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class User {

    @Id
    private String id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    // Holds a 6-digit OTP (not a unique token), so no uniqueness constraint —
    // different users can legitimately be issued the same code at once.
    private String resetToken;

    private Instant resetTokenExpiresAt;
}
