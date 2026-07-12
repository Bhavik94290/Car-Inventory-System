package com.kata.dealership.repository;

import com.kata.dealership.entity.Order;
import com.kata.dealership.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, String> {
    List<Order> findByUserOrderByCreatedAtDesc(User user);
}
