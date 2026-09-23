package com.marketly.shipping;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * Entry point for the shipping-api service.
 *
 * <p>Calculates shipping quotes by calling an external carrier API
 * (e.g. UPS / FedEx / Shippo). The carrier URL is configurable via
 * {@code shipping.carrier-url} in {@code application.yml}.
 */
@SpringBootApplication
@ConfigurationPropertiesScan
public class ShippingApplication {

    public static void main(String[] args) {
        SpringApplication.run(ShippingApplication.class, args);
    }
}
