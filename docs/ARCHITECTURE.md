# Architecture and business flow

## Layered backend
`Controller → Request DTO validation → Service → Repository → PostgreSQL`
Responses follow the reverse path with entity-to-response DTO mapping.

Controllers do not return JPA entities. This prevents leaking persistence details and avoids recursive relationship serialization.

## Domain relationships
- User has many assigned Leads.
- Lead references one assigned User.
- SiteVisit references one Lead, one Property and one User (agent).
- Deal references one Lead and one Property.
- A unique constraint on `deals.property_id` prevents multiple closed deals for the same property.
- Every entity inherits UUID `id`, `createdAt` and `updatedAt` from `BaseEntity`.

## Lead stages
`NEW → CONTACTED → VISIT_SCHEDULED → NEGOTIATION → WON`
A lead can also move to `LOST`. The API currently accepts any valid enum stage; add explicit transition validation if the business requires a strict state machine.

## Deal closure
The deal service runs in a transaction:
1. Find lead and property.
2. Verify the agent owns the lead (unless ADMIN).
3. Reject a property already marked SOLD.
4. Create the deal.
5. Set property status to SOLD.
6. Set lead stage to WON.
7. Commit all changes together.

The database unique constraint is important for concurrent requests. For high-concurrency production use, also lock the property row (`PESSIMISTIC_WRITE`/optimistic versioning) and translate unique-constraint failures into HTTP 409.

## Authentication
- Passwords are stored as BCrypt hashes.
- Login authenticates credentials and issues a signed JWT with UUID subject, email, role, issue time and expiration.
- The JWT filter checks signature/expiry and establishes the Spring Security context.
- API sessions are stateless; CSRF is disabled for the token-based API.
- ADMIN-only user creation and property creation are enforced by Spring Security.
