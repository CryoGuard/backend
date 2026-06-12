# Multi-stage build for CryoGuard Backend
# Stage 1: Build with Maven
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copy dependency files first for better caching
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build
COPY src ./src
RUN mvn package -DskipTests -B

# Stage 2: Runtime with JRE
FROM eclipse-temurin:21-jre
WORKDIR /app

# Create directory for H2 database files
RUN mkdir -p /opt/h2-data

# Copy the built JAR from build stage
COPY --from=build /app/target/*.jar app.jar

# Expose the default port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "/app/app.jar"]