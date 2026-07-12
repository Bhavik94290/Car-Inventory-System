package com.kata.dealership.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ChatRequest {
    @NotBlank
    private String message;

    @Builder.Default
    private List<ChatMessageDto> history = new ArrayList<>();
}
