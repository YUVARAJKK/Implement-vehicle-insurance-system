# 🚗 Vehicle Insurance Management System

[![Java](https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.2.5-6DB33F?style=for-the-badge&logo=spring&logoColor=white)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-336791?style=for-the-badge&logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![JWT](https://img.shields.io/badge/JWT-Authentication-black?style=for-the-badge&logo=JSON%20web%20tokens)](https://jwt.io/)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg?style=for-the-badge)](LICENSE)

A **production-grade Vehicle Insurance Management System** built with Java 21 and Spring Boot 3. It provides a complete insurance lifecycle — from policy issuance and premium calculation to claim filing, fraud detection, and administrative analytics — all secured with JWT-based authentication and role-based access control.

---

## ✨ Key Features

| Feature | Description |
|---|---|
| 🔐 **JWT Authentication** | Stateless token-based auth with access & refresh tokens |
| 📋 **Policy Management** | Create, renew, and cancel insurance policies |
| 💰 **Premium Calculation** | Strategy Pattern: Comprehensive, Third-Party, Zero-Depreciation |
| 🏭 **Plan Factory** | Factory Pattern for dynamic insurance plan creation |
| 🚨 **Claims Workflow** | Multi-stage claim approval with auto-fraud detection |
| 🛡️ **Fraud Detection** | Rule-based engine flags suspicious claims automatically |
| 📧 **Email Notifications** | Async email dispatch for policy events via Spring Mail |
| 📊 **Admin Dashboard** | Revenue analytics, user stats, and claim summaries |
| 📅 **Policy Scheduler** | Automated expiry reminders via Spring Scheduling |
| 🔍 **Swagger UI** | Interactive API documentation at `/swagger-ui.html` |
| 🩺 **Actuator** | Health, metrics, and environment endpoints |
| 🔒 **Rate Limiting** | Bucket4j-based per-endpoint rate limiting |

---

## 🏗️ Architecture

```
com.insurance
├── config/              # Security, Async, OpenAPI, DataSeeder configs
├── controller/          # REST Controllers (Auth, Vehicle, Policy, Claim, Admin)
├── dto/
│   ├── request/         # Input DTOs (RegisterRequest, LoginRequest, etc.)
│   └── response/        # Output DTOs (ApiResponse, AuthResponse, etc.)
├── entity/              # JPA Entities (User, Vehicle, Policy, Claim, Payment, AuditLog)
├── exception/           # Custom exceptions + GlobalExceptionHandler
├── factory/             # PremiumStrategyFactory (Factory Pattern)
├── repository/          # Spring Data JPA Repositories
├── security/            # JWT Filter, Token Provider, UserDetails
├── service/             # Business Logic Services
└── strategy/
    ├── PremiumCalculationStrategy.java   # Interface
    └── impl/
        ├── ComprehensivePremiumStrategy.java
        ├── ThirdPartyPremiumStrategy.java
        └── ZeroDepreciationPremiumStrategy.java
```

### Design Patterns Used

- **Strategy Pattern** — Pluggable premium calculation algorithms
- **Factory Pattern** — Dynamic creation of premium strategies
- **Builder Pattern** — All entities and DTOs use Lombok `@Builder`
- **Repository Pattern** — Spring Data JPA abstracts all DB access
- **AOP / Auditing** — Automatic `createdAt`/`updatedAt` via `@EnableJpaAuditing`

---

## 🛠️ Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.2.5 |
| Security | Spring Security 6 + JJWT 0.11.5 |
| Database | PostgreSQL 15 (H2 for tests) |
| ORM | Spring Data JPA / Hibernate 6 |
| DTO Mapping | MapStruct 1.5.5 |
| Boilerplate | Lombok |
| API Docs | SpringDoc OpenAPI 2.3.0 (Swagger UI) |
| Rate Limiting | Bucket4j 8.7.0 |
| Caching | Spring Cache (Simple / in-memory) |
| Email | Spring Mail (SMTP / Gmail) |
| Testing | JUnit 5, Mockito, Spring Boot Test, H2 |
| Build | Maven 3 (Maven Wrapper included) |
| Container | Docker + Docker Compose |

---

## 🚀 Getting Started

### Prerequisites

- Java 21+
- PostgreSQL 15+ running locally
- Maven 3.9+ **or** use the included Maven Wrapper (`./mvnw`)

### 1. Clone the repository

```bash
git clone https://github.com/<your-username>/vehicle-insurance-management.git
cd vehicle-insurance-management
```

### 2. Create the database

```sql
CREATE DATABASE vehicle_insurance_db;
```

### 3. Configure environment (optional)

The defaults in `application.yml` work out of the box for local PostgreSQL with:
- **Host**: `localhost:5432`
- **Username**: `postgres`
- **Password**: `postgres`

To override, set these environment variables:

```bash
export MAIL_USERNAME=your-email@gmail.com
export MAIL_PASSWORD=your-gmail-app-password
export JWT_SECRET=YourSuperSecretKeyAtLeast64CharactersLongForHMACSHA512!
export ADMIN_EMAIL=admin@insurance.com
export ADMIN_PASSWORD=Admin@123456
```

### 4. Build and run

```bash
# Using Maven Wrapper (recommended — no local Maven needed)
./mvnw spring-boot:run

# Or with Maven installed
mvn spring-boot:run
```

The application starts on **http://localhost:8080**

---

## 🐳 Docker (Optional)

```bash
docker-compose up -d
```

This spins up PostgreSQL + the application together.

---

## 📖 API Documentation

Once running, visit:

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/api-docs

### Quick API Overview

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/api/v1/auth/register` | ❌ | Register new user |
| POST | `/api/v1/auth/login` | ❌ | Login and receive JWT |
| GET | `/api/v1/vehicles` | ✅ USER | List user's vehicles |
| POST | `/api/v1/vehicles` | ✅ USER | Register a vehicle |
| GET | `/api/v1/policies` | ✅ USER | List user's policies |
| POST | `/api/v1/policies` | ✅ USER | Create a new policy |
| POST | `/api/v1/claims` | ✅ USER | File a claim |
| GET | `/api/v1/admin/dashboard` | ✅ ADMIN | View analytics |
| PATCH | `/api/v1/admin/claims/{id}/approve` | ✅ ADMIN | Approve a claim |

---

## 🧪 Running Tests

```bash
# Run all tests (uses H2 in-memory DB — no PostgreSQL needed)
./mvnw test

# Run with coverage report
./mvnw verify
```

### Test Coverage

| Test Class | Type | Coverage |
|---|---|---|
| `AuthServiceTest` | Unit (Mockito) | Auth register, login, duplicate email |
| `PremiumCalculationStrategyTest` | Unit | All 3 premium strategies |
| `AuthControllerIntegrationTest` | Integration (MockMvc) | Auth endpoints end-to-end |

---

## 🔐 Default Credentials (Seeded on Startup)

| Role | Email | Password |
|------|-------|----------|
| Admin | `admin@insurance.com` | `Admin@123456` |

> ⚠️ Change these in production via environment variables `ADMIN_EMAIL` and `ADMIN_PASSWORD`.

---

## 📂 Project Structure

```
JAVA ASSIGNMENT PROJECT/
├── .mvn/                        # Maven Wrapper files
├── src/
│   ├── main/
│   │   ├── java/com/insurance/  # Application source code
│   │   └── resources/
│   │       └── application.yml  # Main configuration
│   └── test/
│       ├── java/com/insurance/  # Unit & integration tests
│       └── resources/
│           └── application.yml  # Test config (H2 in-memory)
├── Dockerfile
├── docker-compose.yml
├── pom.xml
├── mvnw / mvnw.cmd              # Maven Wrapper scripts
└── README.md
```

---

## 📄 License

This project is licensed under the **MIT License** — see the [LICENSE](LICENSE) file for details.

---

## 👨‍💻 Author

**Yuvaraj K K**  
Java Assignment Project — 2024
