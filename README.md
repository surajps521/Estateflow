# Real Estate CRM – Sparknity Assignment Starter

A full-stack starter implementation following the supplied 6-page Real Estate CRM Technical Architecture & Engineering Blueprint.

## Stack
- Backend: Java 21, Spring Boot 3.3.5, Spring Security, Spring Data JPA, Hibernate, JWT (JJWT), Jakarta Validation, springdoc OpenAPI
- Database: PostgreSQL with native UUID primary keys and JPA auditing
- Frontend: React 18, Vite, React Router, Axios, Tailwind CSS
- API prefix: `/api/v1`

## Features included
- Bootstrap initial ADMIN account (environment-configurable)
- Role-based access for ADMIN, AGENT and CUSTOMER accounts
- Customer-to-agent assignment requests, notifications and property recommendations
- JWT login, BCrypt password hashing, stateless Spring Security and role restrictions
- UUID-based User, Property, Lead, SiteVisit and Deal entities with audit timestamps
- Property creation/listing and status/type filters
- Lead creation, agent assignment, agent-scoped listing and stage updates
- Site visit scheduling and outcome/feedback updates
- Transactional deal closure that marks property SOLD and lead WON
- Dashboard metrics and deal ledger
- DTO request/response records and validation
- Swagger UI and centralized error responses
- Responsive React pages for Dashboard, interactive property discovery, Leads, Site Visits and Deals

## Prerequisites
- JDK 21+
- Maven 3.9+
- PostgreSQL 14+
- Node.js 20+ and npm

## 1. Create database
```sql
CREATE DATABASE real_estate_crm;
```

## 2. Configure backend
Open a terminal in `backend/`. Set environment variables (PowerShell example):
```powershell
$env:DB_URL="jdbc:postgresql://localhost:5432/real_estate_crm"
$env:DB_USERNAME="postgres"
$env:DB_PASSWORD="YOUR_POSTGRES_PASSWORD"
$env:JWT_SECRET="replace-with-a-random-secret-at-least-32-characters-long"
$env:ADMIN_EMAIL="admin@crm.local"
$env:ADMIN_PASSWORD="ChangeMe123!"
mvn spring-boot:run
```
The initial admin is created only if that email does not already exist. Change the development password immediately. Do not use the default credentials or development JWT secret in production.

Backend: `http://localhost:8080`
Swagger: `http://localhost:8080/swagger-ui.html`

## 3. Run frontend
Open another terminal in `frontend/`:
```bash
npm install
```
Copy `.env.example` to `.env` and keep the API URL:
```env
VITE_API_BASE_URL=http://localhost:8080/api/v1
```
Then:
```bash
npm run dev
```
Open `http://localhost:5173`.

## 4. First login
Use the bootstrap credentials configured with `ADMIN_EMAIL` and `ADMIN_PASSWORD`. The sample values above are development-only. Login returns a JWT. The React Axios client attaches it to subsequent requests.

