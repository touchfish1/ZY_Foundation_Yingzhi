FROM gradle:8.10-jdk21 AS build
WORKDIR /app
COPY . .
RUN gradle :payment-service:build -x test --no-daemon

FROM eclipse-temurin:21-jre
RUN groupadd -r appuser && useradd -r -g appuser -u 1000 appuser
WORKDIR /app
COPY --from=build /app/payment-service/build/libs/*.jar app.jar
RUN chown -R appuser:appuser /app
USER appuser
EXPOSE 8084
ENV JAVA_OPTS="-Xms128m -Xmx256m"
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
