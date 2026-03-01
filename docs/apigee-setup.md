# Apigee API Gateway Setup

Use **Apigee** as the API gateway in front of the payment system backend services.

## Backend service base URLs (when running via Docker Compose)

| Service            | Base URL                    |
|--------------------|-----------------------------|
| Auth Service       | http://auth-service:8081    |
| Account Service    | http://account-service:8082 |
| Payment Service    | http://payment-service:8083 |
| Ledger Service     | http://ledger-service:8084  |
| Notification Service | http://notification-service:8085 |

## Recommended Apigee configuration

1. **API Proxy**  
   Create an API proxy that routes to the backend by path, for example:
   - `/auth/*` → Auth Service
   - `/accounts/*` → Account Service
   - `/payments/*`, `/admin/*` → Payment Service
   - `/ledger/*` → Ledger Service
   - `/notifications/*` → Notification Service

2. **Target servers**  
   In Apigee, define target servers (or use TargetEndpoint with the URLs above).  
   If backend runs on the same host as Apigee, use `localhost:8081`, etc.; otherwise use the Docker service names or the host that runs the services.

3. **JWT validation**  
   - For paths that require auth (e.g. `/accounts/*`, `/payments/*`, `/ledger/*`), add a **Verify JWT** policy using the same secret and issuer as Auth Service (`app.jwt.secret`, `app.jwt.issuer`).
   - Extract `sub` (user id) from the JWT and send it as header `X-User-Id` to the backend (Auth Service expects this for account/me and similar).

4. **Rate limiting**  
   Use Apigee rate limit policies per app or per path to protect the backend.

5. **CORS**  
   Configure CORS in Apigee if the frontend is on a different origin.

## Example route rules (conceptual)

| Path pattern   | Backend target              | Auth required |
|----------------|-----------------------------|----------------|
| POST /auth/login | Auth Service /api/v1/login   | No            |
| POST /auth/refresh | Auth Service /api/v1/refresh | No         |
| POST /auth/register | Auth Service /api/v1/register | No        |
| /accounts/*    | Account Service /api/v1/*    | Yes (JWT)     |
| /payments/*    | Payment Service /api/v1/*    | Yes (JWT)     |
| /admin/*       | Payment Service /api/v1/admin/* | Yes (JWT, consider admin role) |
| /ledger/*      | Ledger Service /api/v1/*     | Yes (JWT)     |
| /notifications/* | Notification Service /api/v1/* | Yes (JWT)  |

## OpenAPI / Swagger

Each service exposes:
- `GET /v3/api-docs` — OpenAPI 3.0 JSON
- `GET /swagger-ui.html` — Swagger UI

When using Apigee, you can either expose these through the proxy or keep them internal. For production, consider exporting specs to a doc portal and pointing API consumers to Apigee base URL only.
