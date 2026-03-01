# Database scripts and setup

## Overview

| Service            | Database   | Script location                          |
|--------------------|------------|------------------------------------------|
| Auth Service       | PostgreSQL | `auth-service/db/schema.sql`             |
| Account Service    | PostgreSQL | `account-service/db/schema.sql`         |
| Payment Service    | PostgreSQL | `payment-service/db/schema.sql`         |
| Ledger Service     | PostgreSQL | `ledger-service/db/schema.sql`          |
| Notification Service | MongoDB  | `notification-service/db/init-mongo.js`  |

## PostgreSQL

- Each service uses its own database (auth_db, account_db, payment_db, ledger_db).
- Run the corresponding `schema.sql` once before starting the service (e.g. via `psql` or your migration tool).
- With Docker Compose, scripts are mounted into `/docker-entrypoint-initdb.d/` and run on first start.
- For local development without Docker, create DBs and run:

```bash
psql -U postgres -f auth-service/db/schema.sql    # after creating auth_db
psql -U postgres -f account-service/db/schema.sql # after creating account_db
# etc.
```

## MongoDB

- Notification Service uses `notification_db`.
- `init-mongo.js` creates collections and indexes; it is run automatically when using Docker Compose init.
- For local MongoDB, run: `mongosh notification_db < notification-service/db/init-mongo.js` (or equivalent).

## Flyway / Liquibase (optional)

You can add Flyway or Liquibase to each Spring Boot service and replace the one-off `schema.sql` with versioned migrations under `src/main/resources/db/migration/` (Flyway) or similar. The current setup uses `spring.jpa.hibernate.ddl-auto: validate` so that the app does not create or alter schema; it expects the schema to already exist from these scripts.
