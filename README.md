# Core Banking Platform

A highly scalable, distributed enterprise system representing an event-driven Microservices Architecture driven by Apache Kafka, adhering to the Database-per-Service pattern and Hexagonal Architecture.

## System Architecture

The ecosystem consists of the following components:
1. **API Gateway**: Single entry point handling cross-cutting concerns, routing, and Redis rate limiting.
2. **Account Service**: Manages customer accounts, balances, and handles deposits/withdrawals. Strictly adheres to Hexagonal Architecture.
3. **Transfer Service**: Initiates distributed transactions (money transfers). Utilizes the **Saga Choreography Pattern**, **Transactional Outbox Pattern**, and strict **Idempotency** via Redis to ensure safe distributed processing.
4. **Notification Service**: Listens to the Saga outcome (Kafka events) and persists audit notifications.

### Patterns Implemented
- **Hexagonal Architecture (Ports and Adapters)**: Segregating domain logic from infrastructure details.
- **Transactional Outbox Pattern**: Saving state changes and related domain events in a single local transaction, then polling them to Kafka.
- **Choreography-based Saga Pattern**: Distributed transactions mediated purely via Kafka messaging.
- **Idempotency**: Leveraging Redis to safely reject duplicate transfer requests.

## How to Run the Ecosystem

### 1. Start the Infrastructure
The entire infrastructure (Kafka, Zookeeper, PostgreSQL instances, Redis, Zipkin, Prometheus, Grafana) is defined in a single Docker Compose file.
Ensure you have Docker installed, then run:
```bash
docker compose up -d
```

### 2. Start the Microservices
You can run each Spring Boot microservice individually. Using Maven from the root directory:
```bash
mvn clean install -DskipTests
```
Then start each service using the Spring Boot plugin or running the generated jar:
```bash
cd api-gateway && mvn spring-boot:run
cd account-service && mvn spring-boot:run
cd transfer-service && mvn spring-boot:run
cd notification-service && mvn spring-boot:run
```

Alternatively, you could build docker images for the microservices and add them to the docker-compose file for a one-click startup.

## How to Test the Ecosystem

### Unit and Integration Tests
The services use JUnit 5 and Testcontainers to spin up ephemeral PostgreSQL, Kafka, and Redis containers to perform real integration tests.

To run the tests for the entire project, execute:
```bash
mvn clean test
```
*Note: Ensure Docker is running in your host, as Testcontainers requires access to the Docker daemon.*

### Manual Testing

1. **Create an Account:**
```bash
curl -X POST http://localhost:8080/api/v1/accounts \
  -H "Content-Type: application/json" \
  -d '{"customerId": "CUST-001"}'
```

2. **Wait for Services to Sync & then Initiate a Transfer (Saga):**
```bash
curl -X POST http://localhost:8080/api/v1/transfers \
  -H "Idempotency-Key: uuid-1234-5678" \
  -H "Content-Type: application/json" \
  -d '{
    "sourceAccountId": "<UUID from Account 1>",
    "targetAccountId": "<UUID from Account 2>",
    "amount": 100.50
  }'
```

3. **Verify Distributed Tracing:**
Open Zipkin at `http://localhost:9411` to visualize the complete trace of your transfer request as it hops from Gateway -> Transfer Service -> Kafka -> Account Service -> Kafka -> Notification Service.

4. **Verify Metrics:**
Open Grafana at `http://localhost:3000` (admin/admin) or Prometheus at `http://localhost:9090` to view health and custom Micrometer metrics.
