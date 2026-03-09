# ── Stage 1: Build ──────────────────────────────────────────────────────────
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /workspace

COPY pom.xml .
COPY api-gateway/pom.xml api-gateway/pom.xml
COPY account-service/pom.xml account-service/pom.xml
COPY transfer-service/pom.xml transfer-service/pom.xml
COPY notification-service/pom.xml notification-service/pom.xml

RUN mvn dependency:go-offline -pl notification-service -am -q

COPY api-gateway/src api-gateway/src
COPY account-service/src account-service/src
COPY transfer-service/src transfer-service/src
COPY notification-service/src notification-service/src
RUN mvn package -pl notification-service -am -DskipTests -q

# ── Stage 2: Runtime ─────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-jammy
LABEL maintainer="Core Banking Platform Team"
LABEL org.opencontainers.image.title="Notification Service"
LABEL org.opencontainers.image.description="Kafka-driven customer notification dispatcher with audit logging"

WORKDIR /app
RUN groupadd --system cbp && useradd --system --gid cbp cbp
COPY --from=build /workspace/notification-service/target/*.jar app.jar
RUN chown cbp:cbp app.jar
USER cbp

EXPOSE 8083
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
