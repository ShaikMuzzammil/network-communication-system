# ============================================
# SocketComm Java File Transfer Service (TCP Sockets)
# ============================================

# Stage 1: Build with Maven
FROM maven:3.9-eclipse-temurin-17 AS builder

WORKDIR /build

# Copy POM first for dependency caching
COPY java-modules/file-transfer-service/pom.xml .

# Download dependencies (cached layer)
RUN mvn dependency:go-offline -B

# Copy source code
COPY java-modules/file-transfer-service/src ./src

# Build the application
RUN mvn clean package -DskipTests -B

# Stage 2: Runtime (JRE only)
FROM eclipse-temurin:17-jre-alpine

LABEL maintainer="SocketComm Team <team@socketcomm.dev>"
LABEL description="SocketComm TCP Socket File Transfer Service"
LABEL version="1.0.0"

# Install minimal dependencies
RUN apk add --no-cache curl tzdata \
    && cp /usr/share/zoneinfo/UTC /etc/localtime \
    && echo "UTC" > /etc/timezone

# Create non-root user
RUN addgroup -S socketcomm && adduser -S socketcomm -G socketcomm

WORKDIR /app

# Copy built artifact from builder
COPY --from=builder /build/target/*.jar app.jar

# Set permissions
RUN chown -R socketcomm:socketcomm /app

USER socketcomm

# Expose file transfer port
EXPOSE 5000/tcp

# JVM options for containerized environment
ENV JAVA_OPTS="-Xms128m -Xmx512m -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=10s --retries=3 \
    CMD curl -f http://localhost:5000 || exit 1

# Default entrypoint
ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar app.jar ${MODE} --port 5000"]
