package com.kata.dealership.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CartItemRequest {
    @NotNull private String vehicleId;
    @NotNull @Min(1) private Integer quantity;
}
