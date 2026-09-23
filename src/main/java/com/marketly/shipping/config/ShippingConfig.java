package com.marketly.shipping.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration for the shipping-api service.
 *
 * <p>Bound from {@code shipping.*} properties in {@code application.yml}.
 */
@ConfigurationProperties(prefix = "shipping")
public record ShippingConfig(
        String carrierUrl,
        String carrierApiKey,
        long carrierTimeoutMs,
        int defaultCentsPerKg,
        int freeShippingThresholdCents
) {
    public ShippingConfig {
        if (carrierUrl == null || carrierUrl.isBlank()) {
            carrierUrl = "https://api.example-carrier.com/v1/quote";
        }
        if (carrierApiKey == null) {
            carrierApiKey = "";
        }
        if (carrierTimeoutMs <= 0) {
            carrierTimeoutMs = 3000L;
        }
        if (defaultCentsPerKg <= 0) {
            defaultCentsPerKg = 250; // $2.50/kg fallback if carrier is unreachable
        }
        if (freeShippingThresholdCents <= 0) {
            freeShippingThresholdCents = 5_000; // $50.00
        }
    }
}
