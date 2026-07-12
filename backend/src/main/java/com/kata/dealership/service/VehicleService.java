package com.kata.dealership.service;

import com.kata.dealership.dto.VehicleRequest;
import com.kata.dealership.entity.Vehicle;
import com.kata.dealership.exception.OutOfStockException;
import com.kata.dealership.exception.ResourceNotFoundException;
import com.kata.dealership.repository.VehicleRepository;
import com.kata.dealership.util.IdGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VehicleService {

    private final VehicleRepository vehicleRepository;

    public Vehicle addVehicle(VehicleRequest request) {
        Vehicle vehicle = Vehicle.builder()
                .id(IdGenerator.generate("vehicle"))
                .make(request.getMake())
                .model(request.getModel())
                .category(request.getCategory())
                .price(request.getPrice())
                .quantity(request.getQuantity())
                .createdAt(Instant.now())
                .build();
        return vehicleRepository.save(vehicle);
    }

    public List<Vehicle> getAllVehicles() {
        return vehicleRepository.findAll();
    }

    public Vehicle getVehicle(String id) {
        return vehicleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with id: " + id));
    }

    public Page<Vehicle> search(String make, String model, String category,
                                BigDecimal minPrice, BigDecimal maxPrice,
                                boolean inStockOnly, Pageable pageable) {
        return vehicleRepository.search(emptyToNull(make), emptyToNull(model),
                emptyToNull(category), minPrice, maxPrice, inStockOnly, pageable);
    }

    public Vehicle updateVehicle(String id, VehicleRequest request) {
        Vehicle vehicle = getVehicle(id);
        vehicle.setMake(request.getMake());
        vehicle.setModel(request.getModel());
        vehicle.setCategory(request.getCategory());
        vehicle.setPrice(request.getPrice());
        vehicle.setQuantity(request.getQuantity());
        return vehicleRepository.save(vehicle);
    }

    public void deleteVehicle(String id) {
        Vehicle vehicle = getVehicle(id);
        vehicleRepository.delete(vehicle);
    }

    @Transactional
    public Vehicle purchase(String id) {
        Vehicle vehicle = getVehicle(id);
        if (vehicle.getQuantity() <= 0) {
            throw new OutOfStockException(
                    vehicle.getMake() + " " + vehicle.getModel() + " is out of stock");
        }
        vehicle.setQuantity(vehicle.getQuantity() - 1);
        return vehicleRepository.save(vehicle);
    }

    @Transactional
    public Vehicle restock(String id, int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Restock amount must be positive");
        }
        Vehicle vehicle = getVehicle(id);
        vehicle.setQuantity(vehicle.getQuantity() + amount);
        return vehicleRepository.save(vehicle);
    }

    private String emptyToNull(String s) {
        return (s == null || s.isBlank()) ? null : s;
    }
}
