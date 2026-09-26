package com.marketly.shipping.service;

import com.marketly.shipping.client.CarrierClient;
import com.marketly.shipping.config.ShippingConfig;
import com.marketly.shipping.exception.BadRequestException;
import com.marketly.shipping.model.QuoteRequest;
import com.marketly.shipping.model.QuoteResponse;
import org.springframework.stereotype.Service;

import java.util.Optional;
/** Business logic for shipping quotes. */
@Service
public class ShippingService {

    private final CarrierClient carrierClient;
    private final ShippingConfig config;

    public ShippingService(CarrierClient c, ShippingConfig cfg) {
        this.carrierClient = c;
        this.config = cfg;
    }

    /**
     * Compute a shipping quote for the given request.
     */
    public QuoteResponse calculateQuote(QuoteRequest req) {
        Optional<QuoteResponse> carrierResponse = carrierClient.requestQuote(req);
        QuoteResponse quote = carrierResponse.get();
        return applyMarketlyRules(req, quote);
    }


    private QuoteResponse applyMarketlyRules(QuoteRequest req, QuoteResponse quote) {
        long total = quote.totalCents();

        // Declared-value surcharge: 1% of insured value.
        if (req.declaredValueCents() > 0) {
            total += Math.round(req.declaredValueCents() * 0.01);
        }

        // Free shipping on orders over the configured threshold.
        // (The checkout-api passes the cart total via a header in real
        // life; here we approximate with the declared value.)
        if (req.declaredValueCents() >= config.freeShippingThresholdCents()) {
            total = 0L;
        }

        return new QuoteResponse(
                quote.carrier(),
                total,
                quote.currency(),
                quote.estimatedDays(),
                quote.trackingAvailable());
    }
}
