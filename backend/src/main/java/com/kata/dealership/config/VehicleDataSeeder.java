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

    private record SeedVehicle(String make, String model, String category, String price, int quantity, String imageUrl) {
    }

    // Prices are capped well under Razorpay's default per-transaction limit
    // (commonly Rs. 5,00,000 on new/unactivated accounts — see RazorpayService),
    // leaving headroom for multi-quantity carts rather than realistic showroom
    // prices, so checkout actually completes on a fresh test account.
    //
    // imageUrl points to a real photo of the matching make/model, sourced from
    // Wikimedia Commons (freely licensed) and hotlinked directly rather than
    // stored locally.
    private static final List<SeedVehicle> SEED_DATA = List.of(
            // Hatchback
            new SeedVehicle("Maruti Suzuki", "Swift", "Hatchback", "55000", 8,
                    "https://upload.wikimedia.org/wikipedia/commons/thumb/3/3d/Suzuki_Swift_%282024%29_hybrid_DSC_6076.jpg/330px-Suzuki_Swift_%282024%29_hybrid_DSC_6076.jpg"),
            new SeedVehicle("Maruti Suzuki", "Baleno", "Hatchback", "65000", 6,
                    "https://upload.wikimedia.org/wikipedia/commons/thumb/5/59/Suzuki_Baleno_front_20071004.jpg/330px-Suzuki_Baleno_front_20071004.jpg"),
            new SeedVehicle("Hyundai", "i20", "Hatchback", "68000", 5,
                    "https://upload.wikimedia.org/wikipedia/commons/thumb/e/ea/Hyundai_i20_%28III%2C_Facelift%29_%E2%80%93_f_11102025.jpg/330px-Hyundai_i20_%28III%2C_Facelift%29_%E2%80%93_f_11102025.jpg"),
            new SeedVehicle("Tata", "Altroz", "Hatchback", "62000", 7,
                    "https://upload.wikimedia.org/wikipedia/commons/thumb/8/8c/Tata_Altroz_front_20230617.jpg/330px-Tata_Altroz_front_20230617.jpg"),

            // Sedan
            new SeedVehicle("Honda", "City", "Sedan", "95000", 5,
                    "https://upload.wikimedia.org/wikipedia/commons/thumb/a/a9/2022_Honda_City_ZX_i-VTEC_%28India%29_front_view_%28cropped%29.jpg/330px-2022_Honda_City_ZX_i-VTEC_%28India%29_front_view_%28cropped%29.jpg"),
            new SeedVehicle("Hyundai", "Verna", "Sedan", "88000", 4,
                    "https://upload.wikimedia.org/wikipedia/commons/thumb/9/9d/2019_Hyundai_Accent_1.6L%2C_front_10.8.19.jpg/330px-2019_Hyundai_Accent_1.6L%2C_front_10.8.19.jpg"),
            new SeedVehicle("Skoda", "Slavia", "Sedan", "99000", 3,
                    "https://upload.wikimedia.org/wikipedia/commons/thumb/9/92/2021_%C5%A0koda_Slavia_1.5_TSI_Style_%28India%29_front_view.png/330px-2021_%C5%A0koda_Slavia_1.5_TSI_Style_%28India%29_front_view.png"),
            new SeedVehicle("Volkswagen", "Virtus", "Sedan", "97000", 0,
                    "https://upload.wikimedia.org/wikipedia/commons/thumb/9/9e/2022_Volkswagen_Virtus_1.5_GT_%28India%29_front_view_02.png/330px-2022_Volkswagen_Virtus_1.5_GT_%28India%29_front_view_02.png"),

            // SUV
            new SeedVehicle("Hyundai", "Creta", "SUV", "110000", 6,
                    "https://upload.wikimedia.org/wikipedia/commons/thumb/2/25/2022_Hyundai_Creta_1.6_Plus_%28Chile%29_front_view.jpg/330px-2022_Hyundai_Creta_1.6_Plus_%28Chile%29_front_view.jpg"),
            new SeedVehicle("Kia", "Seltos", "SUV", "105000", 5,
                    "https://upload.wikimedia.org/wikipedia/commons/thumb/6/6c/Kia_Seltos_SP2_PE_Snow_White_Pearl_%2817%29_%28cropped%29.jpg/330px-Kia_Seltos_SP2_PE_Snow_White_Pearl_%2817%29_%28cropped%29.jpg"),
            new SeedVehicle("Toyota", "Fortuner", "SUV", "245000", 3,
                    "https://upload.wikimedia.org/wikipedia/commons/thumb/6/66/2015_Toyota_Fortuner_%28New_Zealand%29.jpg/330px-2015_Toyota_Fortuner_%28New_Zealand%29.jpg"),
            new SeedVehicle("Mahindra", "XUV700", "SUV", "165000", 4,
                    "https://upload.wikimedia.org/wikipedia/commons/thumb/b/ba/2021_Mahindra_XUV700_2.2_AX7_%28India%29_front_view.png/330px-2021_Mahindra_XUV700_2.2_AX7_%28India%29_front_view.png"),
            new SeedVehicle("Tata", "Nexon", "SUV", "78000", 9,
                    "https://upload.wikimedia.org/wikipedia/commons/thumb/2/25/Tata_Nexon_Blue_Dual_Tone.jpg/330px-Tata_Nexon_Blue_Dual_Tone.jpg"),
            new SeedVehicle("Maruti Suzuki", "Grand Vitara", "SUV", "135000", 4,
                    "https://upload.wikimedia.org/wikipedia/commons/thumb/0/0f/2022_Suzuki_Grand_Vitara_GX_Smart_Hybrid_%28Indonesia%29_front_view.jpg/330px-2022_Suzuki_Grand_Vitara_GX_Smart_Hybrid_%28Indonesia%29_front_view.jpg"),
            new SeedVehicle("MG", "Hector", "SUV", "150000", 2,
                    "https://upload.wikimedia.org/wikipedia/commons/thumb/0/0d/MG%28Morris_Garages%29_Hector_SUV_in_Jamshedpur%2C_Jharkhand%2C_India_%28Ank_Kumar%2C_Infosys_Limited%29%29_02.jpg/330px-MG%28Morris_Garages%29_Hector_SUV_in_Jamshedpur%2C_Jharkhand%2C_India_%28Ank_Kumar%2C_Infosys_Limited%29%29_02.jpg"),

            // Pickup / commercial
            new SeedVehicle("Tata", "Ace", "Pickup", "58000", 6,
                    "https://upload.wikimedia.org/wikipedia/commons/thumb/8/83/Tataintroace.jpg/330px-Tataintroace.jpg"),
            new SeedVehicle("Isuzu", "D-Max V-Cross", "Pickup", "185000", 2,
                    "https://upload.wikimedia.org/wikipedia/commons/thumb/0/0d/Isuzu_D-Max_%28third_generation%29_autoMOBIL_T%C3%BCbingen_2025_DSC_2758.jpg/330px-Isuzu_D-Max_%28third_generation%29_autoMOBIL_T%C3%BCbingen_2025_DSC_2758.jpg"),

            // Van / MPV
            new SeedVehicle("Toyota", "Innova Crysta", "Van", "160000", 5,
                    "https://upload.wikimedia.org/wikipedia/commons/thumb/8/87/Toyota_Innova_Crysta_2.4_Z_front_right.jpg/330px-Toyota_Innova_Crysta_2.4_Z_front_right.jpg"),
            new SeedVehicle("Kia", "Carnival", "Van", "230000", 2,
                    "https://upload.wikimedia.org/wikipedia/commons/thumb/a/a7/2025_Kia_Carnival_Hybrid_EX%2C_front_right%2C_10-12-2025.jpg/330px-2025_Kia_Carnival_Hybrid_EX%2C_front_right%2C_10-12-2025.jpg"),
            new SeedVehicle("Maruti Suzuki", "Ertiga", "Van", "88000", 6,
                    "https://upload.wikimedia.org/wikipedia/commons/thumb/f/f9/Suzuki_Ertiga_NC_FL_1.5_GLX_Hybrid_Snow_White_Pearl.jpg/330px-Suzuki_Ertiga_NC_FL_1.5_GLX_Hybrid_Snow_White_Pearl.jpg"),

            // Electric
            new SeedVehicle("Tata", "Nexon EV", "Electric", "125000", 4,
                    "https://upload.wikimedia.org/wikipedia/commons/thumb/e/ea/2020_Tata_Nexon_EV_%28India%29_front_view.png/330px-2020_Tata_Nexon_EV_%28India%29_front_view.png"),
            new SeedVehicle("MG", "ZS EV", "Electric", "175000", 3,
                    "https://upload.wikimedia.org/wikipedia/commons/thumb/c/cf/MG_ZS_%28crossover%2C_second_generation%29_DSC_8542.jpg/330px-MG_ZS_%28crossover%2C_second_generation%29_DSC_8542.jpg"),
            new SeedVehicle("Tata", "Tiago EV", "Electric", "78000", 5,
                    "https://upload.wikimedia.org/wikipedia/commons/thumb/c/ca/2022_Tata_Tiago_XZA%2B_front_20230512.jpg/330px-2022_Tata_Tiago_XZA%2B_front_20230512.jpg"),

            // Sports
            new SeedVehicle("Ford", "Mustang GT", "Sports", "249000", 1,
                    "https://upload.wikimedia.org/wikipedia/commons/thumb/1/1f/2019_Ford_Mustang_GT_5.0_facelift.jpg/330px-2019_Ford_Mustang_GT_5.0_facelift.jpg")
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
                    .imageUrl(seed.imageUrl())
                    .createdAt(base.plusSeconds(i * 60L))
                    .build());
        }
    }
}
