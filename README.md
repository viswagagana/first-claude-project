# Payment System – Production-ready Spring Boot Microservices

Monorepo for a **payment system** built with Spring Boot microservices: Auth, Account, Payment (orchestrator), Ledger, and Notification. Uses **Eureka** for discovery, **Kafka** for events, **PostgreSQL** and **MongoDB**, with **Stripe** and **PayPal** gateways. Designed for **Apigee** as the API gateway.

## Features

- **Auth Service:** JWT issue/refresh, register, audit log
- **Account Service:** Users, accounts, payment method references (Stripe/PayPal), multi-currency ready
- **Payment Service:** Orchestrator for authorize/capture, refunds (full/partial), idempotency, Stripe + PayPal, **transactional outbox**, **webhooks**, **audit**, **admin API**, **reconciliation job**
- **Ledger Service:** Balances and ledger entries; consumes payment/refund events (Kafka), **idempotent**, **DLQ**
- **Notification Service:** Email/SMS (stub); consumes events from Kafka, **idempotent**, **DLQ**
- **OpenAPI 3 + Swagger UI** per service
- **DB scripts:** PostgreSQL DDL and MongoDB init scripts
- **Docker Compose** for local run
- **Docs:** Apigee setup, database, ADRs, runbooks; **Postman** collection

## Prerequisites

- Java 17+
- Maven 3.8+
- Docker & Docker Compose (for full stack)

## Build

```bash
mvn clean package -DskipTests
```

## Run with Docker Compose

1. Build and start:

```bash
mvn clean package -DskipTests
docker-compose up -d
```

2. Endpoints (when using default ports):

| Service        | URL                        | Swagger UI                    |
|----------------|----------------------------|-------------------------------|
| Eureka         | http://localhost:8761      | -                             |
| Auth           | http://localhost:8081      | http://localhost:8081/swagger-ui.html |
| Account        | http://localhost:8082      | http://localhost:8082/swagger-ui.html |
| Payment        | http://localhost:8083      | http://localhost:8083/swagger-ui.html |
| Ledger         | http://localhost:8084      | http://localhost:8084/swagger-ui.html |
| Notification   | http://localhost:8085      | http://localhost:8085/swagger-ui.html |

3. Register and login (Auth):

```bash
curl -X POST http://localhost:8081/api/v1/register \
  -H "Content-Type: application/json" \
  -d '{"username":"test","email":"test@example.com","password":"password123"}'

curl -X POST http://localhost:8081/api/v1/login \
  -H "Content-Type: application/json" \
  -d '{"username":"test","password":"password123"}'
```

Use the returned `accessToken` as `Authorization: Bearer <accessToken>` and set `X-User-Id` to the user id (from JWT `sub`) for Account, Payment, and Ledger APIs.

## Configuration (env vars)

- **JWT_SECRET** – Auth signing secret (min 32 chars); required in production
- **STRIPE_SECRET_KEY**, **STRIPE_WEBHOOK_SECRET** – Stripe API and webhook verification
- **PAYPAL_CLIENT_ID** – PayPal (optional; stub used if not set)
- **POSTGRES_*** / **MONGODB_URI** – Override if not using Docker Compose defaults
- **KAFKA_BOOTSTRAP_SERVERS** – e.g. `localhost:9092` or `kafka:29092` in Docker
- **EUREKA_URI** – e.g. `http://localhost:8761/eureka/`

## API Gateway (Apigee)

Use **Apigee** as the single entry point. See [docs/apigee-setup.md](docs/apigee-setup.md) for routing, JWT validation, and target URLs.

## Database scripts

- PostgreSQL: `auth-service/db/schema.sql`, `account-service/db/schema.sql`, `payment-service/db/schema.sql`, `ledger-service/db/schema.sql`
- MongoDB: `notification-service/db/init-mongo.js`

See [docs/database.md](docs/database.md).

## Runbooks and ADRs

- [Deployment runbook](docs/runbook-deployment.md)
- [DLQ and reconciliation runbook](docs/runbook-dlq-and-reconciliation.md)
- [ADR: Kafka for events](docs/adr-001-kafka-for-events.md)

## Postman

Import [postman/Payment-System.postman_collection.json](postman/Payment-System.postman_collection.json). Set variables: `baseUrl` (e.g. http://localhost:8081), and after Login, `accessToken` and `userId` (from JWT `sub`).

## Project layout

```
payment-system/
├── eureka-server/
├── auth-service/
├── account-service/
├── payment-service/    # Orchestrator
├── ledger-service/
├── notification-service/
├── shared-contracts/
├── docs/
├── postman/
├── docker-compose.yml
└── pom.xml
```

## License

MIT (or your choice).
