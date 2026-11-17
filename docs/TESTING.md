# Manual test plan

1. Start PostgreSQL and the backend; confirm `http://localhost:8080/swagger-ui.html` loads.
2. Login as the bootstrap admin using configured `ADMIN_EMAIL` and `ADMIN_PASSWORD`.
3. Copy the JWT from the login response and use Swagger's Authorize control or Postman Bearer Token.
4. Register an AGENT using `/api/v1/auth/register`.
5. Create a property as ADMIN. Verify a UUID is returned and status is AVAILABLE.
6. Login as AGENT and create a lead. Verify its assignedAgentId is the agent UUID.
7. As ADMIN, list all leads; as AGENT, verify only assigned leads are returned.
8. Schedule a site visit for that lead and property.
9. Update the visit to COMPLETED and provide feedback.
10. Update the lead stage to NEGOTIATION.
11. Close a deal for that lead/property.
12. Verify the property becomes SOLD, lead becomes WON, deal appears in the ledger and dashboard totals update.
13. Attempt to close another deal for the same property; it should fail due to the SOLD check/unique property constraint.
14. Attempt ADMIN-only operations with an AGENT token; expect HTTP 403.
15. Submit invalid DTO values; expect HTTP 400 with field-level errors.
16. Call a protected endpoint without a token; expect HTTP 401.

## Sample Postman environment
- `baseUrl`: `http://localhost:8080`
- `token`: JWT returned by login
- Header for protected requests: `Authorization: Bearer {{token}}`
