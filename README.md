<div align="center">

# Core Banking Platform

**A production-grade, event-driven microservices system built with *Java 21*, *Spring Boot 3.3*, and *Apache Kafka*.**  
Implements Hexagonal Architecture (Ports & Adapters), Choreography-based Saga Pattern, Transactional Outbox, and Database-per-Service pattern.

[![Java](https://img.shields.io/badge/Java-21-orange?logo=openjdk)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3-brightgreen?logo=springboot)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue?logo=postgresql)](https://www.postgresql.org/)
[![Kafka](https://img.shields.io/badge/Kafka-3.7-orange?logo=apache-kafka)](https://kafka.apache.org/)
[![Redis](https://img.shields.io/badge/Redis-7-red?logo=redis)](https://redis.io/)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

</div>

---

## Table of Contents

- [Architecture Overview](#architecture-overview)
- [Design Patterns](#design-patterns)
- [Technology Stack](#technology-stack)
- [Project Structure](#project-structure)
- [Getting Started](#getting-started)
- [API Reference](#api-reference)
- [Running Tests](#running-tests)
- [Observability](#observability)
- [Generating JavaDocs](#generating-javadocs)

---

## Architecture Overview

```
                       ┌─────────────────────────────────────────────┐
                       │               Clients / curl                │
                       └──────────────────────┬──────────────────────┘
                                              │ HTTP
                       ┌──────────────────────▼──────────────────────┐
                       │              API Gateway :8080              │
                       │  Rate Limiting · Circuit Breaker · Routing  │
                       └────────┬─────────────────────────┬──────────┘
                                │ HTTP                    │ HTTP
               ┌────────────────▼─────────┐    ┌──────────▼────────────────┐
               │   Account Service :8081  │    │  Transfer Service :8082   │
               │  Hexagonal Architecture  │    │  Saga Initiator           │
               │  PostgreSQL (account_db) │    │  Redis Idempotency        │
               └───────────┬──────────────┘    │  PostgreSQL (transfer_db) │
                           │ Kafka             └────────────┬──────────────┘
           ┌───────────────▼────────────────────────────────▼──────────────┐
           │                         Apache Kafka                          │
           │   transfer.requested · transfer.completed · transfer.failed   │
           └──────────────────────────────┬────────────────────────────────┘
                                          │
                          ┌───────────────▼──────────────────┐
                          │   Notification Service :8083     │
                          │   Event-driven audit log         │
                          │   PostgreSQL (notification_db)   │
                          └──────────────────────────────────┘
```

### Money Transfer Flow (Choreography-based Saga)

```
Client          Transfer Service          Kafka                Account Service         Notification Service
  │                    │                    │                        │                         │
  │──POST /transfers──▶│                    │                        │                         │
  │                    │─save PENDING──────▶DB                       │                         │
  │                    │──publish──────────▶transfer.requested       │                         │
  │◀─202 Accepted──────│                    │                        │                         │
  │                    │                    │──consume──────────────▶│                         │
  │                    │                    │                        │─debit source            │
  │                    │                    │                        │─credit target           │
  │                    │                    │◀─publish───────────────transfer.completed        │
  │                    │◀─consume───────────│                        │                         │
  │                    │─update COMPLETED──▶DB                       │                         │
  │                    │                    │──consume────────────────────────────────────────▶│
  │                    │                    │                        │─save notification────────▶DB
```

---

## Design Patterns

| Pattern | Where Applied |
|---|---|
| **Hexagonal Architecture** | `account-service`, `transfer-service` — domain layer is fully decoupled from Spring, JPA, and Kafka |
| **Choreography-based Saga** | Transfer flow across `transfer-service` and `account-service` via Kafka events |
| **Transactional Outbox** | Domain state and outbound events are written in the same DB transaction |
| **Database-per-Service** | Each service has its own isolated PostgreSQL database |
| **Idempotency Keys** | `transfer-service` stores keys in Redis (24h TTL) to prevent duplicate Sagas |
| **Optimistic Concurrency** | `@Version` column on all mutable JPA entities prevents lost updates |
| **Circuit Breaker** | `api-gateway` wraps downstream calls with Resilience4j circuit breakers |
| **Rate Limiting** | Token-bucket rate limiting via Spring Cloud Gateway + Redis |
| **RFC 7807 Problem Details** | All error responses follow the standardised Problem Detail format |

---

## Technology Stack

| Concern | Technology |
|---|---|
| Language | Java 21 (Virtual Threads) |
| Framework | Spring Boot 3.3, Spring Cloud 2023.0 |
| API Gateway | Spring Cloud Gateway |
| Messaging | Apache Kafka 7.6 (Confluent) |
| Databases | PostgreSQL 16 (one per service) |
| Cache / Idempotency | Redis 7.2 |
| Persistence | Spring Data JPA + Flyway |
| Observability | Micrometer + Prometheus + Grafana |
| Distributed Tracing | OpenTelemetry + Zipkin |
| API Documentation | SpringDoc OpenAPI 3 (Swagger UI) |
| Testing | JUnit 5, Mockito, Testcontainers |
| Build | Maven 3.9 (multi-module) |
| Containerisation | Docker + Docker Compose |

---

## Project Structure

```
core-banking-platform/
├── pom.xml                          # Parent POM — dependency management
├── docker-compose.yml               # Full local stack (infra + services)
├── infra/
│   ├── prometheus/prometheus.yml    # Prometheus scrape config
│   └── grafana/provisioning/        # Grafana datasource provisioning
│
├── api-gateway/                     # Spring Cloud Gateway (port 8080)
│   └── src/main/java/com/cbp/gateway/
│       ├── ApiGatewayApplication.java
│       ├── config/GatewayConfig.java        # Rate limiter + key resolver beans
│       └── controller/FallbackController.java
│
├── account-service/                 # Hexagonal Architecture (port 8081)
│   └── src/main/java/com/cbp/account/
│       ├── domain/
│       │   ├── model/               # Account, AccountStatus, InsufficientFundsException
│       │   ├── event/               # DomainEvent, TransferCompletedEvent, TransferFailedEvent
│       │   └── port/                # in: AccountUseCase | out: AccountRepositoryPort, EventPublisherPort
│       ├── application/service/     # AccountService (orchestration, no business logic)
│       ├── adapter/
│       │   ├── in/web/              # REST: AccountController, DTOs, GlobalExceptionHandler
│       │   ├── in/messaging/        # Kafka: TransferRequestedEventListener
│       │   ├── out/persistence/     # JPA: AccountJpaEntity, AccountPersistenceAdapter
│       │   └── out/messaging/       # Kafka: KafkaEventPublisherAdapter
│       └── config/KafkaConfig.java
│
├── transfer-service/                # Saga initiator (port 8082)
│   └── src/main/java/com/cbp/transfer/
│       ├── domain/
│       │   ├── model/               # Transfer, TransferStatus
│       │   ├── event/               # DomainEvent, TransferRequestedEvent
│       │   └── port/                # in: TransferUseCase | out: TransferRepositoryPort, EventPublisherPort, IdempotencyPort
│       ├── application/service/     # TransferService
│       ├── adapter/
│       │   ├── in/web/              # REST: TransferController, DTOs, GlobalExceptionHandler
│       │   ├── in/messaging/        # Kafka: TransferOutcomeEventListener
│       │   ├── out/persistence/     # JPA: TransferJpaEntity, TransferPersistenceAdapter
│       │   ├── out/messaging/       # Kafka: KafkaEventPublisherAdapter
│       │   └── out/cache/           # Redis: RedisIdempotencyAdapter
│       └── config/KafkaConfig.java
│
└── notification-service/            # Audit log + notifications (port 8083)
    └── src/main/java/com/cbp/notification/
        ├── domain/model/            # Notification, NotificationType, NotificationStatus
        ├── application/             # NotificationService
        ├── adapter/
        │   ├── messaging/           # Kafka: TransferOutcomeEventListener
        │   └── persistence/         # JPA: NotificationJpaEntity, repository
        └── config/KafkaConfig.java
```

---

## Getting Started

### Prerequisites

- Docker 24+ and Docker Compose v2
- Java 21+ (for running tests locally without Docker)
- Maven 3.9+

### Run the full stack

```bash
# Clone the repository
git clone https://github.com/fabiankaraben/core-banking-platform.git
cd core-banking-platform

# Start everything (infrastructure + all microservices)
docker compose up --build

# Or start only the infrastructure (useful during development)
docker compose up zookeeper kafka redis account-db transfer-db notification-db zipkin
```

**Service URLs after startup:**

| Service | URL |
|---|---|
| API Gateway | http://localhost:8080 |
| Account Service (direct) | http://localhost:8081 |
| Transfer Service (direct) | http://localhost:8082 |
| Notification Service (health) | http://localhost:8083/actuator/health |
| Swagger UI — Accounts | http://localhost:8081/swagger-ui.html |
| Swagger UI — Transfers | http://localhost:8082/swagger-ui.html |
| Kafka UI | http://localhost:9093 |
| Zipkin (Tracing) | http://localhost:9411 |
| Prometheus | http://localhost:9090 |
| Grafana | http://localhost:3000 (admin / admin) |

To stop and remove containers:

```bash
docker compose down
```

To stop and also remove all persistent volumes:

```bash
docker compose down -v

---

## API Reference

All examples route through the **API Gateway** on port `8080`.

### Account Service

#### Create an account

```bash
curl -s -X POST http://localhost:8080/api/v1/accounts \
  -H "Content-Type: application/json" \
  -d '{"ownerName": "Alice Smith", "currency": "USD"}' | jq .
```

```json
{
  "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "accountNumber": "CBP-A1B2-C3D4-E5F6",
  "ownerName": "Alice Smith",
  "balance": 0.00,
  "currency": "USD",
  "status": "ACTIVE",
  "createdAt": "2024-01-15T10:00:00Z"
}
```

#### Deposit funds

```bash
curl -s -X POST http://localhost:8080/api/v1/accounts/{accountId}/deposit \
  -H "Content-Type: application/json" \
  -d '{"amount": 500.00}' | jq .
```

#### Get account by ID

```bash
curl -s http://localhost:8080/api/v1/accounts/{accountId} | jq .
```

#### List all accounts

```bash
curl -s http://localhost:8080/api/v1/accounts | jq .
```

---

### Transfer Service

#### Initiate a money transfer

The `Idempotency-Key` header is **required**. Use the same key to safely replay a failed request — the server returns the original response without creating a duplicate transfer.

```bash
curl -s -X POST http://localhost:8080/api/v1/transfers \
  -H "Content-Type: application/json" \
  -H "Idempotency-Key: $(uuidgen)" \
  -d '{
    "sourceAccountId": "<source-account-uuid>",
    "targetAccountId": "<target-account-uuid>",
    "amount": 100.00,
    "currency": "USD",
    "description": "Rent payment for March"
  }' | jq .
```

```json
{
  "id": "7c9e6679-7425-40de-944b-e07fc1f90ae7",
  "sourceAccountId": "...",
  "targetAccountId": "...",
  "amount": 100.00,
  "currency": "USD",
  "status": "PENDING",
  "description": "Rent payment for March",
  "failureReason": null,
  "createdAt": "2024-01-15T10:05:00Z",
  "updatedAt": "2024-01-15T10:05:00Z"
}
```

The transfer is accepted immediately with status `PENDING`. Poll the transfer endpoint to observe the asynchronous outcome:

#### Poll transfer status

```bash
curl -s http://localhost:8080/api/v1/transfers/{transferId} | jq .status
# "COMPLETED" or "FAILED"
```

#### List all transfers

```bash
curl -s http://localhost:8080/api/v1/transfers | jq .
```

---

### Full end-to-end example

```bash
# 1. Create source account
SOURCE=$(curl -s -X POST http://localhost:8080/api/v1/accounts \
  -H "Content-Type: application/json" \
  -d '{"ownerName": "Alice", "currency": "USD"}' | jq -r .id)

# 2. Create target account
TARGET=$(curl -s -X POST http://localhost:8080/api/v1/accounts \
  -H "Content-Type: application/json" \
  -d '{"ownerName": "Bob", "currency": "USD"}' | jq -r .id)

# 3. Deposit funds into source account
curl -s -X POST http://localhost:8080/api/v1/accounts/$SOURCE/deposit \
  -H "Content-Type: application/json" \
  -d '{"amount": 1000.00}' | jq .balance

# 4. Initiate transfer
TRANSFER=$(curl -s -X POST http://localhost:8080/api/v1/transfers \
  -H "Content-Type: application/json" \
  -H "Idempotency-Key: $(uuidgen)" \
  -d "{\"sourceAccountId\":\"$SOURCE\",\"targetAccountId\":\"$TARGET\",\"amount\":250.00,\"currency\":\"USD\"}" \
  | jq -r .id)

# 5. Wait and check outcome
sleep 2
curl -s http://localhost:8080/api/v1/transfers/$TRANSFER | jq '{status, failureReason}'

# 6. Verify balances
curl -s http://localhost:8080/api/v1/accounts/$SOURCE | jq .balance  # 750.00
curl -s http://localhost:8080/api/v1/accounts/$TARGET | jq .balance  # 250.00
```

---

## Running Tests

### Unit tests only (no Docker required)

```bash
./mvnw test
```

### All tests for a specific module

```bash
./mvnw test -pl account-service
./mvnw test -pl transfer-service
./mvnw test -pl notification-service
```

### Full build with tests

```bash
./mvnw verify
```

### Test coverage summary

| Module | Test type | Coverage focus |
|---|---|---|
| `account-service` | Unit (Mockito) | `Account` domain rules, `AccountService` orchestration |
| `transfer-service` | Unit (Mockito) | `Transfer` state machine, `TransferService` idempotency + saga |
| `notification-service` | Unit (Mockito) | `NotificationService` message rendering and persistence |
| `api-gateway` | Spring context | Context loads, smoke test |

---

## Observability

### Health checks

```bash
curl http://localhost:8081/actuator/health
curl http://localhost:8082/actuator/health
curl http://localhost:8083/actuator/health
```

### Distributed Tracing

Every request is automatically instrumented with OpenTelemetry. Trace IDs are propagated through the API Gateway → microservices → Kafka events. Visualise traces at **http://localhost:9411** (Zipkin).

### Metrics

Prometheus scrapes all services at `/actuator/prometheus`. Open **http://localhost:3000** (Grafana) and query metrics such as:

```promql
# HTTP request rate by service
rate(http_server_requests_seconds_count[1m])

# Kafka consumer lag
kafka_consumer_fetch_manager_records_lag

# JVM heap usage
jvm_memory_used_bytes{area="heap"}
```

---

## Generating JavaDocs

```bash
# Generate aggregated Javadoc for all modules
./mvnw javadoc:aggregate

# Output location
open target/reports/apidocs/index.html
```

---

## Key Design Decisions

**Why Hexagonal Architecture?**  
The domain model (e.g., `Account`, `Transfer`) contains zero framework imports. This means business rules can be unit-tested at near-zero cost, and infrastructure (database, Kafka, Redis) can be swapped without touching a single line of domain code.

**Why Choreography over Orchestration?**  
Each service reacts to events autonomously. There is no central saga orchestrator that becomes a single point of failure. The Account Service and Notification Service are both decoupled consumers — adding a new outcome handler requires no changes to existing services.

**Why Idempotency Keys on transfers?**  
HTTP is inherently unreliable. A client that doesn't receive a `202` response may retry. Without idempotency enforcement the same transfer could be initiated twice, double-debiting the customer. Redis-backed idempotency keys with a 24-hour TTL prevent this at the service boundary.

**Why Optimistic Locking?**  
Banking accounts can be debited by multiple concurrent transfers (e.g., standing orders and manual transfers hitting the same account simultaneously). Optimistic locking detects write conflicts at commit time and surfaces them as retryable errors, avoiding deadlocks common with pessimistic locking.
