package com.kata.dealership.service;

import com.kata.dealership.exception.PaymentGatewayException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.Map;

/**
 * Thin wrapper around Razorpay's REST API (Orders + payment signature verification).
 * Uses RestTemplate directly rather than the razorpay-java SDK to avoid an extra
 * Maven dependency for what's otherwise two simple HTTP/crypto operations.
 */
@Service
public class RazorpayService {

    private static final String ORDERS_URL = "https://api.razorpay.com/v1/orders";

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${razorpay.key-id}")
    private String keyId;

    @Value("${razorpay.key-secret}")
    private String keySecret;

    public String getKeyId() {
        return keyId;
    }

    public RazorpayOrderResponse createOrder(long amountInSmallestUnit, String currency, String receipt) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBasicAuth(keyId, keySecret);

        Map<String, Object> body = Map.of(
                "amount", amountInSmallestUnit,
                "currency", currency,
                "receipt", receipt,
                "payment_capture", 1);

        try {
            var response = restTemplate.postForEntity(ORDERS_URL, new HttpEntity<>(body, headers), RazorpayOrderResponse.class);
            RazorpayOrderResponse order = response.getBody();
            if (order == null || order.getId() == null) {
                throw new PaymentGatewayException("Razorpay returned an empty order response");
            }
            return order;
        } catch (RestClientException ex) {
            throw new PaymentGatewayException("Could not create Razorpay order: " + ex.getMessage());
        }
    }

    public boolean verifySignature(String razorpayOrderId, String razorpayPaymentId, String razorpaySignature) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(keySecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] hash = mac.doFinal((razorpayOrderId + "|" + razorpayPaymentId).getBytes(StandardCharsets.UTF_8));
            String expected = HexFormat.of().formatHex(hash);
            return MessageDigest.isEqual(
                    expected.getBytes(StandardCharsets.UTF_8),
                    razorpaySignature.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            return false;
        }
    }
}
