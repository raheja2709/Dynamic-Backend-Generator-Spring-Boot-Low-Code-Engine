# ============================================================
# Multi-stage Dockerfile for Dynamic Backend Generator
# Runtime image: JRE 17 + Maven (needed for BuildVerifier)
# Target: under 250MB runtime layer (excl. Maven cache)
# ============================================================

# --- Stage 1: Build ---
FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /app

# Cache dependencies first
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source and build
COPY src ./src
RUN mvn package -DskipTests -B

# --- Stage 2: Runtime ---
FROM eclipse-temurin:17-jre

# Install Maven for BuildVerifier (verifies generated projects compile)
RUN apt-get update && \
    apt-get install -y --no-install-recommends maven && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Copy built artifact
COPY --from=build /app/target/*.jar app.jar

# Create directories
RUN mkdir -p /app/generated-projects /app/logs

# Environment defaults
ENV SPRING_PROFILES_ACTIVE=docker
ENV APP_GENERATED_PROJECTS_DIR=/app/generated-projects
ENV APP_MAVEN_PATH=mvn
ENV JAVA_OPTS="-Xms256m -Xmx1024m"

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# Graceful shutdown (30s drain period)
STOPSIGNAL SIGTERM

# Run application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Dserver.shutdown=graceful -Dspring.lifecycle.timeout-per-shutdown-phase=30s -jar app.jar"]
