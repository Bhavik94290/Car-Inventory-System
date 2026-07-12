package com.kata.dealership.service;

import com.kata.dealership.dto.AuthResponse;
import com.kata.dealership.dto.ForgotPasswordRequest;
import com.kata.dealership.dto.ForgotPasswordResponse;
import com.kata.dealership.dto.LoginRequest;
import com.kata.dealership.dto.RegisterRequest;
import com.kata.dealership.dto.ResetPasswordRequest;
import com.kata.dealership.entity.Role;
import com.kata.dealership.entity.User;
import com.kata.dealership.exception.DuplicateEmailException;
import com.kata.dealership.exception.InvalidResetTokenException;
import com.kata.dealership.repository.UserRepository;
import com.kata.dealership.security.JwtService;
import com.kata.dealership.util.IdGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final long RESET_TOKEN_VALIDITY_MINUTES = 30;
    private static final String FORGOT_PASSWORD_MESSAGE =
            "If an account exists for that email, a password reset link has been sent.";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateEmailException("Email already registered: " + request.getEmail());
        }

        Role role = "ADMIN".equalsIgnoreCase(request.getRole()) ? Role.ADMIN : Role.USER;

        User user = User.builder()
                .id(IdGenerator.generate("user"))
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(role)
                .build();

        userRepository.save(user);
        return buildResponse(user);
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalStateException("User disappeared after authentication"));

        return buildResponse(user);
    }

    public ForgotPasswordResponse forgotPassword(ForgotPasswordRequest request) {
        Optional<User> maybeUser = userRepository.findByEmail(request.getEmail());
        if (maybeUser.isEmpty()) {
            // Same response whether or not the email is registered, so callers
            // can't use this endpoint to discover which emails have accounts.
            return ForgotPasswordResponse.builder().message(FORGOT_PASSWORD_MESSAGE).build();
        }

        User user = maybeUser.get();
        String token = UUID.randomUUID().toString();
        user.setResetToken(token);
        user.setResetTokenExpiresAt(Instant.now().plus(RESET_TOKEN_VALIDITY_MINUTES, ChronoUnit.MINUTES));
        userRepository.save(user);

        return ForgotPasswordResponse.builder()
                .message(FORGOT_PASSWORD_MESSAGE)
                .resetToken(token)
                .build();
    }

    public void resetPassword(ResetPasswordRequest request) {
        User user = userRepository.findByResetToken(request.getToken())
                .filter(u -> u.getResetTokenExpiresAt() != null && u.getResetTokenExpiresAt().isAfter(Instant.now()))
                .orElseThrow(() -> new InvalidResetTokenException("Reset link is invalid or has expired"));

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setResetToken(null);
        user.setResetTokenExpiresAt(null);
        userRepository.save(user);
    }

    private AuthResponse buildResponse(User user) {
        var userDetails = new org.springframework.security.core.userdetails.User(
                user.getEmail(), user.getPassword(),
                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name())));

        String token = jwtService.generateToken(userDetails, Map.of("role", user.getRole().name(), "name", user.getName()));

        return AuthResponse.builder()
                .token(token)
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .build();
    }
}
