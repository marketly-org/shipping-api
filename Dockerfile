# syntax=docker/dockerfile:1.6

# ---- Build stage ----
FROM maven:3.9-eclipse-temurin-21 AS builder

WORKDIR /build

# Pre-fetch dependencies for better layer caching.
COPY pom.xml ./
RUN mvn -B -q dependency:go-offline

COPY src/ ./src/

RUN mvn -B -q clean package -DskipTests

# ---- Runtime stage ----
FROM eclipse-temurin:21-jre AS runtime

RUN groupadd -r app && useradd -r -g app -d /app app

WORKDIR /app

COPY --from=builder /build/target/shipping-api-*.jar /app/shipping-api.jar

USER app

EXPOSE 8080

ENV JAVA_OPTS="-XX:MaxRAMPercentage=75 -XX:+ExitOnOutOfMemoryError"
ENV SERVER_PORT=8080

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/shipping-api.jar"]
