# Multi-stage build for DexForge CLI
# Stage 1: Build
FROM eclipse-temurin:25-jdk AS builder

WORKDIR /build

# Copy entire project (excluding .dockerignore patterns)
COPY . .

# Build the CLI distribution
RUN ./gradlew :dexforge-cli:installShadowDist -x test --no-daemon

# Stage 2: Runtime
FROM eclipse-temurin:21-jre

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
