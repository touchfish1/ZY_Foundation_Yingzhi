# Build stage
FROM gradle:8.10-jdk21 AS builder
WORKDIR /app
COPY build.gradle settings.gradle ./
COPY src ./src
RUN gradle build -x test --no-daemon

# Runtime stage
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "-Xms128m", "-Xmx256m", "app.jar"]
