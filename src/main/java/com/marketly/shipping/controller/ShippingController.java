package com.marketly.shipping.controller;

import com.marketly.shipping.model.QuoteRequest;
import com.marketly.shipping.model.QuoteResponse;
import com.marketly.shipping.service.ShippingService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * REST controller for the shipping-api.
 */
@RestController
@RequestMapping("/")
public class ShippingController {

    private final ShippingService shippingService;

    public ShippingController(ShippingService shippingService) {
        this.shippingService = shippingService;
    }

    /** POST /quote — compute a shipping quote for a single package. */
    @PostMapping("/quote")
    public ResponseEntity<QuoteResponse> quote(@Valid @RequestBody QuoteRequest req) {
        QuoteResponse resp = shippingService.calculateQuote(req);
        return ResponseEntity.ok(resp);
    }

    /** GET /health — liveness probe. */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "ok",
                "service", "shipping-api",
                "version", "0.1.0"));
    }

    /** GET /ready — readiness probe. */
    @GetMapping("/ready")
    public ResponseEntity<Map<String, String>> ready() {
        return ResponseEntity.ok(Map.of("status", "ready"));
    }
}
