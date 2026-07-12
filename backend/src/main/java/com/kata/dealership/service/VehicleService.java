package com.kata.dealership.service;

import com.kata.dealership.dto.VehicleRequest;
import com.kata.dealership.entity.Vehicle;
import com.kata.dealership.exception.OutOfStockException;
import com.kata.dealership.exception.ResourceNotFoundException;
import com.kata.dealership.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VehicleService {

    private final VehicleRepository vehicleRepository;

    public Vehicle addVehicle(VehicleRequest request) {
        Vehicle vehicle = Vehicle.builder()
                .make(request.getMake())
                .model(request.getModel())
                .category(request.getCategory())
                .price(request.getPrice())
                .quantity(request.getQuantity())
                .build();
        return vehicleRepository.save(vehicle);
    }

    public List<Vehicle> getAllVehicles() {
        return vehicleRepository.findAll();
    }

    public Vehicle getVehicle(Long id) {
        return vehicleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with id: " + id));
    }

    public List<Vehicle> search(String make, String model, String category,
                                BigDecimal minPrice, BigDecimal maxPrice) {
        return vehicleRepository.search(emptyToNull(make), emptyToNull(model),
                emptyToNull(category), minPrice, maxPrice);
    }

    public Vehicle updateVehicle(Long id, VehicleRequest request) {
        Vehicle vehicle = getVehicle(id);
        vehicle.setMake(request.getMake());
        vehicle.setModel(request.getModel());
        vehicle.setCategory(request.getCategory());
        vehicle.setPrice(request.getPrice());
        vehicle.setQuantity(request.getQuantity());
        return vehicleRepository.save(vehicle);
    }

    public void deleteVehicle(Long id) {
        Vehicle vehicle = getVehicle(id);
        vehicleRepository.delete(vehicle);
    }

    @Transactional
    public Vehicle purchase(Long id) {
        Vehicle vehicle = getVehicle(id);
        if (vehicle.getQuantity() <= 0) {
            throw new OutOfStockException(
                    vehicle.getMake() + " " + vehicle.getModel() + " is out of stock");
        }
        vehicle.setQuantity(vehicle.getQuantity() - 1);
        return vehicleRepository.save(vehicle);
    }

    @Transactional
    public Vehicle restock(Long id, int amount) {
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
