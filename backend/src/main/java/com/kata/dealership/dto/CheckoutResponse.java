package com.kata.dealership.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CheckoutResponse {
    private String orderId;
    private String razorpayOrderId;
    private Long amount; // smallest currency unit (paise for INR)
    private String currency;
    private String keyId; // Razorpay public key id, safe to expose to the frontend
}
