package com.kata.dealership.service;

import com.kata.dealership.dto.AuthResponse;
import com.kata.dealership.dto.LoginRequest;
import com.kata.dealership.dto.RegisterRequest;
import com.kata.dealership.entity.Role;
import com.kata.dealership.entity.User;
import com.kata.dealership.exception.DuplicateEmailException;
import com.kata.dealership.repository.UserRepository;
import com.kata.dealership.security.JwtService;
import com.kata.dealership.util.IdGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {

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
