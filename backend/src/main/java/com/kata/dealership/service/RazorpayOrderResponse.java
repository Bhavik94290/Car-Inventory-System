package com.kata.dealership.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class RazorpayOrderResponse {
    private String id;
    private Long amount;
    private String currency;
    private String status;
}
