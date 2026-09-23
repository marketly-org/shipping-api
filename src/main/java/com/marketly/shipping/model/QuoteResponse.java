package com.marketly.shipping.model;

/**
 * Response body for {@code POST /quote}.
 *
 * @param carrier          human-readable carrier name (e.g. "UPS Ground")
 * @param totalCents       total shipping cost in cents
 * @param currency         ISO 4217 currency code
 * @param estimatedDays    estimated delivery time in business days
 * @param trackingAvailable whether the carrier provides tracking for this lane
 */
public record QuoteResponse(
        String carrier,
        long totalCents,
        String currency,
        int estimatedDays,
        boolean trackingAvailable
) {
}
