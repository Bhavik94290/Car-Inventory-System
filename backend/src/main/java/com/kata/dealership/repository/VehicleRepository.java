package com.kata.dealership.repository;

import com.kata.dealership.entity.Vehicle;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;

public interface VehicleRepository extends JpaRepository<Vehicle, String> {

    @Query("""
           SELECT v FROM Vehicle v
           WHERE (:make IS NULL OR LOWER(v.make) LIKE LOWER(CONCAT('%', :make, '%')))
             AND (:model IS NULL OR LOWER(v.model) LIKE LOWER(CONCAT('%', :model, '%')))
             AND (:category IS NULL OR LOWER(v.category) LIKE LOWER(CONCAT('%', :category, '%')))
             AND (:minPrice IS NULL OR v.price >= :minPrice)
             AND (:maxPrice IS NULL OR v.price <= :maxPrice)
             AND (:inStockOnly = FALSE OR v.quantity > 0)
           """)
    Page<Vehicle> search(@Param("make") String make,
                         @Param("model") String model,
                         @Param("category") String category,
                         @Param("minPrice") BigDecimal minPrice,
                         @Param("maxPrice") BigDecimal maxPrice,
                         @Param("inStockOnly") boolean inStockOnly,
                         Pageable pageable);
}
