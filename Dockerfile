# Use OpenJDK 21 slim image
FROM openjdk:21-jdk-slim

# Install AWS CLI and curl for health checks
RUN apt-get update && apt-get install -y \
    curl \
    unzip \
    && curl "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o "awscliv2.zip" \
    && unzip awscliv2.zip \
    && ./aws/install \
    && rm -rf awscliv2.zip aws \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/*

# Set working directory
WORKDIR /app

# Copy the JAR file and startup script
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