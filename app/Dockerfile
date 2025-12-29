
FROM maven:3.9.6-eclipse-temurin-17-alpine AS build
WORKDIR /build

COPY app/pom.xml .
COPY app/src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

RUN addgroup -S taskmastergroup && adduser -S taskmasteruser -G taskmastergroup
USER taskmasteruser

COPY --from=build /build/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
