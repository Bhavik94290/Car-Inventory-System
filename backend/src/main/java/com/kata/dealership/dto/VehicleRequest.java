package com.kata.dealership.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class VehicleRequest {
    @NotBlank private String make;
    @NotBlank private String model;
    @NotBlank private String category;
    @NotNull @DecimalMin(value = "0.0", inclusive = false) private BigDecimal price;
    @NotNull @Min(0) private Integer quantity;
    private String imageUrl; // optional
}
