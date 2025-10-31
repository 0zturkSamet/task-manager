# Multi-stage Dockerfile for Spring Boot Application
# Stage 1: Build stage
FROM maven:3.9.11-eclipse-temurin-17 AS build

# Set working directory
WORKDIR /app

# Copy pom.xml first for better layer caching
COPY pom.xml .

# Download dependencies (this layer will be cached if pom.xml doesn't change)
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application (skip tests for faster builds)
RUN mvn clean package -DskipTests

# Stage 2: Runtime stage
FROM eclipse-temurin:17-jre-jammy

# Set working directory
WORKDIR /app

# Copy the JAR file from build stage
COPY --from=build /app/target/task-manager-backend-1.0.0.jar app.jar

# Expose port (Render will override with $PORT)
EXPOSE 8080

# Set JVM options for optimal performance in containerized environment
ENV JAVA_OPTS="-Xmx512m -Xms256m"

# Run the application
# Use shell form to allow environment variable substitution
ENTRYPOINT java $JAVA_OPTS -Dserver.port=${PORT:-8080} -jar app.jar
