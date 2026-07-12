package com.kata.dealership.dto;

import com.kata.dealership.entity.OrderStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class OrderResponse {
    private String id;
    private OrderStatus status;
    private BigDecimal totalAmount;
    private String currency;
    private String razorpayOrderId;
    private String paymentId; // our own internal Payment record id (pay_xxx), if payment was attempted
    private String razorpayPaymentId;
    private Instant createdAt;
    private List<OrderItemResponse> items;
}
