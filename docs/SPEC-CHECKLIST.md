# Assignment requirement checklist

This checklist maps the supplied Sparknity PDF to the included implementation.

| PDF requirement | Included location / status |
|---|---|
| Java + Spring Boot REST backend | `backend/pom.xml`, `CrmApplication.java` |
| Spring Data JPA + Hibernate | `Domain.java`, `Repositories.java` |
| PostgreSQL native UUID IDs | `BaseEntity` in `Domain.java`; PostgreSQL configuration in `application.yml` |
| JPA created/updated auditing | `BaseEntity`, `@EnableJpaAuditing` |
| Users, Properties, Leads, Site Visits, Deals | `Domain.java` |
| User unique email and ADMIN/AGENT roles | `User` entity, `UserRepository`, `Role` |
| Property AVAILABLE/RESERVED/SOLD and indexes | `Property` entity |
| Lead pipeline stages | `LeadStage` enum |
| Site visit statuses and feedback | `SiteVisit` entity and update endpoint |
| Unique property per closed deal | `Deal` unique constraint |
| Transactional deal closure; property SOLD and lead WON | `CrmService.closeDeal` |
| DTO request/response boundary | `ApiModels.java` |
| Jakarta validation | Request DTO annotations |
| Mapper layer | Explicit mapping methods in `CrmService`; dedicated MapStruct package not yet split out |
| BCrypt | `SecurityConfig.passwordEncoder` |
| JWT stateless security | `JwtTokenProvider`, `JwtAuthenticationFilter`, `SecurityConfig` |
| Admin-only registration | `AuthController`, security matcher and method authorization |
| Agent-scoped lead list and ownership checks | `CrmService.leads`, stage/visit/deal service checks |
| Swagger/OpenAPI JWT scheme | `ApiDocumentation.java` |
| Central exception handling | `GlobalExceptionHandler.java` |
| React + Tailwind SPA | `frontend/` |
| Axios JWT interceptor and 401 logout | `src/api/axiosClient.js` |
| Auth context and protected routing | `src/context/AuthContext.jsx`, `ProtectedRoute.jsx` |
| Sidebar, navbar and reusable components | `src/components/` |
| Dashboard, Leads, Properties, Visits, Deals pages | `src/pages/` |
| Mobile responsive layout | Tailwind responsive classes and mobile sidebar drawer |
| UUIDs passed as route/API identifiers | UUID strings in API DTOs and endpoint paths |

## Important gaps to finish before final company submission
1. Split the consolidated Java files into the exact `config/`, `security/`, `entity/`, `repository/`, `dto/request/`, `dto/response/`, `mapper/`, `service/`, `controller/`, and `exception/` packages shown on PDF page 2.
2. Add a dedicated mapper package (MapStruct or manual mapper beans); current mapping helpers are in the service for compactness.
3. Add server-side pagination and sorting for properties and leads.
4. Add automated tests for role access, DTO validation, agent ownership and atomic deal closure.
5. Add robust authorization for deal listing and all site-visit/deal operations.
6. Add race-safe locking around deal closure and map database constraint violations to HTTP 409.
7. Add the PDF's dashboard upcoming-visits table (metrics endpoint currently provides KPI counts/revenue only).
8. Add a polished UI for Admin user registration and property filtering.
9. Verify the PATCH route contract with the company. The provided blueprint table says `PATCH /api/v1/site-visits` without showing an ID placeholder; this implementation uses `PATCH /api/v1/site-visits/{id}`.
10. Execute full local build/test using JDK 21+, Maven and Node 20+; this environment did not have Maven installed, so the backend has not been compiled here.
