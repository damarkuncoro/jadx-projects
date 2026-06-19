# Multi-stage build for DexForge CLI
# Stage 1: Build
FROM eclipse-temurin:25-jdk AS builder

WORKDIR /build

# Copy Gradle wrapper and build files
COPY gradlew .
COPY gradle/wrapper/gradle-wrapper.properties gradle/wrapper/
COPY build.gradle.kts .
COPY settings.gradle.kts settings.gradle.kts
COPY buildSrc/build.gradle.kts buildSrc/build.gradle.kts
COPY buildSrc/src/main/kotlin/ buildSrc/src/main/kotlin/

# Copy all subprojects
COPY dexforge-cli/ dexforge-cli/
COPY dexforge-commons/ dexforge-commons/
COPY jadx-core/ jadx-core/
COPY jadx-commons/ jadx-commons/
COPY jadx-plugins/ jadx-plugins/

# Build the CLI distribution
RUN ./gradlew :dexforge-cli:installShadowDist -x test --no-daemon

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-slim

LABEL maintainer="DexForge <https://github.com/damarkuncoro/jadx-projects>"
LABEL description="DexForge CLI - Android reverse engineering workbench powered by JADX"
LABEL version="dev"

# Install minimal dependencies
RUN apt-get update && apt-get install -y --no-install-recommends \
    bash \
    curl \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Copy distribution from builder
COPY --from=builder /build/dexforge-cli/build/install/dexforge-cli/bin/ /app/bin/
COPY --from=builder /build/dexforge-cli/build/install/dexforge-cli/lib/ /app/lib/

# Set environment variables
ENV PATH="/app/bin:${PATH}"
ENV DEXFORGE_HOME="/app"

# Create a non-root user
RUN useradd -m -u 1000 dexforge && \
    chown -R dexforge:dexforge /app
USER dexforge

# Expose LSP daemon port
EXPOSE 8080

# Default command
ENTRYPOINT ["/app/bin/dexforge"]
CMD ["--help"]
