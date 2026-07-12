package com.kata.dealership.service;

import com.kata.dealership.dto.VehicleRequest;
import com.kata.dealership.entity.Vehicle;
import com.kata.dealership.exception.OutOfStockException;
import com.kata.dealership.exception.ResourceNotFoundException;
import com.kata.dealership.repository.VehicleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * TDD unit tests for VehicleService.
 * Written first (RED), then the service was implemented to make them pass (GREEN).
 */
@ExtendWith(MockitoExtension.class)
class VehicleServiceTest {

    @Mock
    private VehicleRepository vehicleRepository;

    @InjectMocks
    private VehicleService vehicleService;

    private Vehicle fortuner;

    @BeforeEach
    void setUp() {
        fortuner = Vehicle.builder()
                .id(1L)
                .make("Toyota")
                .model("Fortuner")
                .category("SUV")
                .price(new BigDecimal("3500000"))
                .quantity(4)
                .build();
    }

    // ---------- add ----------
    @Test
    @DisplayName("addVehicle saves and returns the vehicle")
    void addVehicle_savesVehicle() {
        VehicleRequest request = VehicleRequest.builder()
                .make("Maruti").model("Swift").category("Hatchback")
                .price(new BigDecimal("800000")).quantity(5).build();

        when(vehicleRepository.save(any(Vehicle.class)))
                .thenAnswer(inv -> {
                    Vehicle v = inv.getArgument(0);
                    v.setId(10L);
                    return v;
                });

        Vehicle saved = vehicleService.addVehicle(request);

        assertThat(saved.getId()).isEqualTo(10L);
        assertThat(saved.getMake()).isEqualTo("Maruti");
        assertThat(saved.getQuantity()).isEqualTo(5);
        verify(vehicleRepository).save(any(Vehicle.class));
    }

    // ---------- get all ----------
    @Test
    @DisplayName("getAllVehicles returns everything in the repository")
    void getAllVehicles_returnsList() {
        when(vehicleRepository.findAll()).thenReturn(List.of(fortuner));

        List<Vehicle> result = vehicleService.getAllVehicles();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getModel()).isEqualTo("Fortuner");
    }

    // ---------- search ----------
    @Test
    @DisplayName("search delegates filters to the repository and blanks become null")
    void search_delegatesToRepository() {
        when(vehicleRepository.search("Toyota", null, null, null, new BigDecimal("4000000")))
                .thenReturn(List.of(fortuner));

        List<Vehicle> result = vehicleService.search("Toyota", "", null, null, new BigDecimal("4000000"));

        assertThat(result).containsExactly(fortuner);
        verify(vehicleRepository).search("Toyota", null, null, null, new BigDecimal("4000000"));
    }

    // ---------- update ----------
    @Test
    @DisplayName("updateVehicle changes fields on an existing vehicle")
    void updateVehicle_updatesFields() {
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(fortuner));
        when(vehicleRepository.save(any(Vehicle.class))).thenAnswer(inv -> inv.getArgument(0));

        VehicleRequest request = VehicleRequest.builder()
                .make("Toyota").model("Fortuner Legender").category("SUV")
                .price(new BigDecimal("4200000")).quantity(2).build();

        Vehicle updated = vehicleService.updateVehicle(1L, request);

        assertThat(updated.getModel()).isEqualTo("Fortuner Legender");
        assertThat(updated.getPrice()).isEqualByComparingTo("4200000");
        assertThat(updated.getQuantity()).isEqualTo(2);
    }

    @Test
    @DisplayName("updateVehicle throws when the vehicle does not exist")
    void updateVehicle_notFound_throws() {
        when(vehicleRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> vehicleService.updateVehicle(99L,
                VehicleRequest.builder().make("x").model("y").category("z")
                        .price(BigDecimal.ONE).quantity(1).build()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ---------- delete ----------
    @Test
    @DisplayName("deleteVehicle removes an existing vehicle")
    void deleteVehicle_deletes() {
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(fortuner));

        vehicleService.deleteVehicle(1L);

        verify(vehicleRepository).delete(fortuner);
    }

    @Test
    @DisplayName("deleteVehicle throws when the vehicle does not exist")
    void deleteVehicle_notFound_throws() {
        when(vehicleRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> vehicleService.deleteVehicle(99L))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(vehicleRepository, never()).delete(any());
    }

    // ---------- purchase ----------
    @Test
    @DisplayName("purchase reduces quantity by 1 (5 -> 4 style)")
    void purchase_reducesQuantity() {
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(fortuner));
        when(vehicleRepository.save(any(Vehicle.class))).thenAnswer(inv -> inv.getArgument(0));

        Vehicle result = vehicleService.purchase(1L);

        assertThat(result.getQuantity()).isEqualTo(3); // was 4
    }

    @Test
    @DisplayName("purchase fails with OutOfStockException when quantity is 0")
    void purchase_outOfStock_throws() {
        fortuner.setQuantity(0);
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(fortuner));

        assertThatThrownBy(() -> vehicleService.purchase(1L))
                .isInstanceOf(OutOfStockException.class)
                .hasMessageContaining("out of stock");
        verify(vehicleRepository, never()).save(any());
    }

    // ---------- restock ----------
    @Test
    @DisplayName("restock increases quantity by the given amount")
    void restock_increasesQuantity() {
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(fortuner));
        when(vehicleRepository.save(any(Vehicle.class))).thenAnswer(inv -> inv.getArgument(0));

        Vehicle result = vehicleService.restock(1L, 3);

        assertThat(result.getQuantity()).isEqualTo(7); // 4 + 3
    }

    @Test
    @DisplayName("restock rejects zero or negative amounts")
    void restock_invalidAmount_throws() {
        assertThatThrownBy(() -> vehicleService.restock(1L, 0))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
