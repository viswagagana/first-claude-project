# Runbook: Deployment

## Prerequisites

- Java 17, Maven 3.8+
- Docker and Docker Compose (for full stack)
- (Optional) Apigee or another gateway for production

## Build

From the repository root:

```bash
mvn clean package -DskipTests
```

This builds all modules including `eureka-server`, `auth-service`, `account-service`, `payment-service`, `ledger-service`, `notification-service`, and `shared-contracts`.

## Run with Docker Compose

1. Start infrastructure and services:

```bash
docker-compose up -d
```

2. Wait for health checks (Eureka, Postgres, Kafka, MongoDB). Then verify:

- Eureka: http://localhost:8761  
- Auth: http://localhost:8081/actuator/health  
- Account: http://localhost:8082/actuator/health  
- Payment: http://localhost:8083/actuator/health  
- Ledger: http://localhost:8084/actuator/health  
- Notification: http://localhost:8085/actuator/health  

3. Create a user and get a JWT:

```bash
curl -X POST http://localhost:8081/api/v1/register \
  -H "Content-Type: application/json" \
  -d '{"username":"test","email":"test@example.com","password":"password123"}'

curl -X POST http://localhost:8081/api/v1/login \
  -H "Content-Type: application/json" \
  -d '{"username":"test","password":"password123"}'
```

Use the `accessToken` from the response in the `Authorization: Bearer <token>` header for protected APIs.

## Run locally (without Docker)

1. Start Eureka, PostgreSQL (all 4 DBs), Kafka, and MongoDB (e.g. via Docker or local install).
2. Set environment variables or `application.yml` for each service (DB URLs, Kafka bootstrap, Eureka URL).
3. Run each service: `mvn -pl auth-service spring-boot:run`, etc.

## Rollback

- **Docker Compose:** `docker-compose down` then deploy a previous image set and `docker-compose up -d`.
- **Kubernetes:** Use your normal rollback (e.g. `kubectl rollout undo deployment/<name>`).

## Environment variables (summary)

| Variable           | Used by        | Description                    |
|--------------------|----------------|--------------------------------|
| JWT_SECRET         | Auth           | JWT signing secret (min 32 chars) |
| STRIPE_SECRET_KEY  | Payment        | Stripe API key                 |
| STRIPE_WEBHOOK_SECRET | Payment    | Stripe webhook signing secret  |
| PAYPAL_CLIENT_ID   | Payment        | PayPal client id               |
| POSTGRES_*         | Auth, Account, Payment, Ledger | DB connection          |
| KAFKA_BOOTSTRAP_SERVERS | Payment, Ledger, Notification | Kafka brokers   |
| MONGODB_URI        | Notification   | MongoDB connection string      |
| EUREKA_URI         | All services   | Eureka server URL              |
