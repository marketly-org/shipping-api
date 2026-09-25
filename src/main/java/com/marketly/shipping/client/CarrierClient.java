package com.marketly.shipping.client;

import com.marketly.shipping.config.ShippingConfig;
import com.marketly.shipping.model.QuoteRequest;
import com.marketly.shipping.model.QuoteResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Optional;

/**
 * Thin wrapper around the external carrier quote API.
 *
 * <p>The carrier returns {@code 200} with a JSON body for lanes it
 * supports, and {@code 204 No Content} (or an empty body) for lanes it
 * does not — e.g. PO Boxes, APO/FPO addresses, or international postal
 * codes the carrier has no rate card for. In that case this client
 * returns {@link Optional#empty()}.
 */
@Component
public class CarrierClient {

    private static final Logger log = LoggerFactory.getLogger(CarrierClient.class);

    private final ShippingConfig config;
    private final RestClient restClient;

    public CarrierClient(ShippingConfig config) {
        this.config = config;
        this.restClient = RestClient.builder()
                .baseUrl(config.carrierUrl())
                .requestFactory(new org.springframework.http.client.SimpleClientHttpRequestFactory() {
                    {
                        setConnectTimeout((int) Duration.ofMillis(config.carrierTimeoutMs()).toMillis());
                        setReadTimeout((int) Duration.ofMillis(config.carrierTimeoutMs()).toMillis());
                    }
                })
                .build();
    }

    /**
     * Ask the carrier for a quote.
     *
     * @return the quote, or {@link Optional#empty()} if the carrier
     *         does not serve the requested lane.
     */
    public Optional<QuoteResponse> requestQuote(QuoteRequest req) {
        if (config.carrierApiKey() == null || config.carrierApiKey().isBlank()) {
            log.warn("carrier API key not configured; skipping carrier quote");
            return Optional.empty();
        }
        try {
            CarrierQuoteResponse resp = restClient.post()
                    .header("Authorization", "Bearer " + config.carrierApiKey())
                    .body(new CarrierQuoteRequest(req.fromZip(), req.toZip(),
                            req.weightKg().doubleValue(), req.declaredValueCents()))
                    .retrieve()
                    .body(CarrierQuoteResponse.class);
            if (resp == null || resp.carrier() == null) {
                return Optional.empty();
            }
            return Optional.of(new QuoteResponse(
                    resp.carrier(),
                    resp.totalCents(),
                    resp.currency() == null ? "USD" : resp.currency(),
                    resp.estimatedDays() <= 0 ? 5 : resp.estimatedDays(),
                    resp.trackingAvailable()));
        } catch (Exception e) {
            log.warn("carrier quote call failed for {} -> {}: {}",
                    req.fromZip(), req.toZip(), e.getMessage());
            return Optional.empty();
        }
    }

    /** Wire format for the carrier POST body. */
    private record CarrierQuoteRequest(
            String fromZip, String toZip,
            double weightKg, long declaredValueCents) {
    }

    /** Wire format for the carrier response body. */
    private record CarrierQuoteResponse(
            String carrier, long totalCents, String currency,
            int estimatedDays, boolean trackingAvailable) {
    }

    /**
     * A deterministic in-process fallback used only when no carrier URL
     * is configured (e.g. unit tests). Computes a flat per-kg rate.
     */
    public QuoteResponse fallback(QuoteRequest req) {
        long cents = Math.round(
                new BigDecimal(config.defaultCentsPerKg())
                        .multiply(req.weightKg())
                        .doubleValue());
        return new QuoteResponse("Marketly Standard", cents, "USD", 7, true);
    }
}
