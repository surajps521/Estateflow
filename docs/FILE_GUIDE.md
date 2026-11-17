# File and folder guide

## Repository root
- `README.md`: prerequisites, setup commands, environment variables, login instructions, API summary and known scope limitations.
- `backend/`: Spring Boot REST API.
- `frontend/`: React single-page application.
- `docs/`: architecture, file guide and testing walkthrough.

## Backend
- `pom.xml`: Maven build descriptor and dependencies (Spring Web, JPA, Security, Validation, PostgreSQL, JWT and Swagger).
- `src/main/resources/application.yml`: port, PostgreSQL connection, Hibernate, JWT lifetime/secret, initial admin configuration and Swagger path.
- `CrmApplication.java`: Spring Boot entry point; enables JPA auditing and method security.
- `Domain.java`: shared UUID `BaseEntity`, audit timestamps, enums and five JPA entities. `User`, `Property`, `Lead`, `SiteVisit` and `Deal` map to the five required database tables.
- `Repositories.java`: Spring Data JPA interfaces. These provide standard CRUD methods and derived queries for user email, property status/type and agent-owned leads/visits.
- `ApiModels.java`: request and response DTO records. Jakarta Validation annotations reject malformed input.
- `Security.java`: BCrypt encoder, JWT creation/parsing, user lookup, bearer-token filter, stateless security rules and CORS configuration.
- `CrmService.java`: business logic, entity-to-DTO conversion, ownership checks, dashboard aggregation and transactional deal closure.
- `Controllers.java`: REST endpoint definitions. Controllers receive DTOs, call the service layer and return DTO responses.
- `Bootstrap.java`: creates the first ADMIN if the configured admin email does not exist. It is needed because public registration is prohibited by the assignment.
- `ApiDocumentation.java`: OpenAPI metadata and JWT Bearer security scheme for Swagger.
- `GlobalExceptionHandler.java`: standardized validation and HTTP error responses.

## Frontend
- `package.json`: npm scripts and dependencies.
- `vite.config.js`: Vite development server configuration.
- `tailwind.config.js` / `postcss.config.js`: Tailwind CSS build configuration.
- `.env.example`: API base URL template. Copy to `.env` for local development.
- `index.html`: browser HTML entry point.
- `src/main.jsx`: mounts React and imports global CSS.
- `src/App.jsx`: route tree, AuthProvider and protected application shell.
- `src/index.css`: Tailwind directives and shared base styles.
- `src/api/axiosClient.js`: central Axios instance; adds JWT to requests and logs out on HTTP 401.
- `src/context/AuthContext.jsx`: holds current user and login/logout operations.
- `src/components/common/ProtectedRoute.jsx`: redirects anonymous users and optionally restricts ADMIN-only screens.
- `src/components/common/StatusBadge.jsx`: consistent status labels.
- `src/components/common/Modal.jsx`: reusable dialog shell.
- `src/components/layout/Sidebar.jsx`: desktop navigation and mobile slide-out menu.
- `src/components/layout/Navbar.jsx`: header, mobile menu trigger and logout action.
- `src/components/layout/AppLayout.jsx`: combines sidebar, navbar and page outlet.
- `src/pages/LoginPage.jsx`: login form and error display.
- `src/pages/DashboardPage.jsx`: KPI metrics and closed revenue.
- `src/pages/PropertiesPage.jsx`: property inventory and ADMIN create form.
- `src/pages/LeadsBoardPage.jsx`: Kanban-style lead stages and lead creation.
- `src/pages/SiteVisitsPage.jsx`: visit list, scheduling and outcome updates.
- `src/pages/DealsPage.jsx`: deal ledger and deal closure form.

## How a request moves through the system
1. A React page calls the shared Axios client.
2. Axios attaches `Authorization: Bearer <JWT>`.
3. Spring Security's JWT filter validates the token and sets the authenticated principal.
4. The controller validates the incoming request DTO.
5. The service applies business rules and calls repositories.
6. JPA/Hibernate persists or reads PostgreSQL rows.
7. The service maps entities into response DTOs.
8. React renders the response.
