# Stage 1: Build the artifact
FROM maven:3.9.6-eclipse-temurin-17-alpine AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Create the production-grade image
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Senior Move: Create a non-root user for security compliance
RUN addgroup -S pipeline && adduser -S deployer -G pipeline
USER deployer

# Copy only the compiled JAR from the build stage
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

# Performance Tuning: Memory settings for container awareness
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
