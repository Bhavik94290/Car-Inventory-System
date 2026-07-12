package com.kata.dealership.dto;

import com.kata.dealership.entity.Vehicle;
import lombok.*;

import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ChatResponse {
    private String reply;
    private List<Vehicle> vehicles;
}
