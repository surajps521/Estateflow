-- Reference only: development application uses Hibernate ddl-auto=update.
-- UUID primary keys and FK relationships are generated from JPA entities.
-- Use Flyway/Liquibase migrations for production deployments.
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- db/migration/V2__align_users_role_constraint.sql keeps the users.role
-- database constraint consistent with the three application roles.
