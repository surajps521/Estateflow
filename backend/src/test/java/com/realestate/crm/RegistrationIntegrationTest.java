package com.realestate.crm;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@SpringBootTest
@TestPropertySource(properties={
    "spring.datasource.url=jdbc:h2:mem:crm-registration-test;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.flyway.enabled=false",
    "app.bootstrap.admin-email=integration-admin@crm.local",
    "app.bootstrap.admin-password=ChangeMe123!"
})
@Transactional
class RegistrationIntegrationTest {
    @Autowired CrmService service;
    @Autowired UserRepository users;
    @Autowired AgentProfileRepository agentProfiles;
    @Autowired CustomerProfileRepository customerProfiles;

    @Test
    void registersAdminAgentAndCustomerRoles() {
        User admin=service.register(new RegisterRequest("integration-admin2@crm.local","Password123!",Role.ADMIN));
        User agent=service.register(new RegisterRequest("integration-agent@crm.local","Password123!",Role.AGENT));
        User customer=service.registerCustomer(new CustomerRegisterRequest(
            "integration-customer@crm.local","Password123!","Integration Customer","1234567890",
            "Bengaluru",new BigDecimal("5000000"),"Apartment"));

        assertEquals(Role.ADMIN,admin.getRole());
        assertEquals(Role.AGENT,agent.getRole());
        assertEquals(Role.CUSTOMER,customer.getRole());
        assertEquals(customer,customerProfiles.findByUserId(customer.getId()).orElseThrow().getUser());
        assertEquals(agent,agentProfiles.findByUserId(agent.getId()).orElseThrow().getUser());
        assertNotNull(users.findByEmail(customer.getEmail()));
    }

    @Test
    void customerSelfRegistrationCannotCreateAdminOrAgent() {
        User customer=service.registerCustomer(new CustomerRegisterRequest(
            "forced-customer@crm.local","Password123!","Forced Customer","1234567890",
            "Bengaluru",new BigDecimal("5000000"),"House"));

        assertEquals(Role.CUSTOMER,customer.getRole());
    }

    @Test
    void rejectsDuplicateCustomerEmail() {
        CustomerRegisterRequest request=new CustomerRegisterRequest(
            "duplicate-customer@crm.local","Password123!","Duplicate Customer","1234567890",
            "Bengaluru",new BigDecimal("5000000"),"Apartment");
        service.registerCustomer(request);

        assertThrows(ResponseStatusException.class,()->service.registerCustomer(request));
    }
}
