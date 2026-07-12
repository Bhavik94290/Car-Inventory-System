package com.kata.dealership.config;

import com.kata.dealership.entity.Vehicle;
import com.kata.dealership.repository.VehicleRepository;
import com.kata.dealership.util.IdGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * Seeds a realistic starter inventory on first boot only (skipped once any
 * vehicle exists), so it never clobbers real admin-entered data on restart.
 */
@Component
@RequiredArgsConstructor
public class VehicleDataSeeder implements CommandLineRunner {

    private final VehicleRepository vehicleRepository;

    private record SeedVehicle(String make, String model, String category, String price, int quantity) {
    }

    // Prices are capped well under Razorpay's default per-transaction limit
    // (commonly Rs. 5,00,000 on new/unactivated accounts — see RazorpayService),
    // leaving headroom for multi-quantity carts rather than realistic showroom
    // prices, so checkout actually completes on a fresh test account.
    private static final List<SeedVehicle> SEED_DATA = List.of(
            // Hatchback
            new SeedVehicle("Maruti Suzuki", "Swift", "Hatchback", "55000", 8),
            new SeedVehicle("Maruti Suzuki", "Baleno", "Hatchback", "65000", 6),
            new SeedVehicle("Hyundai", "i20", "Hatchback", "68000", 5),
            new SeedVehicle("Tata", "Altroz", "Hatchback", "62000", 7),

            // Sedan
            new SeedVehicle("Honda", "City", "Sedan", "95000", 5),
            new SeedVehicle("Hyundai", "Verna", "Sedan", "88000", 4),
            new SeedVehicle("Skoda", "Slavia", "Sedan", "99000", 3),
            new SeedVehicle("Volkswagen", "Virtus", "Sedan", "97000", 0),

            // SUV
            new SeedVehicle("Hyundai", "Creta", "SUV", "110000", 6),
            new SeedVehicle("Kia", "Seltos", "SUV", "105000", 5),
            new SeedVehicle("Toyota", "Fortuner", "SUV", "245000", 3),
            new SeedVehicle("Mahindra", "XUV700", "SUV", "165000", 4),
            new SeedVehicle("Tata", "Nexon", "SUV", "78000", 9),
            new SeedVehicle("Maruti Suzuki", "Grand Vitara", "SUV", "135000", 4),
            new SeedVehicle("MG", "Hector", "SUV", "150000", 2),

            // Pickup / commercial
            new SeedVehicle("Tata", "Ace", "Pickup", "58000", 6),
            new SeedVehicle("Isuzu", "D-Max V-Cross", "Pickup", "185000", 2),

            // Van / MPV
            new SeedVehicle("Toyota", "Innova Crysta", "Van", "160000", 5),
            new SeedVehicle("Kia", "Carnival", "Van", "230000", 2),
            new SeedVehicle("Maruti Suzuki", "Ertiga", "Van", "88000", 6),

            // Electric
            new SeedVehicle("Tata", "Nexon EV", "Electric", "125000", 4),
            new SeedVehicle("MG", "ZS EV", "Electric", "175000", 3),
            new SeedVehicle("Tata", "Tiago EV", "Electric", "78000", 5),

            // Sports
            new SeedVehicle("Ford", "Mustang GT", "Sports", "249000", 1)
    );

    @Override
    public void run(String... args) {
        if (vehicleRepository.count() > 0) {
            return;
        }

        Instant base = Instant.now().minusSeconds(SEED_DATA.size() * 60L);
        for (int i = 0; i < SEED_DATA.size(); i++) {
            SeedVehicle seed = SEED_DATA.get(i);
            vehicleRepository.save(Vehicle.builder()
                    .id(IdGenerator.generate("vehicle"))
                    .make(seed.make())
                    .model(seed.model())
                    .category(seed.category())
                    .price(new BigDecimal(seed.price()))
                    .quantity(seed.quantity())
                    .createdAt(base.plusSeconds(i * 60L))
                    .build());
        }
    }
}
