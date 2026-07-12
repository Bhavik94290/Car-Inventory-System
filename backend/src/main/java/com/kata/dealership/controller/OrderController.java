package com.kata.dealership.controller;

import com.kata.dealership.dto.CheckoutRequest;
import com.kata.dealership.dto.CheckoutResponse;
import com.kata.dealership.dto.OrderResponse;
import com.kata.dealership.dto.VerifyPaymentRequest;
import com.kata.dealership.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final PaymentService paymentService;

    // ---- any logged-in user (enforced by the default authenticated() rule in SecurityConfig) ----

    @PostMapping("/checkout")
    public ResponseEntity<CheckoutResponse> checkout(@Valid @RequestBody CheckoutRequest request,
                                                       Authentication authentication) {
        return ResponseEntity.ok(paymentService.checkout(request, authentication.getName()));
    }

    @PostMapping("/verify")
    public ResponseEntity<OrderResponse> verify(@Valid @RequestBody VerifyPaymentRequest request,
                                                 Authentication authentication) {
        return ResponseEntity.ok(paymentService.verifyPayment(request, authentication.getName()));
    }

    @GetMapping("/mine")
    public ResponseEntity<List<OrderResponse>> myOrders(Authentication authentication) {
        return ResponseEntity.ok(paymentService.getOrdersForUser(authentication.getName()));
    }
}
