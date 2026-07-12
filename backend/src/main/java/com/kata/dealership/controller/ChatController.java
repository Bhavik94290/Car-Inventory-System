package com.kata.dealership.controller;

import com.kata.dealership.dto.ChatRequest;
import com.kata.dealership.dto.ChatResponse;
import com.kata.dealership.service.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    // Public: works for anonymous visitors (product search) and logged-in users (order lookup too).
    @PostMapping
    public ResponseEntity<ChatResponse> chat(@Valid @RequestBody ChatRequest request, Authentication authentication) {
        String userEmail = (authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken))
                ? authentication.getName()
                : null;
        return ResponseEntity.ok(chatService.chat(request, userEmail));
    }
}
