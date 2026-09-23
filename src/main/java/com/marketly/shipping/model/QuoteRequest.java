package com.marketly.shipping.model;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * Request body for {@code POST /quote}.
 *
 * @param fromZip        origin postal code
 * @param toZip          destination postal code
 * @param weightKg       package weight in kilograms
 * @param declaredValueCents  optional insured value in cents (0 = uninsured)
 */
public record QuoteRequest(
        @NotBlank String fromZip,
        @NotBlank String toZip,
        @NotNull @DecimalMin(value = "0.001", message = "weight must be > 0") BigDecimal weightKg,
        long declaredValueCents
) {
}
