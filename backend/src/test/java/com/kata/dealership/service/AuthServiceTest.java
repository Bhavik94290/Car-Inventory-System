package com.kata.dealership.service;

import com.kata.dealership.dto.AdminCreatedResponse;
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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * TDD unit tests for AuthService (register + login).
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtService jwtService;
    @Mock private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    @Test
    @DisplayName("register creates a USER by default, hashes password, returns token")
    void register_createsUserAndReturnsToken() {
        RegisterRequest request = RegisterRequest.builder()
                .name("Ravi").email("ravi@example.com").password("secret123").build();

        when(userRepository.existsByEmail("ravi@example.com")).thenReturn(false);
        when(passwordEncoder.encode("secret123")).thenReturn("$2a$hashed");
        when(jwtService.generateToken(any(UserDetails.class), anyMap())).thenReturn("jwt-token");

        AuthResponse response = authService.register(request);

        assertThat(response.getToken()).isEqualTo("jwt-token");
        assertThat(response.getRole()).isEqualTo("USER");
        verify(userRepository).save(argThat(u ->
                u.getEmail().equals("ravi@example.com")
                        && u.getPassword().equals("$2a$hashed")
                        && u.getRole() == Role.USER));
    }

    @Test
    @DisplayName("registerAdmin creates an ADMIN account, bypassing the public register path")
    void registerAdmin_createsAdminAccount() {
        RegisterRequest request = RegisterRequest.builder()
                .name("Boss").email("boss@example.com").password("secret123").build();

        when(userRepository.existsByEmail("boss@example.com")).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hash");

        AdminCreatedResponse response = authService.registerAdmin(request);

        assertThat(response.getRole()).isEqualTo("ADMIN");
        assertThat(response.getEmail()).isEqualTo("boss@example.com");
        verify(userRepository).save(argThat(u -> u.getRole() == Role.ADMIN));
        verifyNoInteractions(jwtService);
    }

    @Test
    @DisplayName("register rejects duplicate email")
    void register_duplicateEmail_throws() {
        when(userRepository.existsByEmail("ravi@example.com")).thenReturn(true);

        RegisterRequest request = RegisterRequest.builder()
                .name("Ravi").email("ravi@example.com").password("secret123").build();

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(DuplicateEmailException.class);
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("login authenticates and returns a token for a valid user")
    void login_success_returnsToken() {
        User user = User.builder()
                .id("user_1").name("Ravi").email("ravi@example.com")
                .password("$2a$hashed").role(Role.USER).build();

        when(userRepository.findByEmail("ravi@example.com")).thenReturn(Optional.of(user));
        when(jwtService.generateToken(any(UserDetails.class), anyMap())).thenReturn("jwt-token");

        AuthResponse response = authService.login(
                LoginRequest.builder().email("ravi@example.com").password("secret123").build());

        assertThat(response.getToken()).isEqualTo("jwt-token");
        assertThat(response.getEmail()).isEqualTo("ravi@example.com");
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    @DisplayName("login fails with wrong credentials")
    void login_badCredentials_throws() {
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("bad creds"));

        assertThatThrownBy(() -> authService.login(
                LoginRequest.builder().email("ravi@example.com").password("wrong").build()))
                .isInstanceOf(BadCredentialsException.class);
        verify(jwtService, never()).generateToken(any(), anyMap());
    }

    @Test
    @DisplayName("forgotPassword sets a reset token on the matching user")
    void forgotPassword_existingEmail_setsResetToken() {
        User user = User.builder()
                .id("user_1").name("Ravi").email("ravi@example.com")
                .password("$2a$hashed").role(Role.USER).build();

        when(userRepository.findByEmail("ravi@example.com")).thenReturn(Optional.of(user));

        ForgotPasswordResponse response = authService.forgotPassword(
                ForgotPasswordRequest.builder().email("ravi@example.com").build());

        assertThat(response.getMessage()).isNotBlank();
        assertThat(response.getResetToken()).isNotBlank();
        verify(userRepository).save(argThat(u ->
                u.getResetToken() != null
                        && u.getResetTokenExpiresAt() != null
                        && u.getResetTokenExpiresAt().isAfter(Instant.now())));
    }

    @Test
    @DisplayName("forgotPassword for an unknown email returns the same generic response without saving")
    void forgotPassword_unknownEmail_doesNotSave() {
        when(userRepository.findByEmail("nobody@example.com")).thenReturn(Optional.empty());

        ForgotPasswordResponse response = authService.forgotPassword(
                ForgotPasswordRequest.builder().email("nobody@example.com").build());

        assertThat(response.getMessage()).isNotBlank();
        assertThat(response.getResetToken()).isNull();
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("resetPassword updates the password and clears the token for a valid token")
    void resetPassword_validToken_updatesPassword() {
        User user = User.builder()
                .id("user_1").name("Ravi").email("ravi@example.com")
                .password("$2a$oldhash").role(Role.USER)
                .resetToken("valid-token").resetTokenExpiresAt(Instant.now().plusSeconds(600))
                .build();

        when(userRepository.findByResetToken("valid-token")).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("NewPass123!")).thenReturn("$2a$newhash");

        authService.resetPassword(ResetPasswordRequest.builder()
                .token("valid-token").newPassword("NewPass123!").build());

        verify(userRepository).save(argThat(u ->
                u.getPassword().equals("$2a$newhash")
                        && u.getResetToken() == null
                        && u.getResetTokenExpiresAt() == null));
    }

    @Test
    @DisplayName("resetPassword rejects an unknown token")
    void resetPassword_unknownToken_throws() {
        when(userRepository.findByResetToken("bogus")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.resetPassword(ResetPasswordRequest.builder()
                .token("bogus").newPassword("NewPass123!").build()))
                .isInstanceOf(InvalidResetTokenException.class);
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("resetPassword rejects an expired token")
    void resetPassword_expiredToken_throws() {
        User user = User.builder()
                .id("user_1").name("Ravi").email("ravi@example.com")
                .password("$2a$oldhash").role(Role.USER)
                .resetToken("expired-token").resetTokenExpiresAt(Instant.now().minusSeconds(60))
                .build();

        when(userRepository.findByResetToken("expired-token")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> authService.resetPassword(ResetPasswordRequest.builder()
                .token("expired-token").newPassword("NewPass123!").build()))
                .isInstanceOf(InvalidResetTokenException.class);
        verify(userRepository, never()).save(any());
    }
}
