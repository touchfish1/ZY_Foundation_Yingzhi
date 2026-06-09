FROM gradle:8.10-jdk21 AS build
WORKDIR /app

# Copy root build config
COPY build.gradle settings.gradle gradlew ./
COPY gradle ./gradle

# Copy common module
COPY common/build.gradle ./common/
COPY common/src ./common/src

# Copy this service
COPY api/build.gradle ./api/
COPY api/src ./api/src

# Build the service JAR (standalone executable, not the bootJar)
RUN ./gradlew :api:build -x test --no-daemon

FROM eclipse-temurin:21-jre
RUN groupadd -r appuser && useradd -r -g appuser -u 1000 appuser
WORKDIR /app
COPY --from=build /app/api/build/libs/*.jar app.jar
RUN chown -R appuser:appuser /app
USER appuser
EXPOSE 8088
ENV JAVA_OPTS="-Xms256m -Xmx768m"
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
