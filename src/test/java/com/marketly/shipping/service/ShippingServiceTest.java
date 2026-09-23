package com.marketly.shipping.service;

import com.marketly.shipping.client.CarrierClient;
import com.marketly.shipping.config.ShippingConfig;
import com.marketly.shipping.exception.BadRequestException;
import com.marketly.shipping.model.QuoteRequest;
import com.marketly.shipping.model.QuoteResponse;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link ShippingService}.
 *
 * <p>NOTE: These tests cover the happy path only. The bug (calling
 * {@code Optional.get()} on an empty Optional) is a runtime behaviour
 * that only triggers when the carrier returns an empty Optional — e.g.
 * for an unsupported postal code. Sentinel should fix the bug and then
 * ADD a test for that case (see the disabled test at the bottom).
 */
class ShippingServiceTest {

    private final CarrierClient carrierClient = mock(CarrierClient.class);
    private final ShippingConfig config = new ShippingConfig(
            "https://example.com", "key", 3000L, 250, 5000);
    private final ShippingService service = new ShippingService(carrierClient, config);

    @Test
    void calculateQuote_returnsCarrierQuoteOnHappyPath() {
        QuoteRequest req = new QuoteRequest("10001", "94105", new BigDecimal("2.5"), 0L);
        when(carrierClient.requestQuote(any()))
                .thenReturn(Optional.of(new QuoteResponse("UPS Ground", 1500L, "USD", 4, true)));

        QuoteResponse resp = service.calculateQuote(req);

        assertThat(resp.carrier()).isEqualTo("UPS Ground");
        assertThat(resp.totalCents()).isEqualTo(1500L);
        assertThat(resp.estimatedDays()).isEqualTo(4);
    }

    @Test
    void calculateQuote_appliesDeclaredValueSurcharge() {
        // Declared value stays below the 5000 free-shipping threshold so
        // the surcharge is actually visible in the total.
        QuoteRequest req = new QuoteRequest("10001", "94105", new BigDecimal("1.0"), 4000L);
        when(carrierClient.requestQuote(any()))
                .thenReturn(Optional.of(new QuoteResponse("UPS Ground", 1000L, "USD", 4, true)));

        QuoteResponse resp = service.calculateQuote(req);

        // 1000 base + 1% of 4000 = 40 surcharge
        assertThat(resp.totalCents()).isEqualTo(1040L);
    }

    @Test
    void calculateQuote_appliesFreeShippingOverThreshold() {
        QuoteRequest req = new QuoteRequest("10001", "94105", new BigDecimal("1.0"), 60000L);
        when(carrierClient.requestQuote(any()))
                .thenReturn(Optional.of(new QuoteResponse("UPS Ground", 1000L, "USD", 4, true)));

        QuoteResponse resp = service.calculateQuote(req);

        // 60000 >= 5000 threshold → free shipping
        assertThat(resp.totalCents()).isZero();
    }

    @Test
    void calculateQuote_rejectsBlankDestinationZip() {
        QuoteRequest req = new QuoteRequest("10001", "  ", new BigDecimal("1.0"), 0L);

        assertThatThrownBy(() -> service.calculateQuote(req))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("to_zip");
    }

    /*
     * DISABLED — this is the test Sentinel should ENABLE after fixing
     * the bug. With the current (buggy) code, carrierClient returns an
     * empty Optional and `carrierResponse.get()` throws
     * NoSuchElementException (a 500), not a BadRequestException (400).
     *
     * @Test
     * void calculateQuote_returns400WhenCarrierHasNoQuoteForLane() {
     *     QuoteRequest req = new QuoteRequest("10001", "00000",
     *             new BigDecimal("1.0"), 0L);
     *     when(carrierClient.requestQuote(any())).thenReturn(Optional.empty());
     *
     *     assertThatThrownBy(() -> service.calculateQuote(req))
     *             .isInstanceOf(BadRequestException.class)
     *             .hasMessageContaining("carrier returned no quote");
     * }
     */
}