## API overview
| Method | Endpoint | Access | Purpose |
|---|---|---|---|
| POST | `/api/v1/auth/login` | Public | Login and return JWT |
| POST | `/api/v1/auth/register` | ADMIN | Create ADMIN or AGENT |
| POST | `/api/v1/auth/register/customer` | Public | Self-register a CUSTOMER account |
| GET | `/api/v1/agents` | Authenticated | List active agents |
| GET | `/api/v1/agents/{id}` | Authenticated | View an agent profile |
| GET | `/api/v1/properties` | Authenticated | List/filter properties |
| POST | `/api/v1/properties` | ADMIN | Create property |
| GET | `/api/v1/leads` | ADMIN, AGENT | Admin sees all; agent sees assigned |
| POST | `/api/v1/leads` | ADMIN, AGENT | Create lead |
| PATCH | `/api/v1/leads/{id}/stage` | ADMIN, AGENT | Change stage |
| GET | `/api/v1/site-visits` | ADMIN, AGENT | List visits |
| POST | `/api/v1/site-visits` | ADMIN, AGENT | Schedule visit |
| PATCH | `/api/v1/site-visits/{id}` | ADMIN, AGENT | Update status/feedback |
| GET | `/api/v1/deals` | ADMIN, AGENT | List closed deals |
| POST | `/api/v1/deals` | ADMIN, AGENT | Close deal |
| GET | `/api/v1/dashboard` | ADMIN, AGENT | Dashboard metrics |
| POST | `/api/v1/agent-requests` | CUSTOMER | Select an agent and submit a property enquiry |
| GET | `/api/v1/agent-requests/my` | CUSTOMER | View own assignment requests |
| GET | `/api/v1/agent-requests/pending` | AGENT | View pending customer requests |
| PATCH | `/api/v1/agent-requests/{id}/accept` | AGENT | Accept an assignment request |
| PATCH | `/api/v1/agent-requests/{id}/reject` | AGENT | Reject an assignment request |
| POST | `/api/v1/recommendations` | AGENT | Recommend a property to an assigned customer |
| GET | `/api/v1/recommendations/my` | CUSTOMER | View property recommendations |
| GET | `/api/v1/notifications` | Authenticated | List notifications |
| PATCH | `/api/v1/notifications/{id}/read` | Authenticated | Mark an owned notification read |
| GET | `/api/v1/leads/my` | CUSTOMER | View own buying progress |
| GET | `/api/v1/site-visits/my` | CUSTOMER | View own visits |
| GET | `/api/v1/deals/my` | CUSTOMER | View own completed deals |
| GET | `/api/v1/admin/users` | ADMIN | View all accounts |
| PATCH | `/api/v1/admin/users/{id}/active?value=false` | ADMIN | Activate/deactivate an account |

All protected requests require `Authorization: Bearer <JWT>`.

## Example request bodies
Login:
```json
{"email":"admin@crm.local","password":"ChangeMe123!"}
```
Register an agent or customer (must be called by ADMIN):
```json
{"email":"agent@example.com","password":"AgentPass123!","role":"AGENT"}
```
Customer self-registration:
```json
{"email":"customer@example.com","password":"CustomerPass123!","displayName":"Asha Rao","phone":"9876543210","preferredLocation":"Bengaluru","budgetMax":9000000,"requirements":"2BHK with parking"}
```
Create property:
```json
{"title":"Lakeview Residency – 2BHK","type":"Apartment","price":8500000,"areaSqft":1250}
```
Create lead:
```json
{"fullName":"Rahul Sharma","phone":"9876543210","budgetMax":9000000}
```
Create site visit:
```json
{"leadId":"LEAD_UUID","propertyId":"PROPERTY_UUID","visitTime":"2026-12-01T10:30:00Z"}
```
Close deal:
```json
{"leadId":"LEAD_UUID","propertyId":"PROPERTY_UUID","finalPrice":8200000,"closedDate":"2026-12-05"}
```

## Folder/file guide
See `docs/FILE_GUIDE.md` for the purpose and working of the important files and folders, `docs/ARCHITECTURE.md` for request/data flows, and `docs/TESTING.md` for a manual test sequence.

## Important implementation notes / scope
This is a runnable starter/reference implementation, not a production-certified system. Review and extend it before submission:
- Add automated unit/integration tests and a complete Postman collection.
- Add pagination/sorting to property and lead endpoints; current list responses are unpaginated.
- Add property update/delete and richer search filters if required by the evaluator.
- Tighten method-level authorization consistently, including deal listing and agent ownership rules.
- Consider refresh tokens, token revocation, secure cookie strategy or hardened storage, rate limiting, audit logs and production secret management.
- Add a formal database migration tool (Flyway/Liquibase) instead of relying on `ddl-auto: update`.
- Review race conditions during deal closure; the unique property constraint is the final database-level protection.
- The supplied blueprint mentions PATCH `/api/v1/site-visits`; this starter uses `/api/v1/site-visits/{id}` because an individual visit must be identified for updating.
- The PDF mentions MapStruct/manual mappers. This starter uses explicit DTO mapping methods in the service to keep the sample dependency-light; a dedicated mapper package can be introduced during refinement.
