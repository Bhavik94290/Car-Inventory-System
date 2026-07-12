package com.kata.dealership.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "vehicles", indexes = {
        @Index(name = "idx_vehicle_make", columnList = "make"),
        @Index(name = "idx_vehicle_model", columnList = "model"),
        @Index(name = "idx_vehicle_category", columnList = "category"),
        @Index(name = "idx_vehicle_price", columnList = "price"),
})
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Vehicle {

    @Id
    private String id;

    @Column(nullable = false)
    private String make;

    @Column(nullable = false)
    private String model;

    @Column(nullable = false)
    private String category;

    @Column(nullable = false)
    private BigDecimal price;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private Instant createdAt;
}
