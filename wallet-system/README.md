# SecureLedger Wallet API

A small Spring Boot REST API for wallet registration, JWT login, balance lookup, and transfers. It uses PostgreSQL for persistent data and H2 for automated tests.

## Stack

- Java 17, Spring Boot 3, Spring Security
- JWT bearer tokens and BCrypt password hashing
- Spring Data JPA / Hibernate
- PostgreSQL (local and deployment database); H2 (tests)

## Run locally

Start PostgreSQL with Docker Compose:

```bash
docker compose up -d postgres
```

Set a private JWT secret before starting the app. Use at least 32 random bytes; the app will not start without it.

PowerShell:

```powershell
$env:JWT_SECRET = "replace-with-a-long-random-secret-value"
./mvnw.cmd spring-boot:run
```

The API listens on `http://localhost:9090`. Swagger UI is at `/swagger-ui/index.html`.

To bootstrap an administrator, set both `WALLET_ADMIN_EMAIL` and `WALLET_ADMIN_PASSWORD` before launch. The password must be at least 12 characters. New registrations always receive the `USER` role.

## API flow

### 1. Register a user and wallet

`POST /api/auth/register`

```json
{
  "email": "alice@example.com",
  "password": "a-long-demo-password",
  "initialBalance": 100.00
}
```

The optional initial balance is a demo convenience. A real money product should fund wallets through a trusted payment or ledger process, never a public registration field.

### 2. Log in and get a token

`POST /api/auth/login`

```json
{
  "email": "alice@example.com",
  "password": "a-long-demo-password"
}
```

Use the returned `accessToken` as `Authorization: Bearer <token>` for wallet and admin endpoints.

### 3. Transfer funds

`POST /api/wallet/transfer` (authenticated)

```json
{
  "transactionReference": "TXN-DEMO-1",
  "fromAccount": "ACC-...",
  "toAccount": "ACC-...",
  "amount": 20.00
}
```

The authenticated user must own the source wallet. Transfers check the balance and use database row locks and a unique transaction reference to protect against concurrent and duplicate requests.

### 4. Read balance

`GET /api/wallet/accounts/{accountNumber}/balance` (authenticated)

Users can read their own wallet balance. An administrator can read any wallet.

### Admin example

`GET /api/admin/user-count` requires the `ADMIN` role. Set the bootstrap admin environment variables to create or promote that account at startup.

## Data and tests

- PostgreSQL is configured by default. Override `DATABASE_URL`, `DATABASE_USERNAME`, and `DATABASE_PASSWORD` as needed.
- `docker-compose.yml` starts a local PostgreSQL instance.
- Tests use an isolated in-memory H2 database.
- Hibernate schema update is enabled for this learning project. A deployed system should use versioned migrations and a reviewed secret/database configuration.
