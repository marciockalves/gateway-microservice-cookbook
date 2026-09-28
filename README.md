# Gateway Microservice - Contract Processing API

A resilient, event-driven gateway microservice built with **Spring Boot 3** and **Java 21**. 
This application acts as an integration gateway between asynchronous messaging (Kafka) and external HTTP REST APIs, including event tracking and request execution logging.

---

## 🏗 System Architecture & Flow

[ Front-End / Clients ]
│
▼
[ BFF ]
│ (Publishes event)
▼
[ Kafka Topic ] ───► (Consumes message)
│
▼
[ Gateway Microservice ]
├── 1. Persists Event Record
├── 2. Maps Payload & Calls External API
└── 3. Logs Execution (Success / Error)
│
▼
[ External Contract API ]
(Emulated by WireMock)

---

## 🎯 Business Domain & Use Case

The primary responsibility of this gateway is to process **Contract Creation Events** received asynchronously:

1. **Consume Message:** Listens to incoming contract creation events from a Kafka topic.
2. **Event Tracking:** Creates a master entry in the `events` table (e.g., event type `CONTRACT_CREATION`).
3. **Execution & Logging:** 
   - Maps the incoming Kafka payload into the contract API format.
   - Dispatches an HTTP request to the External Contract API (WireMock).
   - Writes step-by-step logs into the `request_logs` table linked to the parent `event_id` (tracking start of consumption, HTTP request status, and any downstream failure details).

---

## 📊 Data Model Concept

* **`events`**: Represents the lifecycle of a single incoming message/business transaction.
  * Attributes: `id` (PK / Reference ID), `event_type`, `status`, `created_at`.
* **`request_logs`**: Represents individual processing steps or HTTP integration attempts related to an event.
  * Attributes: `id`, `event_id` (FK), `step_name`, `http_status`, `payload`, `error_message`, `timestamp`.

---

## 🛠 Tech Stack & Requirements

* **Language:** Java 21 (LTS)
* **Framework:** Spring Boot 3.3+
* **Messaging:** Spring Kafka
* **Database & Persistence:** Spring Data JPA / PostgreSQL (H2 / Testcontainers for tests)
* **HTTP Client:** Spring `RestClient` or `WebClient`
* **Mocking & Integration Testing:** WireMock, Testcontainers
* **Build Tool:** Maven

---

## ⚙️ Development Guidelines for Devin

When implementing tasks for this repository:
1. Always keep pull requests small, scoped, and functional.
2. Ensure every step includes unit or integration tests that pass before creating a PR.
3. Follow idiomatic Spring Boot 3 practices with Java 21 features (records, pattern matching, records for DTOs where appropriate).
