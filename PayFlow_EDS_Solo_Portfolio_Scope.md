# PayFlow EDS — Solo Portfolio Edition
## Event-Driven Payment System · Junior Java Developer Build

| Field | Details |
|---|---|
| Document Type | Portfolio Project Scope |
| Version | 1.0 |
| Status | Ready to Build |
| Architecture | Modular Monolith |
| Stack | Java 21 · Spring Boot 3.x · Spring Modulith · Maven |
| Intended For | Solo developer · Portfolio / GitHub showcase |

---

## Table of Contents

1. [What You're Building](#1-what-youre-building)
2. [Scope: What's In vs Out](#2-scope-whats-in-vs-out)
3. [Architecture Overview](#3-architecture-overview)
4. [Technology Stack](#4-technology-stack)
5. [Event System Design](#5-event-system-design)
6. [Database Design](#6-database-design)
7. [API Specification](#7-api-specification)
8. [Project Structure](#8-project-structure)
9. [Build Milestones](#9-build-milestones)
10. [What to Show on Your Portfolio](#10-what-to-show-on-your-portfolio)

---

## 1. What You're Building

PayFlow EDS (Solo Edition) is a **Modular Monolith** payment event-driven system — a single Spring Boot application internally split into clean, bounded modules that communicate via domain events.

This is a strong portfolio project because it demonstrates:
- Real-world architecture patterns (not just CRUD)
- Event-driven design with Spring Modulith
- Domain-separated modules with enforced boundaries
- A payment domain — highly relevant in fintech hiring

**Core flow you will build:**
> Client submits a payment → system validates it → runs a fraud check → records a ledger entry → sends a notification

---

## 2. Scope: What's In vs Out

### ✅ In Scope (Build These)

| Module | What It Does |
|---|---|
| **Payment Ingestion** | Accept, validate, and persist payment requests via REST API |
| **Fraud Detection** | Simple rule-based scoring; approve or decline the payment |
| **Ledger** | Record double-entry journal entries when a payment is approved |
| **Notifications** | Log a notification (console or email via a mock/stub) |
| **Audit Log** | Append-only record of all events (immutable, append-only table) |
| **Identity / Security** | OAuth2 Resource Server integration for secure API access |

### ❌ Cut (Too Complex for One Person)

| Removed | Why |
|---|---|
| Apache Kafka | Overkill solo; use Spring's internal event bus instead |
| Redis / distributed locking | No distributed system, so not needed |
| Reconciliation module | Requires settlement file integration — skip for now |
| Kubernetes / Helm / ArgoCD | Use Docker Compose for local dev; deploy to Railway or Render |
| Gatling load tests | Target 2,000 TPS is a team/production goal — not needed here |
| PCI-DSS compliance | Real compliance requires a team + QSA; note it as "inspired by" |
| HashiCorp Vault | Use Spring Boot's `application.yml` with env variables |
| Horizontal Pod Autoscaler | Single instance is fine for portfolio |
| Multiple environments (dev/staging/prod) | One local + one deployed environment is enough |

---

## 3. Architecture Overview

### Single-Process Modular Monolith

```
┌─────────────────────────────────────────────────────┐
│                  PayFlow App (1 JVM)                │
│                                                     │
│  ┌────────────┐     Spring Application Events       │
│  │  Payments  │ ──► Fraud ──► Payments ──► Ledger  │
│  │  REST API  │                    │                │
│  └────────────┘                    └──► Notify      │
│                                         │           │
│                                         └──► Audit  │
└─────────────────────────────────────────────────────┘
         │
    PostgreSQL (single DB, per-module schemas)
```

### Module Communication
- Modules talk to each other **only** via Spring Application Events — never via direct method calls across boundaries
- Spring Modulith enforces this at compile-time and in tests
- Each module has its **own DB schema** — no cross-module SQL joins

### Event Flow: Payment Lifecycle

```
POST /api/v1/payment
        │
[Payment Module]
  ├─ Validate & persist (status: PENDING)
  └─ Publish ──► Payment.Transaction.Initiated
                        │
              ┌─────────┴──────────┐
              ▼                    ▼
          [Fraud]              [Audit]
              │
              └─► Fraud.Assessment.Completed
                        │
               ┌────────┴────────┐
               ▼                 ▼
  Payment.Transaction.Authorised  Payment.Transaction.Declined
               │                         │
          [Ledger]                 [Notification]
               │
          [Notification]
               │
           [Audit]
```

---

## 4. Technology Stack

### Core Stack

| Component | Technology | Notes |
|---|---|---|
| Language | Java 21 | Use virtual threads (Project Loom) |
| Framework | Spring Boot 3.3.x | Starter parent in POM |
| Module Boundary | Spring Modulith 1.x | Enforces module isolation |
| Internal Events | Spring Application Events | No Kafka needed |
| REST API | Spring Web MVC | Synchronous endpoints |
| Database | PostgreSQL 16 | Or H2 in-memory for tests |
| ORM | Spring Data JPA + Hibernate 6 | Per-module schemas |
| Security | Spring Security + OAuth2 Resource Server | JWT-based token validation |
| Build | Maven (multi-module) | One parent POM, one module per domain |
| Containers | Docker + Docker Compose | For local PostgreSQL |

### Testing Stack

| Tool | Purpose |
|---|---|
| JUnit 5 + AssertJ | Unit tests per module |
| Spring Modulith Test | Verify no illegal cross-module calls |
| Testcontainers | Real PostgreSQL in integration tests |
| WireMock | Mock external calls (if any) |

### Deployment (Portfolio)

- **Local**: Docker Compose (app + PostgreSQL)
- **Public**: Deploy to [Railway](https://railway.app) or [Render](https://render.com) — both have free tiers and support Spring Boot + PostgreSQL

---

## 5. Event System Design

### Event Naming Convention
`<Domain>.<Entity>.<PastTenseVerb>` — e.g. `Payment.Transaction.Initiated`

### Event Envelope (all events extend this)

```java
public abstract class DomainEvent {
    private final String eventId = UUID.randomUUID().toString();
    private final String correlationId;
    private final Instant occurredAt = Instant.now();
    // constructor + getters
}
```

### Events to Implement

| Event | Producer | Consumers |
|---|---|---|
| `Payment.Transaction.Initiated` | Payments | Fraud, Audit |
| `Fraud.Assessment.Completed` | Fraud | Payments, Audit |
| `Payment.Transaction.Authorised` | Payments | Ledger, Notifications, Audit |
| `Payment.Transaction.Declined` | Payments | Notifications, Audit |
| `Ledger.Entry.Posted` | Ledger | Audit |
| `Notification.Sent` | Notifications | Audit |

### Idempotency (Simplified)
Use a unique constraint on `idempotency_key` in the `payment.transactions` table. Return the existing result if a duplicate key is submitted — no Redis needed.

---

## 6. Database Design

### Schema Strategy
Each module gets its own PostgreSQL schema. Cross-schema queries are **forbidden**.

| Module | Schema |
|---|---|
| Payments | `payment` |
| Fraud | `fraud` |
| Ledger | `ledger` |
| Notifications | `notifications` |
| Audit | `audit` |

Use **Flyway** for versioned migrations — one migrations folder per schema.

### Key Tables

**`payment.transactions`**
| Column | Type | Notes |
|---|---|---|
| `id` | UUID PK | |
| `correlation_id` | UUID | Unique per payment flow |
| `idempotency_key` | VARCHAR(128) | Unique constraint |
| `amount` | DECIMAL(18,4) | |
| `currency` | CHAR(3) | ISO 4217 e.g. `USD`, `GHS` |
| `status` | VARCHAR(20) | PENDING → AUTHORISED / DECLINED |
| `merchant_id` | UUID | |
| `created_at` | TIMESTAMP | UTC |
| `updated_at` | TIMESTAMP | UTC |

**`fraud.assessments`**
| Column | Type | Notes |
|---|---|---|
| `id` | UUID PK | |
| `transaction_id` | UUID | FK reference (no join across schemas) |
| `score` | INTEGER | 0–100 |
| `decision` | VARCHAR(10) | APPROVE / DECLINE |
| `assessed_at` | TIMESTAMP | |

**`ledger.journal_entries`**
| Column | Type | Notes |
|---|---|---|
| `id` | UUID PK | |
| `correlation_id` | UUID | |
| `debit_account` | VARCHAR(50) | |
| `credit_account` | VARCHAR(50) | |
| `amount` | DECIMAL(18,4) | |
| `currency` | CHAR(3) | |
| `posted_at` | TIMESTAMP | Immutable once posted |

**`audit.event_log`**
| Column | Type | Notes |
|---|---|---|
| `id` | UUID PK | |
| `correlation_id` | UUID | |
| `event_type` | VARCHAR(100) | e.g. `Payment.Transaction.Initiated` |
| `payload` | JSONB | Full event payload |
| `occurred_at` | TIMESTAMP | |

---

## 7. API Specification

### Base URL
`http://localhost:8080/api/v1`

### Authentication
OAuth2 (JWT) Bearer token via header: `Authorization: Bearer <token>`. Configure JWK Set URI in `application.yml`.

### Endpoints

| Method | Path | Description |
|---|---|---|
| `POST` | `/payment` | Submit a new payment |
| `GET` | `/payment/{id}` | Get payment by ID |
| `GET` | `/payment` | List payment (filter by status) |
| `GET` | `/payment/{id}/events` | Get full event history for a payment |
| `GET` | `/ledger/journal/{correlationId}` | Get journal entries for a payment |
| `GET` | `/fraud/assessments/{transactionId}` | Get fraud result for a transaction |
| `GET` | `/audit/events` | Query audit log |

### Payment Request Body

```json
{
  "merchantId": "uuid",
  "amount": "150.00",
  "currency": "USD",
  "paymentMethod": {
    "type": "card",
    "token": "tok_test_1234"
  },
  "payeeAccountId": "uuid",
  "idempotencyKey": "unique-client-key-123"
}
```

### Error Format (RFC 7807)

```json
{
  "type": "https://payflow.io/errors/validation",
  "title": "Validation Failed",
  "status": 400,
  "detail": "Amount must be greater than zero",
  "correlationId": "uuid"
}
```

---

## 8. Project Structure

```
payflow-eds/
├── pom.xml                        # Parent POM
├── app/                           # Spring Boot entry point
│   └── src/main/java/io/payflow/
│       └── PayFlowApplication.java
├── modules/
│   ├── payment/                  # payflow.payment module
│   │   └── src/
│   │       ├── main/java/
│   │       │   ├── api/           # REST controllers
│   │       │   ├── domain/        # Entities, events, service
│   │       │   └── infra/         # JPA repos, mappers
│   │       └── test/java/
│   ├── fraud/                     # payflow.fraud module
│   ├── ledger/                    # payflow.ledger module
│   ├── notifications/             # payflow.notifications module
│   └── audit/                     # payflow.audit module
├── shared/
│   └── events/                    # Shared event envelope DTOs
├── db/
│   └── migrations/
│       ├── payment/              # Flyway V1__init.sql etc.
│       ├── fraud/
│       ├── ledger/
│       ├── notifications/
│       └── audit/
├── docker-compose.yml             # PostgreSQL for local dev
└── docs/
    └── adr/                       # Architecture Decision Records
```

---

## 9. Build Milestones

Work through these phases one at a time. Each phase produces a working, committable state.

| Phase | Name | Est. Time | Deliverables |
|---|---|---|---|
| 0 | **Project Scaffold** | 3–4 days | Maven multi-module POM, Spring Boot app boots, Spring Modulith configured, Docker Compose with PostgreSQL, Flyway running |
| 1 | **Payment Ingestion** | 1 week | `POST /payment` endpoint, transaction persisted, `Payment.Transaction.Initiated` event fired, idempotency key enforced, basic tests |
| 2 | **Fraud Detection** | 1 week | Rule engine (hardcoded rules: amount > threshold → flag), `Fraud.Assessment.Completed` event, payment status updated |
| 3 | **Ledger Module** | 1 week | Double-entry journal entry on `AUTHORISED`, `Ledger.Entry.Posted` event, journal query API |
| 4 | **Notifications & Audit** | 3–4 days | Console/log notification on AUTHORISED and DECLINED, append-only audit log consuming all events |
| 5 | **Polish & Deploy** | 3–4 days | README, Swagger/OpenAPI docs, deployed to Railway or Render, Spring Modulith tests green |

**Total: ~5–6 weeks** (part-time) or **2–3 weeks** (full-time focus)

### Definition of Done (Per Module)
- Unit tests passing for domain logic
- Spring Modulith verification test passes (no illegal cross-module access)
- Flyway migration committed
- Module README section written
- REST endpoint documented in Swagger

---

## 10. What to Show on Your Portfolio

When writing up this project on your CV or GitHub README, highlight:

- **Spring Modulith** — most Java devs haven't used it; it shows you understand bounded contexts
- **Event-driven design** — you understand decoupled, async-capable architecture
- **Modular Monolith** — you know the tradeoffs vs microservices; you can explain why this pattern was chosen
- **Transactional consistency** — explain that each payment write and its event are committed atomically
- **Layered testing** — unit tests, integration tests with Testcontainers, architecture tests with Spring Modulith Test

### Suggested GitHub README Structure
1. What the project does (1 paragraph)
2. Architecture diagram (use the event flow diagram from Section 3)
3. Tech stack badges
4. How to run locally (`docker-compose up`, `./mvnw spring-boot:run`)
5. API examples (curl commands or Swagger screenshot)
6. Module breakdown table
7. What you'd add next (Kafka, Redis, reconciliation) — shows you understand the full picture

---

*PayFlow EDS Solo Edition — Portfolio Build Guide*
*Adapted from the full-team PayFlow EDS v1.0 specification*
