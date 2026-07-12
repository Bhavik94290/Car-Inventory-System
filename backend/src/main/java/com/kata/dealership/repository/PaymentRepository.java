package com.kata.dealership.repository;

import com.kata.dealership.entity.Order;
import com.kata.dealership.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, String> {
    Optional<Payment> findFirstByOrderOrderByCreatedAtDesc(Order order);
}
