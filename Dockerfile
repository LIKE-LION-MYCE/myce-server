# Multi-stage build for optimized production image
# Stage 1: Build environment with AWS CLI (temporary)
FROM eclipse-temurin:21-jdk-jammy AS builder

# Install AWS CLI and build tools in builder stage only
RUN apt-get update && apt-get install -y \
    curl \
    unzip \
    && curl "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o "awscliv2.zip" \
    && unzip awscliv2.zip \
    && ./aws/install \
    && rm -rf awscliv2.zip aws \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/*

# Stage 2: Runtime image (final, optimized)
FROM eclipse-temurin:21-jre-jammy

# Install only essential runtime dependencies
RUN apt-get update && apt-get install -y \
    curl \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/*

# Copy AWS CLI from builder stage (only necessary components)
COPY --from=builder /usr/local/aws-cli /usr/local/aws-cli
COPY --from=builder /usr/local/bin/aws /usr/local/bin/aws

# Set working directory
WORKDIR /app

# Copy application files
COPY build/libs/*.jar app.jar
COPY startup.sh startup.sh

# Make startup script executable
RUN chmod +x startup.sh

# Create a non-root user for security
RUN addgroup --system spring && adduser --system spring --ingroup spring
USER spring:spring

# Expose port 8080
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# Run the application using startup script
ENTRYPOINT ["./startup.sh"]