# InboxIQ - AI-Powered Gmail Subscription Manager (Java Backend)

A Spring Boot backend that classifies email senders as **keep** or **unsubscribe** using the Gemini AI API, stores everything in MySQL through **Hibernate (JPA)**, and exposes analytics through **raw JDBC**.

## Tech stack
| Layer | Technology |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 3.3 |
| ORM | Hibernate (Spring Data JPA) |
| Raw SQL | JDBC (`JdbcTemplate`) |
| Database | MySQL 8 |
| AI | Google Gemini API (WebClient) |
| Validation | Jakarta Bean Validation |
| Build | Maven |

## Architecture
Controller -> Service -> Repository (JPA / JDBC) -> MySQL

- `entity/` Hibernate entities: User (1) -> (many) Sender, User (1) -> (many) ScanHistory
- `repository/` JPA repositories plus `AnalyticsRepository` (JdbcTemplate)
- `service/` ScanService (orchestration, upsert), GeminiClassifierService, UserService
- `controller/` REST endpoints; DTOs avoid the bidirectional-entity recursion problem
- `config/` GlobalExceptionHandler returns structured JSON errors

## Features
- AI classification of senders with confidence and reason
- Upsert on rescan (no duplicate sender rows)
- User override of the AI decision
- Unsubscribe with dry-run mode
- Email input validation (400 responses)
- Raw JDBC analytics query

## Setup
1. Install JDK 17, Maven and MySQL 8
2. Copy `src/main/resources/application.properties.example` to `application.properties` and fill in your MySQL password and Gemini key
3. Run: `mvn spring-boot:run`

## Endpoints
| Method | URL | Purpose |
|---|---|---|
| GET | /api/user?email= | Create or fetch a user |
| GET | /api/test-classify | Test the Gemini call |
| GET | /api/scan-demo?email= | Classify sample senders and save them |
| GET | /api/senders/user/{userId} | List a user's senders |
| PUT | /api/senders/{id}/override?decision=keep | Override the AI decision |
| POST | /api/senders/{id}/unsubscribe?dryRun=true | Unsubscribe (dry run by default) |
| GET | /api/analytics/summary/{userId} | JDBC decision counts |

## Status
The scan currently classifies sample senders. Live Gmail/Drive access through OAuth 2.0 is the planned next step.
