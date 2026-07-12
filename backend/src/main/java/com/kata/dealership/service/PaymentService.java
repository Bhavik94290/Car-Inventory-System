package com.kata.dealership.service;

import com.kata.dealership.dto.*;
import com.kata.dealership.entity.Order;
import com.kata.dealership.entity.OrderItem;
import com.kata.dealership.entity.OrderStatus;
import com.kata.dealership.entity.Payment;
import com.kata.dealership.entity.PaymentStatus;
import com.kata.dealership.entity.User;
import com.kata.dealership.entity.Vehicle;
import com.kata.dealership.exception.OutOfStockException;
import com.kata.dealership.exception.PaymentVerificationException;
import com.kata.dealership.exception.ResourceNotFoundException;
import com.kata.dealership.repository.OrderRepository;
import com.kata.dealership.repository.PaymentRepository;
import com.kata.dealership.repository.UserRepository;
import com.kata.dealership.repository.VehicleRepository;
import com.kata.dealership.util.IdGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final OrderRepository orderRepository;
    private final VehicleRepository vehicleRepository;
    private final VehicleService vehicleService;
    private final UserRepository userRepository;
    private final PaymentRepository paymentRepository;
    private final RazorpayService razorpayService;

    @Transactional
    public CheckoutResponse checkout(CheckoutRequest request, String userEmail) {
        User user = findUser(userEmail);

        List<OrderItem> items = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (CartItemRequest itemReq : request.getItems()) {
            Vehicle vehicle = vehicleRepository.findById(itemReq.getVehicleId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Vehicle not found with id: " + itemReq.getVehicleId()));

            if (itemReq.getQuantity() > vehicle.getQuantity()) {
                throw new OutOfStockException(
                        vehicle.getMake() + " " + vehicle.getModel() + " has only "
                                + vehicle.getQuantity() + " in stock");
            }

            BigDecimal lineTotal = vehicle.getPrice().multiply(BigDecimal.valueOf(itemReq.getQuantity()));
            total = total.add(lineTotal);

            items.add(OrderItem.builder()
                    .id(IdGenerator.generate("item"))
                    .vehicleId(vehicle.getId())
                    .make(vehicle.getMake())
                    .model(vehicle.getModel())
                    .unitPrice(vehicle.getPrice())
                    .quantity(itemReq.getQuantity())
                    .build());
        }

        long amountInPaise = total.multiply(BigDecimal.valueOf(100)).setScale(0, RoundingMode.HALF_UP).longValueExact();
        // Razorpay caps "receipt" at 40 chars; a dash-free UUID (32 chars) plus prefix fits.
        String receipt = "rcpt_" + UUID.randomUUID().toString().replace("-", "");
        RazorpayOrderResponse razorpayOrder = razorpayService.createOrder(amountInPaise, "INR", receipt);

        Order order = Order.builder()
                .id(IdGenerator.generate("order"))
                .user(user)
                .totalAmount(total)
                .currency("INR")
                .razorpayOrderId(razorpayOrder.getId())
                .status(OrderStatus.CREATED)
                .createdAt(Instant.now())
                .build();
        items.forEach(item -> item.setOrder(order));
        order.setItems(items);

        Order saved = orderRepository.save(order);

        return CheckoutResponse.builder()
                .orderId(saved.getId())
                .razorpayOrderId(razorpayOrder.getId())
                .amount(amountInPaise)
                .currency("INR")
                .keyId(razorpayService.getKeyId())
                .build();
    }

    @Transactional
    public OrderResponse verifyPayment(VerifyPaymentRequest request, String userEmail) {
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + request.getOrderId()));

        if (!order.getUser().getEmail().equalsIgnoreCase(userEmail)) {
            throw new ResourceNotFoundException("Order not found: " + request.getOrderId());
        }
        if (!order.getRazorpayOrderId().equals(request.getRazorpayOrderId())) {
            throw new PaymentVerificationException("Order reference mismatch");
        }
        if (order.getStatus() == OrderStatus.PAID) {
            return toResponse(order); // idempotent replay
        }

        boolean valid = razorpayService.verifySignature(
                request.getRazorpayOrderId(), request.getRazorpayPaymentId(), request.getRazorpaySignature());
        if (!valid) {
            order.setStatus(OrderStatus.FAILED);
            orderRepository.save(order);
            paymentRepository.save(Payment.builder()
                    .id(IdGenerator.generate("pay"))
                    .order(order)
                    .razorpayPaymentId(request.getRazorpayPaymentId())
                    .razorpaySignature(request.getRazorpaySignature())
                    .amount(order.getTotalAmount())
                    .currency(order.getCurrency())
                    .status(PaymentStatus.FAILED)
                    .createdAt(Instant.now())
                    .build());
            throw new PaymentVerificationException("Payment signature verification failed");
        }

        // Payment is confirmed genuine at this point. Decrementing stock can still fail
        // (e.g. another buyer drained it in the meantime); that's a rare edge case this
        // demo doesn't auto-refund for — it would need Razorpay's Refunds API in a real deployment.
        for (OrderItem item : order.getItems()) {
            for (int i = 0; i < item.getQuantity(); i++) {
                vehicleService.purchase(item.getVehicleId());
            }
        }

        order.setStatus(OrderStatus.PAID);
        Order saved = orderRepository.save(order);

        paymentRepository.save(Payment.builder()
                .id(IdGenerator.generate("pay"))
                .order(saved)
                .razorpayPaymentId(request.getRazorpayPaymentId())
                .razorpaySignature(request.getRazorpaySignature())
                .amount(saved.getTotalAmount())
                .currency(saved.getCurrency())
                .status(PaymentStatus.SUCCESS)
                .createdAt(Instant.now())
                .build());

        return toResponse(saved);
    }

    public List<OrderResponse> getOrdersForUser(String userEmail) {
        User user = findUser(userEmail);
        return orderRepository.findByUserOrderByCreatedAtDesc(user).stream()
                .map(this::toResponse)
                .toList();
    }

    private User findUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
    }

    private OrderResponse toResponse(Order order) {
        Optional<Payment> payment = paymentRepository.findFirstByOrderOrderByCreatedAtDesc(order);

        return OrderResponse.builder()
                .id(order.getId())
                .status(order.getStatus())
                .totalAmount(order.getTotalAmount())
                .currency(order.getCurrency())
                .razorpayOrderId(order.getRazorpayOrderId())
                .paymentId(payment.map(Payment::getId).orElse(null))
                .razorpayPaymentId(payment.map(Payment::getRazorpayPaymentId).orElse(null))
                .createdAt(order.getCreatedAt())
                .items(order.getItems().stream()
                        .map(item -> OrderItemResponse.builder()
                                .vehicleId(item.getVehicleId())
                                .make(item.getMake())
                                .model(item.getModel())
                                .unitPrice(item.getUnitPrice())
                                .quantity(item.getQuantity())
                                .build())
                        .toList())
                .build();
    }
}
