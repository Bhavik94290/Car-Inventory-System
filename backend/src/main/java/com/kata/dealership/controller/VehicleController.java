package com.kata.dealership.controller;

import com.kata.dealership.dto.RestockRequest;
import com.kata.dealership.dto.VehicleRequest;
import com.kata.dealership.entity.Vehicle;
import com.kata.dealership.service.FileStorageService;
import com.kata.dealership.service.VehicleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/vehicles")
@RequiredArgsConstructor
public class VehicleController {

    private static final Set<String> SORTABLE_FIELDS = Set.of("createdAt", "make", "model", "category", "price", "quantity");
    private static final int MAX_PAGE_SIZE = 100;

    private final VehicleService vehicleService;
    private final FileStorageService fileStorageService;

    // ---- public ----
    @GetMapping
    public ResponseEntity<List<Vehicle>> getAll() {
        return ResponseEntity.ok(vehicleService.getAllVehicles());
    }

    @GetMapping("/search")
    public ResponseEntity<Page<Vehicle>> search(
            @RequestParam(required = false) String make,
            @RequestParam(required = false) String model,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(defaultValue = "false") boolean inStockOnly,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        String safeSortBy = SORTABLE_FIELDS.contains(sortBy) ? sortBy : "createdAt";
        Sort.Direction direction = "desc".equalsIgnoreCase(sortDir) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(
                Math.max(page, 0),
                Math.min(Math.max(size, 1), MAX_PAGE_SIZE),
                Sort.by(direction, safeSortBy));

        return ResponseEntity.ok(vehicleService.search(make, model, category, minPrice, maxPrice, inStockOnly, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Vehicle> getOne(@PathVariable String id) {
        return ResponseEntity.ok(vehicleService.getVehicle(id));
    }

    // ---- admin only (enforced in SecurityConfig) ----
    @PostMapping
    public ResponseEntity<Vehicle> add(@Valid @RequestBody VehicleRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(vehicleService.addVehicle(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Vehicle> update(@PathVariable String id, @Valid @RequestBody VehicleRequest request) {
        return ResponseEntity.ok(vehicleService.updateVehicle(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        vehicleService.deleteVehicle(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/upload-image")
    public ResponseEntity<Map<String, String>> uploadImage(@RequestParam("file") MultipartFile file) {
        String filename = fileStorageService.store(file);
        String url = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/uploads/vehicles/")
                .path(filename)
                .toUriString();
        return ResponseEntity.ok(Map.of("imageUrl", url));
    }

    @PostMapping("/{id}/restock")
    public ResponseEntity<Vehicle> restock(@PathVariable String id, @Valid @RequestBody RestockRequest request) {
        return ResponseEntity.ok(vehicleService.restock(id, request.getAmount()));
    }

    // ---- any logged-in user ----
    @PostMapping("/{id}/purchase")
    public ResponseEntity<Vehicle> purchase(@PathVariable String id) {
        return ResponseEntity.ok(vehicleService.purchase(id));
    }
}
