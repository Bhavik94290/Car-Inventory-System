package com.kata.dealership.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;

import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CheckoutRequest {
    @NotEmpty(message = "Cart must contain at least one item")
    @Valid
    private List<CartItemRequest> items;
}
