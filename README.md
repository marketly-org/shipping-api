# shipping-api

Shipping quote service for the **Marketly** e-commerce platform.

Calls an external carrier API to compute shipping costs for a single
package, then applies Marketly-specific rules (free-shipping threshold,
declared-value surcharge).

## Stack

- **Java 21** + **Spring Boot 3.3**
- Maven build

## Endpoints

| Method | Path | Description |
|--------|------|-------------|
| POST | `/quote` | Compute a shipping quote |
| GET | `/health` | Liveness probe |
| GET | `/ready` | Readiness probe |

## Local development

```bash
mvn spring-boot:run
# then:
curl -X POST http://localhost:8080/quote \
  -H 'Content-Type: application/json' \
  -d '{"fromZip":"10001","toZip":"94105","weightKg":2.5,"declaredValueCents":0}'
```

## Tests

```bash
mvn test
```

## Configuration

| Env var | Default | Description |
|---------|---------|-------------|
| `SHIPPING_CARRIER_URL` | `https://api.example-carrier.com/v1/quote` | Carrier quote API |
| `SHIPPING_CARRIER_API_KEY` | (empty) | Carrier bearer token |
| `SHIPPING_CARRIER_TIMEOUT_MS` | `3000` | Carrier call timeout |
| `SERVER_PORT` | `8080` | Listen port |
