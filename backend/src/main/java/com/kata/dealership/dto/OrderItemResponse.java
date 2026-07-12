package com.kata.dealership.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class OrderItemResponse {
    private String vehicleId;
    private String make;
    private String model;
    private BigDecimal unitPrice;
    private Integer quantity;
}
